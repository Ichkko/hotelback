package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoomAvailabilityResponse {
    private Long roomId;
    private String roomNumber;
    private String roomType;
    private List<AvailabilityDayResponse> days;
}
