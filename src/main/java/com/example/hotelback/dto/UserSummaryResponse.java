package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;

    /** Platform-wide role (ADMIN | USER). */
    private String globalRole;

    /**
     * @deprecated Use {@link #globalRole} instead. Will be removed in a future release.
     */
    @Deprecated
    private String role;
}
