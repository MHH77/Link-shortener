package org.mhh.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class UpdateUrlRequest {

    @Size(max = 2048)
    private String originalUrl;

    @Future(message = "Expiration date must be in the future.")
    private ZonedDateTime expirationDate;

    private boolean clearExpiration;
}
