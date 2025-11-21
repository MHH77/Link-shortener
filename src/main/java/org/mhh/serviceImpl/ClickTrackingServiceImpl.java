package org.mhh.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mhh.domain.Url;
import org.mhh.repository.UrlRepository;
import org.mhh.service.ClickTrackingService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @auther:MHEsfandiari
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ClickTrackingServiceImpl implements ClickTrackingService {

    private final UrlRepository urlRepository;

    @Override
    @Async
    @Transactional
    public void trackClick(Long urlId) {
        log.info("Starting async click tracking for URL ID: {}", urlId);
        try {
            // یک تأخیر کوچک برای شبیه‌سازی کار سنگین و دیدن تفاوت تردها در لاگ
            Thread.sleep(100);

            Url url = urlRepository.findById(urlId).orElse(null);
            if (url != null) {
                url.setClickCount(url.getClickCount() + 1);
                urlRepository.save(url);
                log.info("Successfully tracked click for URL ID: {}. New count: {}", urlId, url.getClickCount());
            } else {
                log.warn("Could not track click. URL with ID {} not found.", urlId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Async click tracking was interrupted for URL ID: {}", urlId, e);
        }
    }
}