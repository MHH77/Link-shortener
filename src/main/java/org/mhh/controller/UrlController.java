package org.mhh.controller;

import lombok.AllArgsConstructor;
import org.mhh.domain.Urls;
import org.mhh.dto.UrlDTO;
import org.mhh.service.UrlService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/url")
public class UrlController {

    private UrlService urlService;

    @PostMapping
    public Urls createUser(@RequestBody UrlDTO urlDTO) {
        return urlService.createUrl(urlDTO);
    }
}
