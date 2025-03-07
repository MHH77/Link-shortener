package org.mhh.service;

import org.mhh.domain.Url;
import org.mhh.dto.UrlDTO;

import java.util.List;

public interface UrlService {
    Url createUrl(UrlDTO urls);

    List<Url> getUrls();

    String getOriginalUrlFromShortUrl (String shortUrl);

    void deleteShortUrl(String shortUrl);

}
