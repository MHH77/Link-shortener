package org.mhh.service;

import org.mhh.domain.Urls;
import org.mhh.dto.UrlDTO;

public interface UrlService {
    Urls createUrl(UrlDTO urls);
}
