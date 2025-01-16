package org.mhh.repository;

import org.mhh.domain.Urls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<Urls,Long> {
}
