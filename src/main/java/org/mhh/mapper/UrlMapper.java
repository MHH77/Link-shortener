package org.mhh.mapper;

import org.mapstruct.Mapper;
import org.mhh.domain.Urls;
import org.mhh.domain.dto.UrlDTO;

@Mapper(componentModel = "spring")
public interface UrlMapper {
    UrlDTO UrlsToUrlDTO(Urls url);
    Urls UrlDTOToUrls(UrlDTO urlDTO);
}
