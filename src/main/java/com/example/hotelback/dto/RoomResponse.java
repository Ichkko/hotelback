package com.example.hotelback.dto;

import com.example.hotelback.model.RoomStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoomResponse {
    private Long id;
    private Long hotelId;
    private String roomType;
    private Double price;
    private Integer capacity;
    private String roomNumber;
    private Integer floor;
    private String wing;
    private String section;
    private Double positionX;
    private Double positionY;
    private RoomStatus status;
    private List<RoomDetailResponse> details;
    private List<RoomImageResponse> images;
    private String roomDetails;

    @Builder
    public RoomResponse(Long id,
                        Long hotelId,
                        String roomType,
                        Double price,
                        Integer capacity,
                        String roomNumber,
                        Integer floor,
                        String wing,
                        String section,
                        Double positionX,
                        Double positionY,
                        RoomStatus status,
                        List<RoomDetailResponse> details,
                        List<RoomImageResponse> images,
                        String roomDetails) {
        this.id = id;
        this.hotelId = hotelId;
        this.roomType = roomType;
        this.price = price;
        this.capacity = capacity;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.wing = wing;
        this.section = section;
        this.positionX = positionX;
        this.positionY = positionY;
        this.status = status;
        this.details = details;
        this.images = images;
        this.roomDetails = roomDetails;
    }
}
