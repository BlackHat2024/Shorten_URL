package com.shorten_url.controller;

import com.shorten_url.dto.*;
import com.shorten_url.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrlControllerTest {

    @Test
    void shorten_passesEmailFromAuthenticationPrincipal() {
        UrlService urlService = mock(UrlService.class);
        UrlController controller = new UrlController(urlService);

        ShortUrlRequest req = new ShortUrlRequest();
        req.setUrl("https://example.com");
        req.setExpirationMinutes(10);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("user@test.com");

        ShortUrlResponse mocked = new ShortUrlResponse();
        mocked.setShortUrl("http://x/AbCd1234");
        mocked.setOriginalUrl("https://example.com");

        when(urlService.shorten(req, "user@test.com")).thenReturn(mocked);

        ResponseEntity<ShortUrlResponse> res = controller.shorten(req, auth);

        assertThat(res.getBody().getOriginalUrl()).isEqualTo("https://example.com");
        verify(urlService).shorten(req, "user@test.com");
    }

    @Test
    void getAll_returnsFromService() {
        UrlService urlService = mock(UrlService.class);
        UrlController controller = new UrlController(urlService);

        when(urlService.getAllShortenedUrls()).thenReturn(List.of(new ShortUrlResponse()));

        ResponseEntity<List<ShortUrlResponse>> res = controller.getAll();

        assertThat(res.getBody()).hasSize(1);
        verify(urlService).getAllShortenedUrls();
    }

    @Test
    void getOriginalUrl_returnsFromService() {
        UrlService urlService = mock(UrlService.class);
        UrlController controller = new UrlController(urlService);

        OriginalUrl o = new OriginalUrl();
        o.setOriginalUrl("https://example.com");
        when(urlService.getOriginalUrl("http://x/AbCd1234")).thenReturn(o);

        ResponseEntity<OriginalUrl> res = controller.getOriginalUrl("http://x/AbCd1234");

        assertThat(res.getBody().getOriginalUrl()).isEqualTo("https://example.com");
    }

    @Test
    void overwriteExpiration_returnsFromService() {
        UrlService urlService = mock(UrlService.class);
        UrlController controller = new UrlController(urlService);

        UpdateExpirationRequest req = new UpdateExpirationRequest();
        req.setExpirationMinutes(25);

        ShortUrlResponse out = new ShortUrlResponse();
        out.setShortUrl("http://x/AbCd1234");
        when(urlService.overwriteExpirationByShortUrl("http://x/AbCd1234", req)).thenReturn(out);

        ResponseEntity<ShortUrlResponse> res = controller.overwriteExpiration("http://x/AbCd1234", req);

        assertThat(res.getBody().getShortUrl()).isEqualTo("http://x/AbCd1234");
    }
}
