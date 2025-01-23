package org.mhh.serviceImpl;

import lombok.AllArgsConstructor;
import org.mhh.dto.UrlDTO;
import org.mhh.mapper.UrlMapper;
import org.mhh.service.UrlService;
import org.mhh.domain.Urls;
import org.mhh.repository.UrlRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UrlServiceImpl implements UrlService {

    private UrlRepository urlRepository;
    private final UrlMapper urlMapper;

    @Override
    public Urls createUrl(UrlDTO urls) {
        Urls urls1 = urlMapper.UrlDTOToUrls(urls);
        return urlRepository.save(urls1);
    }
}
