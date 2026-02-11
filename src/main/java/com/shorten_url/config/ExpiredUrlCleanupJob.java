package com.shorten_url.config;

import com.shorten_url.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpiredUrlCleanupJob {

    private final UrlService urlService;

    @Scheduled(fixedDelayString = "${url-shortener.cleanup-interval-ms}")
    public void cleanup() {
        urlService.deleteExpiredNow();
    }
}