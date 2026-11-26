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

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:link-api", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class LinkApiTest {
    @Autowired MockMvc api;
    @Autowired UrlRepository links;
    private static final String ROOT = "/api/v1/urls";
    private static final String TOKEN = "X-Management-Token";

    @Test void creatorControlsAliasAndPublicReadsDoNotRevealToken() throws Exception {
        String destination = "https://example.org/guide";
        String token = api.perform(post(ROOT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"" + destination + "\",\"alias\":\"guide\"}"))
                .andExpect(status().isCreated()).andExpect(header().exists(TOKEN))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:7070/@guide"))
                .andReturn().getResponse().getHeader(TOKEN);
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
