package org.mhh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.mhh.domain.Url;
import org.mhh.dto.UrlDTO;
import org.mhh.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/url")
@Tag(name = "URL", description = "Operations related to URL shortening and management") // API Tag
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/create")
    @Operation(summary = "Create a short URL", description = "Creates a new short URL for the given original URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Short URL created successfully",
                    content = @Content(schema = @Schema(implementation = Url.class))),
            @ApiResponse(responseCode = "400", description = "Invalid URL format or missing original URL",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))), // Assuming you have an ErrorResponse class
            @ApiResponse(responseCode = "409", description = "Original URL already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Url> createUrl(
            @Parameter(description = "The original (long) URL to be shortened", required = true)
            @Valid @RequestBody UrlDTO urlDTO) {  // Added @Valid for DTO validation
        Url createdUrl = urlService.createUrl(urlDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUrl);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all URLs", description = "Retrieves a list of all shortened URLs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved URLs",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<Url>> getAllUrls() { // Changed to ResponseEntity<List<Url>>
        List<Url> urls = urlService.getUrls();
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/short")
    @Operation(summary = "Get original URL by short URL", description = "Retrieves the original URL corresponding to the given short URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Original URL found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Short URL not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> getOriginalUrlFromShortUrl(
            @Parameter(description = "The short URL to lookup", required = true)
            @RequestParam("originalUrl") String shortUrl) { // Corrected parameter name, added description
        String originalUrl = urlService.getOriginalUrlFromShortUrl(shortUrl); // Pass shortUrl, not originalUrl
        return ResponseEntity.ok(originalUrl);
    }

    @DeleteMapping("/{shortUrl}")
    @Operation(summary = "Delete a short URL", description = "Deletes the shortened URL associated with the given short URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Short URL deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Short URL not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteShortUrl(
            @Parameter(description = "The short URL to delete", required = true)
            @PathVariable String shortUrl) {
        urlService.deleteShortUrl(shortUrl);
        return ResponseEntity.noContent().build();
    }
}