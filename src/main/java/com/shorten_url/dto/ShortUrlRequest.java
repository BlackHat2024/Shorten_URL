package com.shorten_url.dto;

import jakarta.validation.constraints.*;
import lombok.*;




@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlRequest {

    @NotBlank(message = "URL cannot be blank")
    @Pattern(regexp = "^(https?://).*", message = "URL must start with http:// or https://")
    private String url;

    @Min(value = 1)
    @Max(value = 5)
    private Integer expirationMinutes;
}