package com.shorten_url.controller;

import com.shorten_url.dto.OriginalUrl;
import com.shorten_url.dto.ShortUrlRequest;
import com.shorten_url.dto.ShortUrlResponse;
import com.shorten_url.dto.UpdateExpirationRequest;
import com.shorten_url.entity.Jwt;
import com.shorten_url.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/urls")
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortUrlResponse> shorten(
            @Valid @RequestBody ShortUrlRequest request,
            Authentication authentication) {

        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(urlService.shorten(request, email));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ShortUrlResponse>> getAll() {
        return ResponseEntity.ok(urlService.getAllShortenedUrls());
    }

    @GetMapping("/original")
    public ResponseEntity<OriginalUrl> getOriginalUrl(@RequestParam String shortUrl) {
        OriginalUrl originalUrl = urlService.getOriginalUrl(shortUrl);
        return ResponseEntity.ok(originalUrl);
    }

    @PutMapping("/expiration")
    public ResponseEntity<ShortUrlResponse> overwriteExpiration(
            @RequestParam String shortUrl,
            @Valid @RequestBody UpdateExpirationRequest request
    ) {
        ShortUrlResponse response = urlService.overwriteExpirationByShortUrl(shortUrl, request);
        return ResponseEntity.ok(response);
    }

}
