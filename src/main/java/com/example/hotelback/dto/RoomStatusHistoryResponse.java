package com.example.hotelback.dto;

import com.example.hotelback.model.RoomStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RoomStatusHistoryResponse {
    private Long id;
    private Long roomId;
    private RoomStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
}
