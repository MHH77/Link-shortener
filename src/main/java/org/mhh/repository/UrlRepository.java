package org.mhh.repository;

import org.mhh.domain.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    @Modifying
    @Query("update Url link set link.clickCount = link.clickCount + 1 where link.id = ?1")
    int incrementClickCount(Long id);

    Optional<Url> findByShortCode(String shortCode);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select link from Url link where link.shortCode = ?1")
    Optional<Url> lockByShortCode(String code);

    @Query("select link.shortCode from Url link where link.moderationState = org.mhh.domain.ModerationState.REPORTED order by link.id")
    java.util.List<String> reportedCodes(org.springframework.data.domain.Pageable page);
    boolean existsByShortCode(String shortCode);
    Optional<Url> findByOriginalUrl(String originalUrl);
}
