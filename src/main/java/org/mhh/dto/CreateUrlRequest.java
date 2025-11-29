package org.mhh.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.ZonedDateTime;

/**
 * @auther:MHEsfandiari
 */

@Data
public class CreateUrlRequest {

    @NotBlank(message = "Original URL cannot be empty.")
    @URL(message = "A valid URL format is required.")
    private String originalUrl;

    @Future(message = "Expiration date must be in the future.")
    private ZonedDateTime expirationDate;

}