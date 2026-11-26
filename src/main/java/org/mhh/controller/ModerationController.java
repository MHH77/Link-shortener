package org.mhh.controller;

import org.mhh.service.ModerationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ModerationController {
    private final ModerationService moderation;
    private final String adminToken;

    public ModerationController(ModerationService moderation, @Value("${app.moderation-token:}") String adminToken) {
        this.moderation = moderation;
        this.adminToken = adminToken;
    }

    @PostMapping("/urls/{code}/reports")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void report(@PathVariable String code) {
        moderation.report(code);
    }

    @GetMapping("/moderation/reports")
    public List<String> pending(@RequestHeader(value = "X-Moderator-Token", required = false) String token,
                                @RequestParam(defaultValue = "0") int page) {
        authorize(token);
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page");
        return moderation.pending(page);
    }

    @PutMapping("/moderation/links/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void review(@RequestHeader(value = "X-Moderator-Token", required = false) String token,
                       @PathVariable String code, @RequestParam boolean disabled) {
        authorize(token);
        moderation.review(code, disabled);
    }

    private void authorize(String token) {
        if (adminToken.isBlank() || token == null || !MessageDigest.isEqual(
                adminToken.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
