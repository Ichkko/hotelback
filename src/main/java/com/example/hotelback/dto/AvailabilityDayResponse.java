package com.example.hotelback.dto;

import com.example.hotelback.model.AvailabilityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AvailabilityDayResponse {
    private LocalDate date;
    private AvailabilityStatus status;
    private Long bookingId;
}
