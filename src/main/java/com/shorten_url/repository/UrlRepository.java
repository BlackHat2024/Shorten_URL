package com.shorten_url.repository;

import com.shorten_url.entity.ShortURL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<ShortURL, Long> {

    Optional<ShortURL> findByShortUrl(String shortUrl);

    Optional<ShortURL> findByOriginalUrl(String originalUrl);



}
