package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HotelResponse {
    private Long id;
    private String name;
    private String address;
    private String aimag;
    private String phone;
    private String description;
    private Double startingPrice;
    private String coverImageUrl;
    private Long ownerId;
}
