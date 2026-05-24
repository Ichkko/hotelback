package com.example.hotelback.service.impl;

import com.example.hotelback.config.GoogleOAuthProperties;
import com.example.hotelback.exception.BadRequestException;
import com.example.hotelback.exception.ErrorCode;
import com.example.hotelback.exception.UnauthorizedException;
import com.example.hotelback.service.GoogleAuthService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private static final String GOOGLE_CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final List<String> ALLOWED_ISSUERS = List.of("accounts.google.com", "https://accounts.google.com");

    private final GoogleOAuthProperties properties;
    private final JwtDecoder jwtDecoder;

    public GoogleAuthServiceImpl(GoogleOAuthProperties properties) {
        this.properties = properties;
        this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_CERTS_URL).build();
    }

    @Override
    public GoogleUserInfo verify(String idToken) {
        String clientId = properties.clientId();
        if (clientId == null || clientId.isBlank()) {
            throw new BadRequestException(ErrorCode.INVALID_REQUEST, "Google client id тохируулаагүй байна");
        }

        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(idToken);
        } catch (JwtException e) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_TOKEN, "Google token буруу эсвэл хугацаа нь дууссан байна");
        }

        if (!ALLOWED_ISSUERS.contains(jwt.getIssuer() != null ? jwt.getIssuer().toString() : "")) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_TOKEN, "Google token issuer буруу байна");
        }
        if (!jwt.getAudience().contains(clientId)) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_TOKEN, "Google token энэ app-д зориулагдаагүй байна");
        }
        if (!Boolean.TRUE.equals(jwt.getClaim("email_verified"))) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_TOKEN, "Google email баталгаажаагүй байна");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_TOKEN, "Google token email агуулаагүй байна");
        }

        String name = jwt.getClaimAsString("name");
        if (name == null || name.isBlank()) {
            name = email.substring(0, email.indexOf('@'));
        }

        return new GoogleUserInfo(email, name);
    }
}
