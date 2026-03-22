package com.example.hotelback.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) {
            return;
        }
        purgeExpired();
        blacklistedTokens.put(token, expiresAt != null ? expiresAt : Instant.now().plusSeconds(3600));
    }

    public boolean isBlacklisted(String token) {
        purgeExpired();
        Instant expiresAt = blacklistedTokens.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
