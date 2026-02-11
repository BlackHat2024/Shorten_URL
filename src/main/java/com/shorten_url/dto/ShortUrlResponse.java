package com.shorten_url.dto;


import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ShortUrlResponse {

    private String shortUrl;

    private String originalUrl;

    private LocalDateTime createdAt;

    private LocalDateTime expirationTime;

    private Long clickCount;

    private String createdBy;
}
