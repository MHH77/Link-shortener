package org.mhh.serviceImpl;

import lombok.AllArgsConstructor;
import org.mhh.dto.UrlDTO;
import org.mhh.mapper.UrlMapper;
import org.mhh.service.UrlService;
import org.mhh.domain.Url;
import org.mhh.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class UrlServiceImpl implements UrlService {

    private UrlRepository urlRepository;
    private final UrlMapper urlMapper;

    @Override
    public Url createUrl(UrlDTO urlsDto) {
        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(urlsDto.getOriginalUrl());
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }
        Url url = urlMapper.UrlDTOToUrls(urlsDto);
        url.setShortUrl(generateUniqueShortUrl());
        return urlRepository.save(url);
    }

    @Override
    public List<Url> getUrls() {
        return urlRepository.findAll();
    }

    @Override
    public String getOriginalUrlFromShortUrl (String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new RuntimeException("Short URL not found."));
        return url.getOriginalUrl();
    }

    @Override
    public void deleteShortUrl(String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new RuntimeException("Short URL not found."));
        urlRepository.delete(url);
    }

    private String generateShortUrl() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder shortUrl = new StringBuilder();
        shortUrl.append("http://");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }

    private String generateUniqueShortUrl() {
        String shortUrl;
        boolean exists;

        do {
            shortUrl = generateShortUrl();
            exists = urlRepository.existsByShortUrl(shortUrl);
        } while (exists);

        return shortUrl;
    }

}
