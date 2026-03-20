package com.example.hotelback.dto;

import com.example.hotelback.model.RoomStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomResponse {
    private Long id;
    private Long hotelId;
    private String roomType;
    private Double price;
    private Integer capacity;
    private RoomStatus status;
}
