package org.mhh.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * @auther:MHEsfandiari
 */

@Data
public class CreateUrlRequest {

    @NotBlank(message = "Original URL cannot be empty.")
    @URL(message = "A valid URL format is required.")
    private String originalUrl;

}