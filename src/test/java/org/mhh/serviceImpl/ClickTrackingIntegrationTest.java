package org.mhh.serviceImpl;

import org.junit.jupiter.api.Test;
import org.mhh.domain.Url;
import org.mhh.exception.LinkExpiredException;
import org.mhh.exception.UrlNotFoundException;
import org.mhh.repository.UrlRepository;
import org.mhh.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@Import({UrlServiceImpl.class, ClickTrackingServiceImpl.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ClickTrackingIntegrationTest {
    @Autowired UrlRepository repository;
    @Autowired UrlService service;

    @Test
    void concurrentRedirectsEachAddOneClick() throws Exception {
        Url link = saveLink("active", null);
        ExecutorService workers = Executors.newFixedThreadPool(12);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> redirects = new ArrayList<>();
        try {
            for (int request = 0; request < 100; request++) {
                redirects.add(workers.submit(() -> {
                    start.await();
                    return service.getOriginalUrlAndTrackClick("active");
                }));
            }
            start.countDown();
            for (Future<String> redirect : redirects) {
                assertEquals(link.getOriginalUrl(), redirect.get(30, TimeUnit.SECONDS));
            }
            assertEquals(100, service.findByShortCode("active").getClickCount());
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void detailsAndRejectedRedirectsDoNotCount() {
        saveLink("expired", ZonedDateTime.now().minusDays(1));
        service.findByShortCode("expired");
        assertThrows(LinkExpiredException.class, () -> service.getOriginalUrlAndTrackClick("expired"));
        assertThrows(UrlNotFoundException.class, () -> service.getOriginalUrlAndTrackClick("missing"));
        assertEquals(0, repository.findByShortCode("expired").orElseThrow().getClickCount());
    }

    private Url saveLink(String code, ZonedDateTime expiration) {
        Url link = new Url();
        link.setShortCode(code);
        link.setOriginalUrl("https://example.org/" + code);
        link.setExpirationDate(expiration);
        return repository.save(link);
    }
}
