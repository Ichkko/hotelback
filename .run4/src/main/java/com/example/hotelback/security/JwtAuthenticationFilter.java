package com.example.hotelback.security;

import com.example.hotelback.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   CustomUserDetailsService userDetailsService,
                                   TokenBlacklistService tokenBlacklistService,
                                   JwtAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Blacklisted JWT used. path={}, remoteAddr={}", request.getRequestURI(), request.getRemoteAddr());
                authenticationEntryPoint.commence(request, response,
                        new JwtAuthenticationException(ErrorCode.AUTH_INVALID_TOKEN, "Хүчингүй болсон token байна"));
                return;
            }

            final String email = jwtUtil.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.isAccessTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT rejected. path={}, remoteAddr={}, reason={}", request.getRequestURI(), request.getRemoteAddr(), e.getMessage());
            authenticationEntryPoint.commence(request, response,
                    new JwtAuthenticationException(ErrorCode.AUTH_TOKEN_EXPIRED, "Token-ийн хугацаа дууссан байна"));
            return;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT rejected. path={}, remoteAddr={}, reason={}", request.getRequestURI(), request.getRemoteAddr(), e.getMessage());
            authenticationEntryPoint.commence(request, response,
                    new JwtAuthenticationException(ErrorCode.AUTH_INVALID_TOKEN, "JWT token буруу эсвэл эвдэрсэн байна"));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
