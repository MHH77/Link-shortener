package org.mhh.controller;

import lombok.AllArgsConstructor;
import org.mhh.domain.Urls;
import org.mhh.dto.UrlDTO;
import org.mhh.service.UrlService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/url")
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/create")
    public Urls createUrls(@RequestBody UrlDTO urlDTO) {
        return urlService.createUrl(urlDTO);
    }

    @GetMapping("/all")
    public List<Urls> getAllUrls() {
        return urlService.getUrls();
    }

    @GetMapping("/short")
    public String getShortUrl(@RequestParam String originalUrl) {
        return urlService.getShortUrlFromOriginal(originalUrl);
    }
}