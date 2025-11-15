package org.mhh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.mhh.service.UrlService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @auther:MHEsfandiari
 */

@RestController
@AllArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to Original URL", description = "Redirects a short code to its original long URL and tracks the click.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirecting to the original URL"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public void redirectToOriginalUrl(
            @Parameter(description = "The short code to look up", required = true)
            @PathVariable String shortCode,
            HttpServletResponse httpServletResponse) throws IOException {

        String originalUrl = urlService.getOriginalUrlAndTrackClick(shortCode);

        httpServletResponse.sendRedirect(originalUrl);
    }
}