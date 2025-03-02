package org.mhh.serviceImpl;

import lombok.AllArgsConstructor;
import org.mhh.dto.UrlDTO;
import org.mhh.mapper.UrlMapper;
import org.mhh.service.UrlService;
import org.mhh.domain.Urls;
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
    public Urls createUrl(UrlDTO urlsDto) {
        Optional<Urls> existingUrl = urlRepository.findByOriginalUrl(urlsDto.getOriginalUrl());
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }
        Urls url = urlMapper.UrlDTOToUrls(urlsDto);
        url.setShortUrl(generateUniqueShortUrl());
        return urlRepository.save(url);
    }

    @Override
    public List<Urls> getUrls() {
        return urlRepository.findAll();
    }

    @Override
    public String getShortUrlFromOriginal(String shortUrl) {
        Urls url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new RuntimeException("Short URL not found."));
        return url.getOriginalUrl();
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
