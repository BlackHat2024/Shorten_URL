package com.shorten_url.dto;

import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpirationRequest {

    @NotNull(message = "Expiration minutes cannot be null")
    @Min(value = 1)
    @Max(value = 10)
    private Integer expirationMinutes;
}