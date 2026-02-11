package com.shorten_url.repository;

import com.shorten_url.entity.ShortURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<ShortURL, Long> {

    Optional<ShortURL> findByShortUrl(String shortUrl);

    @Query("SELECT s FROM ShortURL s WHERE s.originalUrl = :originalUrl AND s.expirationTime > :now")
    Optional<ShortURL> findByOriginalUrlAndNotExpired(
            @Param("originalUrl") String originalUrl,
            @Param("now") LocalDateTime now);

    boolean existsByShortUrl(String shortUrl);


}
