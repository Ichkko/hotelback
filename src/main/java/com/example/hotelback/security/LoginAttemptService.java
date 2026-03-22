package com.example.hotelback.security;

import com.example.hotelback.config.JwtProperties;
import com.example.hotelback.exception.ErrorCode;
import com.example.hotelback.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final JwtProperties jwtProperties;
    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public void checkAllowed(String email) {
        AttemptState state = attempts.get(normalize(email));
        if (state == null) {
            return;
        }

        if (state.blockedUntil != null && state.blockedUntil.isAfter(Instant.now())) {
            throw new TooManyRequestsException(
                    ErrorCode.AUTH_RATE_LIMITED,
                    "Олон удаагийн буруу оролдлого илэрлээ. Түр хүлээгээд дахин оролдоно уу"
            );
        }

        if (state.blockedUntil != null && !state.blockedUntil.isAfter(Instant.now())) {
            attempts.remove(normalize(email));
        }
    }

    public void recordFailure(String email) {
        String key = normalize(email);
        Instant now = Instant.now();
        AttemptState updated = attempts.compute(key, (ignored, state) -> {
            AttemptState current = state == null ? new AttemptState() : state;
            if (current.windowStartedAt == null || current.windowStartedAt.plusSeconds(jwtProperties.loginAttemptWindowSeconds()).isBefore(now)) {
                current.windowStartedAt = now;
                current.failures = 0;
                current.blockedUntil = null;
            }

            current.failures++;
            if (current.failures >= jwtProperties.loginMaxAttempts()) {
                current.blockedUntil = now.plusSeconds(jwtProperties.loginBlockDurationSeconds());
            }
            return current;
        });

        if (updated.blockedUntil != null && updated.blockedUntil.isAfter(now)) {
            throw new TooManyRequestsException(
                    ErrorCode.AUTH_RATE_LIMITED,
                    "Олон удаагийн буруу оролдлого илэрлээ. Түр хүлээгээд дахин оролдоно уу"
            );
        }
    }

    public void recordSuccess(String email) {
        attempts.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "unknown" : email.trim().toLowerCase();
    }

    private static class AttemptState {
        private int failures;
        private Instant windowStartedAt;
        private Instant blockedUntil;
    }
}
