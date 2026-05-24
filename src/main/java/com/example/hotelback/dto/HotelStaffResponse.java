package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HotelStaffResponse {
    private Long userId;
    private String name;
    private String email;
    private String hotelRole;  // OWNER | MANAGER | RECEPTION | ACCOUNTANT
}
