package org.mhh.dto;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * @auther:MHEsfandiari
 */

@Data
public class UrlResponse {
    private String originalUrl;
    private String shortUrl;
    private ZonedDateTime expirationDate;
    private int clickCount;
}