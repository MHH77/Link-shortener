package org.mhh.mapper;

import org.mapstruct.Mapper;
import org.mhh.domain.Urls;
import org.mhh.dto.UrlDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UrlMapper {
    UrlDTO UrlsToUrlDTO(Urls url);

    Urls UrlDTOToUrls(UrlDTO urlDTO);

    List<UrlDTO> urlsToUrlDTOs(List<Urls> urls);

    List<Urls> urlDTOsToUrls(List<UrlDTO> urlDTOs);

}
