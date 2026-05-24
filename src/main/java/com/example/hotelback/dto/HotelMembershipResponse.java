package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HotelMembershipResponse {
    private Long hotelId;
    private String hotelName;
    private String role;
}
