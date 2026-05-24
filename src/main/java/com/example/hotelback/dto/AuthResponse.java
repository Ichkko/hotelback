package com.example.hotelback.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String email;
    private String name;

    /** Platform-wide role (ADMIN | USER). */
    private String globalRole;

    /**
     * @deprecated Use {@link #globalRole} instead. Will be removed in a future release.
     */
    @Deprecated
    private String role;

    public AuthResponse(String token,
                        String refreshToken,
                        Long userId,
                        String email,
                        String name,
                        String globalRole) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.globalRole = globalRole;
        this.role = globalRole;   // backward compat — same value
    }
}
