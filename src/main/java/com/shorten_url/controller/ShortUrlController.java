package com.shorten_url.controller;

import com.shorten_url.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ShortUrlController {

    private final UrlService urlService;

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        // Validate code format (8 alphanumeric characters)
        if (!code.matches("^[0-9A-Za-z]{8}$")) {
            return ResponseEntity.notFound().build();
        }

        try {
            String originalUrl = urlService.resolveByCode(code).getOriginalUrl();
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(URI.create(originalUrl))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

