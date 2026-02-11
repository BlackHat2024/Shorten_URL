package com.shorten_url.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String shortUrl, boolean isExpired) {
        super(isExpired
                ? "URL with short code '" + shortUrl + "' has expired"
                : "URL with short code '" + shortUrl + "' not found");
    }
}

