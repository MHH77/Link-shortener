package org.mhh.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

/**
 * @auther:MHEsfandiari
 */

@Data
public class CreateUrlRequest {

    @NotBlank(message = "Original URL cannot be empty.")
    @Size(max = 2048)
    private String originalUrl;

    @Pattern(regexp = "[a-z0-9][a-z0-9-]{2,39}", message = "Use 3 to 40 lowercase letters, digits or hyphens, starting with a letter or digit.")
    private String alias;

    @Future(message = "Expiration date must be in the future.")
    private ZonedDateTime expirationDate;

}
