package com.shorten_url.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<?> handleUrlNotFound(UrlNotFoundException ex) {
        log.error("UrlNotFoundException: {}", ex.getMessage(), ex); // ✅ prints to console

        return ResponseEntity.status(404).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", 404,
                        "error", "NOT_FOUND",
                        "message", ex.getMessage()
                )
        );
    }
}
