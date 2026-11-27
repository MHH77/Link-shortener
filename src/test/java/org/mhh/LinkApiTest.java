package org.mhh;

import org.junit.jupiter.api.Test;
import org.mhh.domain.Url;
import org.mhh.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.ZonedDateTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:link-api",
        "app.moderation-token=test-moderator"})
@AutoConfigureMockMvc
class LinkApiTest {
    @Autowired MockMvc api;
    @Autowired UrlRepository links;
    private static final String ROOT = "/api/v1/urls";
    private static final String TOKEN = "X-Management-Token";

    @Test void moderatorCanDisableAndRestoreReportedLink() throws Exception {
        String path = "/api/v1/moderation/links/@review";
        String queue = "/api/v1/moderation/reports";
        String header = "X-Moderator-Token";
        api.perform(post(ROOT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"https://example.org/review\",\"alias\":\"review\"}"))
                .andExpect(status().isCreated());
        for (int repeat = 0; repeat < 2; repeat++)
            api.perform(post(ROOT + "/@review/reports")).andExpect(status().isAccepted());
        api.perform(get(queue)).andExpect(status().isForbidden());
        api.perform(get(queue).header(header, "test-moderator")).andExpect(content().json("[\"@review\"]", true));
        api.perform(put(path).param("disabled", "true")).andExpect(status().isForbidden());
        api.perform(get("/@review")).andExpect(status().isFound());
        api.perform(put(path).header(header, "test-moderator").param("disabled", "true")).andExpect(status().isNoContent());
        api.perform(get("/@review")).andExpect(status().isGone());
        api.perform(head("/@review")).andExpect(status().isGone());
        api.perform(get(ROOT + "/@review")).andExpect(jsonPath("$.clickCount").value(1));
        api.perform(get(queue).header(header, "test-moderator")).andExpect(content().json("[]", true));
        api.perform(put(path).header(header, "test-moderator").param("disabled", "false")).andExpect(status().isNoContent());
        api.perform(get("/@review")).andExpect(status().isFound());
    }

    @Test void ownerUpdatesDestinationAndExpiry() throws Exception {
        String link = ROOT + "/@edit";
        String token = api.perform(post(ROOT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"https://example.org/edit-old\",\"alias\":\"edit\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader(TOKEN);
        api.perform(post(ROOT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"https://example.org/edit-taken\"}")).andExpect(status().isCreated());
        String moved = "{\"originalUrl\":\"https://example.org/edit-new\"}";
        api.perform(patch(link).contentType(MediaType.APPLICATION_JSON).content(moved)).andExpect(status().isForbidden());
        api.perform(patch(link).header(TOKEN, "wrong").contentType(MediaType.APPLICATION_JSON).content(moved))
                .andExpect(status().isForbidden());
        api.perform(patch(link).header(TOKEN, token).contentType(MediaType.APPLICATION_JSON).content(moved))
                .andExpect(status().isOk()).andExpect(jsonPath("$.originalUrl").value("https://example.org/edit-new"))
                .andExpect(header().doesNotExist(TOKEN));
        api.perform(get("/@edit")).andExpect(redirectedUrl("https://example.org/edit-new"));
        api.perform(patch(link).header(TOKEN, token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"https://example.org/edit-taken\"}")).andExpect(status().isConflict());
        for (String body : new String[]{"{}", "{\"originalUrl\":\"ftp://example.org/file\"}",
                "{\"expirationDate\":\"2000-01-01T00:00:00Z\"}",
                "{\"expirationDate\":\"2999-01-01T00:00:00Z\",\"clearExpiration\":true}"}) {
            api.perform(patch(link).header(TOKEN, token).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        api.perform(patch(link).header(TOKEN, token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"expirationDate\":\"2999-01-01T00:00:00Z\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.expirationDate").isNotEmpty())
                .andExpect(jsonPath("$.originalUrl").value("https://example.org/edit-new"));
        api.perform(patch(link).header(TOKEN, token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"clearExpiration\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.expirationDate").isEmpty());
        api.perform(patch(ROOT + "/missing").header(TOKEN, token).contentType(MediaType.APPLICATION_JSON).content(moved))
                .andExpect(status().isNotFound());
    }

    @Test void creatorControlsAliasAndPublicReadsDoNotRevealToken() throws Exception {
        String destination = "https://example.org/guide";
        String token = api.perform(post(ROOT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"" + destination + "\",\"alias\":\"guide\"}"))
                .andExpect(status().isCreated()).andExpect(header().exists(TOKEN))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:7070/@guide"))
                .andReturn().getResponse().getHeader(TOKEN);
        api.perform(head("/@guide")).andExpect(status().isFound()).andExpect(redirectedUrl(destination))
                .andExpect(header().string("Cache-Control", "no-store")).andExpect(content().string(""));
        api.perform(get("/@guide")).andExpect(status().isFound()).andExpect(redirectedUrl(destination));
        api.perform(get(ROOT + "/@guide")).andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(destination))
                .andExpect(jsonPath("$.clickCount").value(1)).andExpect(header().doesNotExist(TOKEN));
        api.perform(post(ROOT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"https://example.org/other\",\"alias\":\"guide\"}"))
                .andExpect(status().isConflict());
        api.perform(delete(ROOT + "/@guide")).andExpect(status().isForbidden());
        api.perform(delete(ROOT + "/@guide").header(TOKEN, "wrong")).andExpect(status().isForbidden());
        api.perform(delete(ROOT + "/@guide").header(TOKEN, token)).andExpect(status().isNoContent());
        api.perform(get("/@guide")).andExpect(status().isNotFound());
    }

    @Test void rejectsInvalidInputsAndReportsExpiry() throws Exception {
        for (String body : new String[]{"{", "{\"originalUrl\":\"ftp://example.org/file\"}",
                "{\"originalUrl\":\"https://user@example.org/\"}",
                "{\"originalUrl\":\"https://example.org/\",\"alias\":\"a/b\"}"}) {
            api.perform(post(ROOT).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        Url expired = new Url();
        expired.setShortCode("past");
        expired.setOriginalUrl("https://example.org/past");
        expired.setExpirationDate(ZonedDateTime.now().minusHours(1));
        links.save(expired);
        api.perform(get("/past")).andExpect(status().isGone());
    }
}
