package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QPayUrlResponse {
    private String name;
    private String description;
    private String logo;
    private String link;
}
