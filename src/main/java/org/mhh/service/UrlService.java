package org.mhh.service;

import org.mhh.domain.Urls;
import org.mhh.dto.UrlDTO;

import java.util.List;

public interface UrlService {
    Urls createUrl(UrlDTO urls);
    List<Urls> getUrls();
}
