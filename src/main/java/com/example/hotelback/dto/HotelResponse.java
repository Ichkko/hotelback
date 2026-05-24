package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
    private List<Long> ownerIds;
    private List<Long> managerIds;
    private List<Long> receptionistIds;
    private List<Long> accountantIds;
    private HotelMembershipResponse membership;
    private HotelPermissionsResponse permissions;
}
