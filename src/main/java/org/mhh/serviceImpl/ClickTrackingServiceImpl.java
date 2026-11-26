package org.mhh.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.mhh.exception.UrlNotFoundException;
import org.mhh.repository.UrlRepository;
import org.mhh.service.ClickTrackingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClickTrackingServiceImpl implements ClickTrackingService {
    private final UrlRepository urlRepository;

    @Override
    @Transactional
    public void trackClick(Long urlId) {
        if (urlRepository.incrementClickCount(urlId) == 0) {
            throw new UrlNotFoundException("URL not found: " + urlId);
        }
    }
}
