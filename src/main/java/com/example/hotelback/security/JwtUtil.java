package com.example.hotelback.security;

import com.example.hotelback.config.JwtProperties;
import com.example.hotelback.model.GlobalRole;
import com.example.hotelback.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return generateToken(user, jwtProperties.expiration(), "access");
    }

    public String generateAccessToken(String email, String role) {
        return generateAccessToken(buildTokenUser(email, role));
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, jwtProperties.refreshExpiration(), "refresh");
    }

    public String generateRefreshToken(String email, String role) {
        return generateRefreshToken(buildTokenUser(email, role));
    }

    public String generateToken(User user) {
        return generateAccessToken(user);
    }

    public String generateToken(String email, String role) {
        return generateAccessToken(email, role);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractGlobalRole(String token) {
        return extractAllClaims(token).get("globalRole", String.class);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("tokenType", String.class);
    }

    public Instant extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration).toInstant();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, "access");
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, "refresh");
    }

    private boolean isTokenValid(String token, UserDetails userDetails, String expectedType) {
        final String email = extractEmail(token);
        final String tokenType = extractTokenType(token);
        return email.equals(userDetails.getUsername()) && expectedType.equals(tokenType) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private String generateToken(User user, long expiration, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("globalRole", (user.getGlobalRole() != null ? user.getGlobalRole() : GlobalRole.USER).name());
        claims.put("tokenType", tokenType);
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private User buildTokenUser(String email, String role) {
        User user = new User();
        user.setEmail(email);
        user.setGlobalRole("ADMIN".equalsIgnoreCase(role) ? GlobalRole.ADMIN : GlobalRole.USER);
        return user;
    }
}
