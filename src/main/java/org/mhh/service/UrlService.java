package org.mhh.service;

import org.mhh.domain.Url;
import org.mhh.dto.CreateUrlRequest;


public interface UrlService {

    record CreatedLink(Url url, String managementToken) {}

    CreatedLink createUrl(CreateUrlRequest request);

    Url findByShortCode(String shortCode);

    String getOriginalUrlAndTrackClick(String shortCode);

    void deleteByShortCode(String shortCode, String managementToken);
}
