package com.example.hotelback.security;

import com.example.hotelback.config.JwtProperties;
import com.example.hotelback.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "test-secret-key-test-secret-key-1234",
                86_400_000L,
                604_800_000L,
                3,
                300,
                600
        );
        loginAttemptService = new LoginAttemptService(properties);
    }

    @Test
    void blocksAfterConfiguredNumberOfFailures() {
        loginAttemptService.recordFailure("user@example.com");
        loginAttemptService.recordFailure("user@example.com");

        assertThatThrownBy(() -> loginAttemptService.recordFailure("user@example.com"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void successClearsFailureState() {
        loginAttemptService.recordFailure("user@example.com");
        loginAttemptService.recordSuccess("user@example.com");

        assertThatCode(() -> loginAttemptService.checkAllowed("user@example.com"))
                .doesNotThrowAnyException();
    }
}
