package com.example.hotelback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.oauth2")
public record GoogleOAuthProperties(String clientId) {
}
