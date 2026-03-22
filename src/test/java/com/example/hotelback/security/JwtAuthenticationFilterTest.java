package com.example.hotelback.security;

import com.example.hotelback.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService, tokenBlacklistService, authenticationEntryPoint);
        userDetails = new User("user@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void blacklistedTokenTriggersStandardUnauthorizedResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/bookings");
        request.addHeader("Authorization", "Bearer blocked-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenBlacklistService.isBlacklisted("blocked-token")).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, (req, res) -> { });

        ArgumentCaptor<JwtAuthenticationException> captor = ArgumentCaptor.forClass(JwtAuthenticationException.class);
        verify(authenticationEntryPoint).commence(eq(request), eq(response), captor.capture());
        assertThat(captor.getValue().getErrorCode()).isEqualTo(ErrorCode.AUTH_INVALID_TOKEN);
    }

    @Test
    void expiredTokenTriggersExpiredTokenResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/bookings");
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenBlacklistService.isBlacklisted("expired-token")).thenReturn(false);
        when(jwtUtil.extractEmail("expired-token")).thenThrow(expiredJwtException());

        jwtAuthenticationFilter.doFilter(request, response, (req, res) -> { });

        ArgumentCaptor<JwtAuthenticationException> captor = ArgumentCaptor.forClass(JwtAuthenticationException.class);
        verify(authenticationEntryPoint).commence(eq(request), eq(response), captor.capture());
        assertThat(captor.getValue().getErrorCode()).isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED);
    }

    @Test
    void validTokenAuthenticatesUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/bookings");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenBlacklistService.isBlacklisted("valid-token")).thenReturn(false);
        when(jwtUtil.extractEmail("valid-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtUtil.isAccessTokenValid("valid-token", userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, (req, res) -> { });

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@example.com");
    }

    private ExpiredJwtException expiredJwtException() {
        return new ExpiredJwtException(
                null,
                Jwts.claims().subject("user@example.com").expiration(new Date(System.currentTimeMillis() - 1000)).build(),
                "expired"
        );
    }
}
