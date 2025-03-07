package org.mhh.controller;

import lombok.AllArgsConstructor;
import org.mhh.domain.Url;
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
    public ResponseEntity<Url> createUrl(@RequestBody UrlDTO urlDTO) {
        Url createdUrl = urlService.createUrl(urlDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUrl);
    }

    @GetMapping("/all")
    public List<Url> getAllUrls() {
        return urlService.getUrls();
    }

    @GetMapping("/short")
    public ResponseEntity<String> getOriginalUrlFromShortUrl(@RequestParam String originalUrl) {
        return ResponseEntity.ok(urlService.getOriginalUrlFromShortUrl (originalUrl));
    }
    @DeleteMapping("/{shortUrl}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortUrl) {
        urlService.deleteShortUrl(shortUrl);
        return ResponseEntity.noContent().build();
    }
}