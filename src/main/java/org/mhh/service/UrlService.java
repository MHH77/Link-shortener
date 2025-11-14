package org.mhh.service;

import org.mhh.domain.Url;
import org.mhh.dto.CreateUrlRequest;


public interface UrlService {

    Url createUrl(CreateUrlRequest request);

    Url findByShortCode(String shortCode);

    String getOriginalUrlAndTrackClick(String shortCode);

    void deleteByShortCode(String shortCode);
}