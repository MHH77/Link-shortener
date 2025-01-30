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

    private UrlService urlService;

    @PostMapping
    public Urls createUrls(@RequestBody UrlDTO urlDTO) {
        return urlService.createUrl(urlDTO);
    }

    @GetMapping
    public List<Urls> createUser() {
        return urlService.getUrls();
    }

}
