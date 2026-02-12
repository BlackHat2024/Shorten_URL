package com.shorten_url.repository;

import com.shorten_url.entity.ShortURL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UrlRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UrlRepository urlRepository;

    private ShortURL shortURL;

    @BeforeEach
    void setUp() {
        shortURL = new ShortURL();
        shortURL.setShortUrl("abc12345");
        shortURL.setOriginalUrl("https://example.com");
        shortURL.setCreatedAt(LocalDateTime.now());
        shortURL.setExpirationTime(LocalDateTime.now().plusMinutes(60));
        shortURL.setClickCount(0L);
        shortURL.setCreatedBy("user@example.com");

        entityManager.persistAndFlush(shortURL);
        entityManager.clear(); // optional: ensures repo reads from DB
    }

    @Test
    void findByShortUrl_ShouldReturnShortURL() {
        Optional<ShortURL> found = urlRepository.findByShortUrl("abc12345");

        assertTrue(found.isPresent());
        assertEquals("https://example.com", found.get().getOriginalUrl());
    }

    @Test
    void findByOriginalUrlAndNotExpired_ShouldReturnShortURL() {
        Optional<ShortURL> found = urlRepository.findByOriginalUrlAndNotExpired(
                "https://example.com",
                LocalDateTime.now()
        );

        assertTrue(found.isPresent());
        assertEquals("abc12345", found.get().getShortUrl());
    }

    @Test
    void existsByShortUrl_ShouldReturnTrue() {
        assertTrue(urlRepository.existsByShortUrl("abc12345"));
    }

    @Test
    void deleteExpiredUrls_ShouldDeleteExpired() {
        // make existing row expired
        ShortURL managed = entityManager.find(ShortURL.class, shortURL.getId());
        managed.setExpirationTime(LocalDateTime.now().minusMinutes(1));
        entityManager.flush();

        int deleted = urlRepository.deleteExpiredUrls(LocalDateTime.now());

        assertTrue(deleted > 0);
        assertFalse(urlRepository.existsByShortUrl("abc12345"));
    }
}
