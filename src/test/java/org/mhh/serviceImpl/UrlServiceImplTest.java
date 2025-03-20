package org.mhh.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mhh.dto.UrlDTO;
import org.mhh.exception.UrlNotFoundException;
import org.mhh.mapper.UrlMapper;
import org.mhh.domain.Url;
import org.mhh.repository.UrlRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlMapper urlMapper;

    @InjectMocks
    private UrlServiceImpl urlService;

    private UrlDTO urlDTO;
    private Url url;

    @BeforeEach
    void setUp() {
        urlDTO = new UrlDTO();
        urlDTO.setOriginalUrl("https://www.example.com");

        url = new Url();
        url.setOriginalUrl("https://www.example.com");
        url.setShortUrl("http://abcdef");
    }

    @Test
    void createUrl_whenOriginalUrlDoesNotExist_shouldCreateAndSaveNewUrl() {
        // Arrange
        when(urlRepository.findByOriginalUrl(urlDTO.getOriginalUrl())).thenReturn(Optional.empty());
        when(urlMapper.UrlDTOToUrls(urlDTO)).thenReturn(url);
        when(urlRepository.save(any(Url.class))).thenReturn(url);

        // Act
        Url createdUrl = urlService.createUrl(urlDTO);

        // Assert
        assertNotNull(createdUrl);
        assertEquals(url.getOriginalUrl(), createdUrl.getOriginalUrl());
        assertNotNull(createdUrl.getShortUrl()); // Short URL should be generated
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    void createUrl_whenOriginalUrlExists_shouldReturnExistingUrl() {
        // Arrange
        when(urlRepository.findByOriginalUrl(urlDTO.getOriginalUrl())).thenReturn(Optional.of(url));

        // Act
        Url existingUrl = urlService.createUrl(urlDTO); // Type declaration added here

        // Assert
        assertNotNull(existingUrl);
        assertEquals(url.getOriginalUrl(), existingUrl.getOriginalUrl());
        verify(urlRepository, never()).save(any(Url.class)); // Ensure save is not called
    }

    @Test
    void getOriginalUrlFromShortUrl_whenShortUrlExists_shouldReturnOriginalUrl() {
        // Arrange
        String shortUrl = "http://abcdef";
        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(url));

        // Act
        String originalUrl = urlService.getOriginalUrlFromShortUrl(shortUrl);

        // Assert
        assertEquals(url.getOriginalUrl(), originalUrl);
    }

    @Test
    void getOriginalUrlFromShortUrl_whenShortUrlDoesNotExist_shouldThrowUrlNotFoundException() {
        // Arrange
        String shortUrl = "http://invalid";
        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UrlNotFoundException.class, () -> urlService.getOriginalUrlFromShortUrl(shortUrl));
    }
    @Test
    void deleteShortUrl_whenShortUrlExists_shouldDeleteUrl() {
        // Arrange
        String shortUrl = "http://abcdef";
        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(url));

        // Act
        urlService.deleteShortUrl(shortUrl);

        // Assert
        verify(urlRepository, times(1)).delete(url);
    }

    @Test
    void deleteShortUrl_whenShortUrlDoesNotExist_shouldThrowUrlNotFoundException() {
        // Arrange
        String shortUrl = "http://invalid"; // Define and initialize shortUrl here
        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UrlNotFoundException.class, () -> urlService.deleteShortUrl(shortUrl));
    }
}