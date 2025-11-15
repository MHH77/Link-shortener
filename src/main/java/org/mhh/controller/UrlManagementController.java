package org.mhh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mhh.domain.Url;
import org.mhh.dto.CreateUrlRequest;
import org.mhh.dto.UrlResponse;
import org.mhh.mapper.UrlMapper;
import org.mhh.service.UrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Tag(name = "URL Management", description = "API for creating and managing short URLs")
public class UrlManagementController {

    private final UrlService urlService;
    private final UrlMapper urlMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping
    @Operation(summary = "Create a short URL", description = "Creates a new short URL for the given original URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Short URL created successfully",
                    content = @Content(schema = @Schema(implementation = UrlResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UrlResponse> createShortUrl(
            @Parameter(description = "The original URL to be shortened", required = true)
            @Valid @RequestBody CreateUrlRequest request) {

        Url createdUrl = urlService.createUrl(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildUrlResponse(createdUrl));
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get details for a short URL", description = "Retrieves details for a given short code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Details found successfully",
                    content = @Content(schema = @Schema(implementation = UrlResponse.class))),
            @ApiResponse(responseCode = "404", description = "Short code not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UrlResponse> getUrlDetails(
            @Parameter(description = "The short code to look up", required = true)
            @PathVariable String shortCode) {

        Url url = urlService.findByShortCode(shortCode);

        return ResponseEntity.ok(buildUrlResponse(url));
    }

    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Delete a short URL", description = "Deletes a URL entry by its short code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Short URL deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public ResponseEntity<Void> deleteShortUrl(
            @Parameter(description = "The short code to delete", required = true)
            @PathVariable String shortCode) {

        urlService.deleteByShortCode(shortCode);

        return ResponseEntity.noContent().build();
    }


    private UrlResponse buildUrlResponse(Url url) {
        UrlResponse response = urlMapper.urlToUrlResponse(url);
        response.setShortUrl(baseUrl + "/" + url.getShortCode());
        return response;
    }
}