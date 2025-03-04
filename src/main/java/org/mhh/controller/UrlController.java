package org.mhh.controller;

import lombok.AllArgsConstructor;
import org.mhh.domain.Urls;
import org.mhh.dto.UrlDTO;
import org.mhh.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/url")
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/create")
    public ResponseEntity<Urls> createUrl(@RequestBody UrlDTO urlDTO) {
        Urls createdUrl = urlService.createUrl(urlDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUrl);
    }

    @GetMapping("/all")
    public List<Urls> getAllUrls() {
        return urlService.getUrls();
    }

    @GetMapping("/short")
    public ResponseEntity<String> getShortUrl(@RequestParam String originalUrl) {
        return ResponseEntity.ok(urlService.getShortUrlFromOriginal(originalUrl));
    }

}