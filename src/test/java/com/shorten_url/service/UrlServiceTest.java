package com.shorten_url.service;

import com.shorten_url.dto.OriginalUrl;
import com.shorten_url.dto.ShortUrlRequest;
import com.shorten_url.dto.ShortUrlResponse;
import com.shorten_url.dto.UpdateExpirationRequest;
import com.shorten_url.entity.ShortURL;
import com.shorten_url.exception.UrlNotFoundException;
import com.shorten_url.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    UrlRepository urlRepository;

    @InjectMocks
    UrlService urlService;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(urlService, "defaultExpirationMinutes", 60);
        ReflectionTestUtils.setField(urlService, "shortCodeLength", 8);
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080/short");
    }

    @Test
    void shorten_createsNewShortUrl_whenNoActiveExisting() {

        ShortUrlRequest req = new ShortUrlRequest();
        req.setUrl("https://example.com");
        req.setExpirationMinutes(null);

        when(urlRepository.findByOriginalUrlAndNotExpired(eq(req.getUrl()), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(urlRepository.existsByShortUrl(anyString())).thenReturn(false);
        when(urlRepository.save(any(ShortURL.class))).thenAnswer(inv -> inv.getArgument(0));


        ShortUrlResponse res = urlService.shorten(req, "user@test.com");
        verify(urlRepository).deleteByOriginalUrlAndExpirationTimeBefore(eq(req.getUrl()), any(LocalDateTime.class));
        verify(urlRepository).save(any(ShortURL.class));

        assertThat(res.getShortUrl()).startsWith("http://localhost:8080/short/");
        assertThat(res.getShortUrl()).hasSize("http://localhost:8080/short/".length() + 8);
        assertThat(res.getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(res.getCreatedBy()).isEqualTo("user@test.com");
        assertThat(res.getCreatedAt()).isNotNull();
        assertThat(res.getExpirationTime()).isAfter(res.getCreatedAt());
        assertThat(res.getClickCount()).isEqualTo(0L);
    }

    @Test
    void shorten_reusesExisting_whenNotExpired_resetsExpiration_updatesCreatedBy() {
        ShortUrlRequest req = new ShortUrlRequest();
        req.setUrl("https://example.com");
        req.setExpirationMinutes(15);

        ShortURL existing = new ShortURL();
        existing.setShortUrl("AbCd1234");
        existing.setOriginalUrl(req.getUrl());
        existing.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        existing.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        existing.setCreatedBy("old@test.com");
        existing.setClickCount(10L);

        when(urlRepository.findByOriginalUrlAndNotExpired(eq(req.getUrl()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existing));
        when(urlRepository.save(any(ShortURL.class))).thenAnswer(inv -> inv.getArgument(0));

        ShortUrlResponse res = urlService.shorten(req, "new@test.com");

        verify(urlRepository).deleteByOriginalUrlAndExpirationTimeBefore(eq(req.getUrl()), any(LocalDateTime.class));
        verify(urlRepository).save(existing);

        assertThat(res.getShortUrl()).isEqualTo("http://localhost:8080/short/AbCd1234");
        assertThat(res.getCreatedBy()).isEqualTo("new@test.com");
        assertThat(res.getExpirationTime()).isAfter(LocalDateTime.now());
        assertThat(res.getClickCount()).isEqualTo(10L);
    }

    @Test
    void shorten_throws_whenCannotGenerateUniqueCode_afterManyAttempts() {
        ShortUrlRequest req = new ShortUrlRequest();
        req.setUrl("https://example.com");
        req.setExpirationMinutes(10);

        when(urlRepository.findByOriginalUrlAndNotExpired(eq(req.getUrl()), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        when(urlRepository.existsByShortUrl(anyString())).thenReturn(true);

        assertThatThrownBy(() -> urlService.shorten(req, "user@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to generate unique short code");

        verify(urlRepository).deleteByOriginalUrlAndExpirationTimeBefore(eq(req.getUrl()), any(LocalDateTime.class));
        verify(urlRepository, never()).save(any());
    }

    @Test
    void resolveByCode_incrementsClickCount_andReturnsResponse() {
        ShortURL entity = new ShortURL();
        entity.setShortUrl("AbCd1234");
        entity.setOriginalUrl("https://example.com");
        entity.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        entity.setExpirationTime(LocalDateTime.now().plusHours(1));
        entity.setClickCount(0L);

        when(urlRepository.findByShortUrl("AbCd1234")).thenReturn(Optional.of(entity));

        ShortUrlResponse res = urlService.resolveByCode("AbCd1234");

        assertThat(res.getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(res.getClickCount()).isEqualTo(1L);
    }

    @Test
    void getOriginalUrl_acceptsFullShortUrl_withTrailingSlash_extractsCode() {
        ShortURL entity = new ShortURL();
        entity.setShortUrl("AbCd1234");
        entity.setOriginalUrl("https://example.com");
        entity.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        entity.setExpirationTime(LocalDateTime.now().plusHours(1));
        entity.setClickCount(0L);

        when(urlRepository.findByShortUrl("AbCd1234")).thenReturn(Optional.of(entity));

        OriginalUrl res = urlService.getOriginalUrl("http://localhost:8080/short/AbCd1234/");

        assertThat(res.getOriginalUrl()).isEqualTo("https://example.com");
    }

    @Test
    void overwriteExpiration_updatesExpiration_andReturnsResponse() {
        ShortURL entity = new ShortURL();
        entity.setShortUrl("AbCd1234");
        entity.setOriginalUrl("https://example.com");
        entity.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        entity.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        entity.setClickCount(0L);

        when(urlRepository.findByShortUrl("AbCd1234")).thenReturn(Optional.of(entity));

        UpdateExpirationRequest req = new UpdateExpirationRequest();
        req.setExpirationMinutes(30);

        ShortUrlResponse res = urlService.overwriteExpirationByShortUrl("http://x/AbCd1234", req);

        assertThat(res.getExpirationTime()).isAfter(LocalDateTime.now().plusMinutes(25));
    }

    @Test
    void expiredUrl_isDeleted_andThrowsUrlNotFoundException() {
        ShortURL entity = new ShortURL();
        entity.setShortUrl("AbCd1234");
        entity.setOriginalUrl("https://example.com");
        entity.setCreatedAt(LocalDateTime.now().minusHours(2));
        entity.setExpirationTime(LocalDateTime.now().minusSeconds(1));
        entity.setClickCount(0L);

        when(urlRepository.findByShortUrl("AbCd1234")).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> urlService.getOriginalUrl("AbCd1234"))
                .isInstanceOf(UrlNotFoundException.class);

        verify(urlRepository).delete(entity);
    }

    @Test
    void deleteExpiredNow_callsRepository() {
        urlService.deleteExpiredNow();
        verify(urlRepository).deleteExpiredUrls(any(LocalDateTime.class));
    }

    @Test
    void getAllShortenedUrls_mapsEntitiesToResponses() {
        ShortURL a = new ShortURL();
        a.setShortUrl("AbCd1234");
        a.setOriginalUrl("https://a.com");
        a.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        a.setExpirationTime(LocalDateTime.now().plusHours(1));
        a.setClickCount(1L);
        a.setCreatedBy("a@test.com");

        ShortURL b = new ShortURL();
        b.setShortUrl("ZyXw9876");
        b.setOriginalUrl("https://b.com");
        b.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        b.setExpirationTime(LocalDateTime.now().plusHours(2));
        b.setClickCount(2L);
        b.setCreatedBy("b@test.com");

        when(urlRepository.findAll()).thenReturn(List.of(a, b));

        List<ShortUrlResponse> res = urlService.getAllShortenedUrls();

        assertThat(res).hasSize(2);
        assertThat(res.get(0).getShortUrl()).isEqualTo("http://localhost:8080/short/AbCd1234");
        assertThat(res.get(1).getShortUrl()).isEqualTo("http://localhost:8080/short/ZyXw9876");
    }
}
