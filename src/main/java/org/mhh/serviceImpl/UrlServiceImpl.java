package org.mhh.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mhh.dto.CreateUrlRequest;
import org.mhh.exception.DuplicateUrlException;
import org.mhh.exception.LinkExpiredException;
import org.mhh.exception.UrlNotFoundException;
import org.mhh.service.UrlService;
import org.mhh.service.ClickTrackingService;
import org.mhh.domain.Url;
import org.mhh.repository.UrlRepository;
import org.mhh.util.Base62Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final ClickTrackingService clickTrackingService;

    @Value("${url-shortener.id-offset}")
    private long idOffset;

    @Override
    @Transactional
    public CreatedLink createUrl(CreateUrlRequest request) {
        validateDestination(request.getOriginalUrl());
        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(request.getOriginalUrl());

        if (existingUrl.isPresent()) {
            throw new DuplicateUrlException(
                    "The URL '" + request.getOriginalUrl() + "' has already been shortened. The short code is: " + existingUrl.get().getShortCode()
            );
        }

        Url newUrl = new Url();
        String token = UUID.randomUUID().toString();
        newUrl.setManagementTokenHash(hashToken(token));
        if (request.getAlias() != null) {
            newUrl.setShortCode("@" + request.getAlias());
        }
        newUrl.setOriginalUrl(request.getOriginalUrl());
        newUrl.setExpirationDate(request.getExpirationDate());
        newUrl.setClickCount(0);

        try {
            Url savedUrl = urlRepository.saveAndFlush(newUrl);
            if (savedUrl.getShortCode() == null) {
                savedUrl.setShortCode(Base62Encoder.encode(savedUrl.getId() + idOffset));
            }
            log.info("event=link_created linkId={} customAlias={}", savedUrl.getId(), request.getAlias() != null);
            return new CreatedLink(savedUrl, token);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMostSpecificCause() instanceof java.sql.SQLException sql
                    && "23505".equals(sql.getSQLState())) {
                throw new DuplicateUrlException("That destination or alias is already in use.");
            }
            throw ex;
        }
    }

    private static void validateDestination(String value) {
        try {
            URI uri = URI.create(value);
            if (("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getRawUserInfo() == null && uri.getPort() <= 65535) {
                return;
            }
        } catch (IllegalArgumentException ignored) {
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use an absolute HTTP or HTTPS URL without credentials.");
    }

    private static String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
    @Override
    public Url findByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));
    }

    @Override
    @Transactional
    public String getOriginalUrlAndTrackClick(String shortCode) {
        Url url = findByShortCode(shortCode);

        if (url.getModerationState() == org.mhh.domain.ModerationState.DISABLED) {
            throw new ResponseStatusException(HttpStatus.GONE, "Link disabled");
        }
        if (url.getExpirationDate() != null && url.getExpirationDate().isBefore(ZonedDateTime.now())) {
            log.debug("event=redirect_expired linkId={}", url.getId());
            throw new LinkExpiredException("This link has expired on " + url.getExpirationDate());
        }

        clickTrackingService.trackClick(url.getId());

        return url.getOriginalUrl();
    }

    @Override
    @Transactional
    public void deleteByShortCode(String shortCode, String token) {
        Url url = findByShortCode(shortCode);
        if (token == null || !hashToken(token).equals(url.getManagementTokenHash())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A valid management token is required.");
        }
        urlRepository.delete(url);
        log.info("event=link_deleted linkId={}", url.getId());
    }
}
