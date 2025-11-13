package org.mhh.serviceImpl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mhh.dto.UrlDTO;
import org.mhh.exception.UrlNotFoundException;
import org.mhh.mapper.UrlMapper;
import org.mhh.service.UrlService;
import org.mhh.domain.Url;
import org.mhh.repository.UrlRepository;
import org.mhh.util.Base62Encoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class UrlServiceImpl implements UrlService {

    private UrlRepository urlRepository;
    private final UrlMapper urlMapper;

    @Override
    @Transactional
    public Url createUrl(UrlDTO urlsDto) {
        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(urlsDto.getOriginalUrl());
        if (existingUrl.isPresent()) {
            log.info("URL {} already exists. Returning existing short URL: {}", urlsDto.getOriginalUrl(), existingUrl.get().getShortUrl());
            return existingUrl.get();
        }

        Url url = urlMapper.UrlDTOToUrls(urlsDto);
        Url savedUrl = urlRepository.save(url);
        String shortUrl = Base62Encoder.encode(savedUrl.getId());
        savedUrl.setShortUrl(shortUrl);
        return urlRepository.save(savedUrl);
    }

    @Override
    public List<Url> getUrls() {
        return urlRepository.findAll();
    }

    @Override
    public String getOriginalUrlFromShortUrl(String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortUrl));
        return url.getOriginalUrl();
    }

    @Override
    public void deleteShortUrl(String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortUrl));
        urlRepository.delete(url);
    }

}
