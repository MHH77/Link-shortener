package org.mhh.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mhh.dto.CreateUrlRequest;
import org.mhh.exception.UrlNotFoundException;
import org.mhh.service.UrlService;
import org.mhh.domain.Url;
import org.mhh.repository.UrlRepository;
import org.mhh.util.Base62Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;

    @Value("${url-shortener.id-offset}")
    private long idOffset;

    @Override
    @Transactional
    public Url createUrl(CreateUrlRequest request) {
        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(request.getOriginalUrl());
        if (existingUrl.isPresent()) {
            log.info("URL '{}' already exists. Returning existing entry.", request.getOriginalUrl());
            return existingUrl.get();
        }

        Url newUrl = new Url();
        newUrl.setOriginalUrl(request.getOriginalUrl());
        newUrl.setClickCount(0);

        Url savedUrl = urlRepository.save(newUrl);

        long effectiveId = savedUrl.getId() + idOffset;
        String shortCode = Base62Encoder.encode(effectiveId);

        savedUrl.setShortCode(shortCode);
        return urlRepository.save(savedUrl);
    }
    @Override
    public Url findByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));
    }

    @Override
    @Transactional
    public String getOriginalUrlAndTrackClick(String shortCode) {
        Url url = findByShortCode(shortCode);

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        log.info("Redirecting short code '{}' to '{}'. Total clicks: {}",
                shortCode, url.getOriginalUrl(), url.getClickCount());

        return url.getOriginalUrl();
    }

    @Override
    @Transactional
    public void deleteByShortCode(String shortCode) {
        if (!urlRepository.existsByShortCode(shortCode)) {
            throw new UrlNotFoundException("Cannot delete. Short code not found: " + shortCode);
        }
        urlRepository.delete(urlRepository.findByShortCode(shortCode).get());
        log.info("Successfully deleted URL with short code: {}", shortCode);
    }
}