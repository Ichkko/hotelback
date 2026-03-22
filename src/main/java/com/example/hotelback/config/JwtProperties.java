package com.example.hotelback.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank(message = "JWT secret must be provided")
        @Size(min = 32, message = "JWT secret must be at least 32 characters long")
        String secret,
        @Min(value = 1, message = "JWT expiration must be greater than 0")
        long expiration,
        @Min(value = 1, message = "JWT refresh expiration must be greater than 0")
        long refreshExpiration,
        @Min(value = 1, message = "JWT login max attempts must be greater than 0")
        int loginMaxAttempts,
        @Min(value = 1, message = "JWT login attempt window seconds must be greater than 0")
        long loginAttemptWindowSeconds,
        @Min(value = 1, message = "JWT login block duration seconds must be greater than 0")
        long loginBlockDurationSeconds
) {
}
