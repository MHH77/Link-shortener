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
    boolean existsByShortCode(String shortCode);
    Optional<Url> findByOriginalUrl(String originalUrl);
}
