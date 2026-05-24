package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RoomImageResponse {
    private Long id;
    private String imageUrl;
    private String description;

    @Builder
    public RoomImageResponse(Long id, String imageUrl, String description) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.description = description;
    }
}
