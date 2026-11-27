package org.mhh.service;

import org.mhh.domain.Url;
import org.mhh.dto.CreateUrlRequest;
import org.mhh.dto.UpdateUrlRequest;


public interface UrlService {

    record CreatedLink(Url url, String managementToken) {}

    CreatedLink createUrl(CreateUrlRequest request);

    Url findByShortCode(String shortCode);

    String getOriginalUrlAndTrackClick(String shortCode);

    String previewDestination(String shortCode);

    Url updateByShortCode(String shortCode, String managementToken, UpdateUrlRequest request);

    void deleteByShortCode(String shortCode, String managementToken);
}
