package org.mhh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mhh.domain.Url;
import org.mhh.dto.UrlResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UrlMapper {

    @Mapping(target = "shortUrl", ignore = true)
    UrlResponse urlToUrlResponse(Url url);

    List<UrlResponse> urlsToUrlResponses(List<Url> urls);
}