package com.example.hotelback.dto;

import com.example.hotelback.model.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RoomStatusPeriodRequest {
    @NotNull
    private RoomStatus status;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private String note;
}
