package org.mhh.mapper;

import org.mapstruct.Mapper;
import org.mhh.domain.Url;
import org.mhh.dto.UrlDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UrlMapper {
    UrlDTO UrlsToUrlDTO(Url url);

    Url UrlDTOToUrls(UrlDTO urlDTO);

    List<UrlDTO> urlsToUrlDTOs(List<Url> urls);

    List<Url> urlDTOsToUrls(List<UrlDTO> urlDTOs);

}
