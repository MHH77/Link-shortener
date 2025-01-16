package org.mhh.service;

import org.mhh.domain.Urls;
import org.mhh.domain.dto.UrlDTO;

public interface UrlService {
    Urls createUrl(UrlDTO urls);
}
