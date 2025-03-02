package org.mhh.repository;

import org.mhh.domain.Urls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Urls,Long> {
    Optional<Urls> findByShortUrl(String shortUrl);
    boolean existsByShortUrl(String shortUrl);
    Optional<Urls> findByOriginalUrl(String originalUrl);
}
