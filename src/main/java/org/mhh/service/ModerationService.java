package org.mhh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mhh.domain.ModerationState;
import org.mhh.domain.Url;
import org.mhh.exception.UrlNotFoundException;
import org.mhh.repository.UrlRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {
    private final UrlRepository links;

    @Transactional
    public void report(String code) {
        Url link = locked(code);
        if (link.getModerationState() == null || link.getModerationState() == ModerationState.ACTIVE) {
            link.setModerationState(ModerationState.REPORTED);
            log.info("event=link_reported linkId={}", link.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<String> pending(int page) {
        return links.reportedCodes(PageRequest.of(page, 50));
    }

    @Transactional
    public void review(String code, boolean disabled) {
        Url link = locked(code);
        ModerationState next = disabled ? ModerationState.DISABLED : ModerationState.ACTIVE;
        if (link.getModerationState() != next) {
            link.setModerationState(next);
            log.info("event=link_reviewed linkId={} state={}", link.getId(), next);
        }
    }

    private Url locked(String code) {
        return links.lockByShortCode(code).orElseThrow(() -> new UrlNotFoundException("Link not found"));
    }
}
