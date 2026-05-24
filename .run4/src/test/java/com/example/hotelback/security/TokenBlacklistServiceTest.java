package com.example.hotelback.security;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBlacklistServiceTest {

    private final TokenBlacklistService tokenBlacklistService = new TokenBlacklistService();

    @Test
    void blacklistedTokenIsRejectedUntilExpiry() {
        tokenBlacklistService.blacklist("token-123", Instant.now().plusSeconds(60));

        assertThat(tokenBlacklistService.isBlacklisted("token-123")).isTrue();
    }

    @Test
    void expiredBlacklistEntryIsIgnored() {
        tokenBlacklistService.blacklist("token-123", Instant.now().minusSeconds(10));

        assertThat(tokenBlacklistService.isBlacklisted("token-123")).isFalse();
    }
}
