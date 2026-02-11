package com.shorten_url.service;

import com.shorten_url.dto.OriginalUrl;
import com.shorten_url.dto.ShortUrlRequest;
import com.shorten_url.dto.ShortUrlResponse;
import com.shorten_url.dto.UpdateExpirationRequest;
import com.shorten_url.entity.ShortURL;
import com.shorten_url.exception.UrlNotFoundException;
import com.shorten_url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    @Value( "${url-shortener.default-expiration-minutes}")
    private int defaultExpirationMinutes;

    @Value("${url-shortener.short-url-length}")
    private int shortCodeLength;

    @Value("${url-shortener.base-url}")
    private String baseUrl;

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public ShortUrlResponse shorten(ShortUrlRequest request, String createdBy) {
        LocalDateTime now = LocalDateTime.now();

        int expirationMinutes = (request.getExpirationMinutes() != null)
                ? request.getExpirationMinutes()
                : defaultExpirationMinutes;

        LocalDateTime newExpiration = now.plusMinutes(expirationMinutes);

        ShortURL existing = urlRepository
                .findByOriginalUrlAndNotExpired(request.getUrl(), now)
                .orElse(null);

        if (existing != null) {
            existing.resetExpiration(newExpiration);
            if (createdBy != null && !createdBy.isBlank()) {
                existing.setCreatedBy(createdBy);
            }
            return toResponse(existing);
        }

        String code = generateUniqueCode();

        ShortURL entity = new ShortURL();
        entity.setShortUrl(code);
        entity.setOriginalUrl(request.getUrl());
        entity.setCreatedAt(now);
        entity.setExpirationTime(newExpiration);
        entity.setCreatedBy(createdBy);

        ShortURL saved = urlRepository.save(entity);
        return toResponse(saved);
    }


    @Transactional
    public ShortUrlResponse resolveByCode(String shortCode) {
        LocalDateTime now = LocalDateTime.now();

        ShortURL entity = urlRepository.findByShortUrl(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode, false));

        if (!entity.getExpirationTime().isAfter(now)) {
            urlRepository.delete(entity);
            throw new UrlNotFoundException(shortCode, true);
        }

        entity.incrementClickCount();
        return toResponse(entity);
    }

    @Transactional
    public OriginalUrl getOriginalUrl(String shortUrlFull) {
        ShortURL entity = getValidEntityByShortUrlFull(shortUrlFull);
        return toUrl(entity);
    }

    @Transactional
    public ShortUrlResponse overwriteExpirationByShortUrl(String shortUrlFull, UpdateExpirationRequest request) {
        ShortURL entity = getValidEntityByShortUrlFull(shortUrlFull);
        entity.resetExpiration(LocalDateTime.now().plusMinutes(request.getExpirationMinutes()));
        return toResponse(entity);
    }

    @Transactional
    public void deleteExpiredNow() {
        urlRepository.deleteExpiredUrls(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> getAllShortenedUrls() {
        return urlRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ShortURL getValidEntityByShortUrlFull(String shortUrlFull) {
        String code = extractCode(shortUrlFull);
        LocalDateTime now = LocalDateTime.now();

        ShortURL entity = urlRepository.findByShortUrl(code)
                .orElseThrow(() -> new UrlNotFoundException(code, false));

        if (!entity.getExpirationTime().isAfter(now)) {
            urlRepository.delete(entity);
            throw new UrlNotFoundException(code, true);
        }
        return entity;
    }

    private ShortUrlResponse toResponse(ShortURL s) {
        return new ShortUrlResponse()
                .setShortUrl(buildFullShortUrl(s.getShortUrl()))
                .setOriginalUrl(s.getOriginalUrl())
                .setCreatedAt(s.getCreatedAt())
                .setExpirationTime(s.getExpirationTime())
                .setClickCount(s.getClickCount())
                .setCreatedBy(s.getCreatedBy());
    }
    private OriginalUrl toUrl(ShortURL s) {
        return new OriginalUrl().setOriginalUrl(s.getOriginalUrl());
    }

    private String buildFullShortUrl(String code) {
        String b = baseUrl == null ? "" : baseUrl.trim();
        if (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        return b + "/" + code;
    }

    private String extractCode(String shortUrlOrCode) {
        if (shortUrlOrCode == null || shortUrlOrCode.trim().isEmpty()) {
            throw new IllegalArgumentException("shortUrl cannot be blank");
        }
        String s = shortUrlOrCode.trim();

        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);

        int idx = s.lastIndexOf('/');
        return (idx >= 0) ? s.substring(idx + 1) : s;
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 25; attempt++) {
            String code = randomCode(shortCodeLength);
            if (!urlRepository.existsByShortUrl(code)) return code;
        }
        throw new IllegalStateException("Failed to generate unique short code after multiple attempts");
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
