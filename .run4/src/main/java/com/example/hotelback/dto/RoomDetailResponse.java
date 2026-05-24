package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomDetailResponse {
    private Long id;
    private String category;
    private String label;
    private String value;
    private Integer displayOrder;
}
