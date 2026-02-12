package com.shorten_url.controller;

import com.shorten_url.dto.ShortUrlResponse;
import com.shorten_url.service.UrlService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShortUrlControllerTest {

    @Test
    void redirect_returns404_whenCodeInvalid() {
        UrlService urlService = mock(UrlService.class);
        ShortUrlController c = new ShortUrlController(urlService);

        ResponseEntity<Void> res = c.redirect("bad!");

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(urlService);
    }

    @Test
    void redirect_returns302_whenResolved() {
        UrlService urlService = mock(UrlService.class);
        ShortUrlController c = new ShortUrlController(urlService);

        ShortUrlResponse s = new ShortUrlResponse();
        s.setOriginalUrl("https://example.com");
        when(urlService.resolveByCode("AbCd1234")).thenReturn(s);

        ResponseEntity<Void> res = c.redirect("AbCd1234");

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(res.getHeaders().getLocation().toString()).isEqualTo("https://example.com");
    }

    @Test
    void redirect_returns404_whenServiceThrows() {
        UrlService urlService = mock(UrlService.class);
        ShortUrlController c = new ShortUrlController(urlService);

        when(urlService.resolveByCode("AbCd1234")).thenThrow(new RuntimeException("fail"));

        ResponseEntity<Void> res = c.redirect("AbCd1234");

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
