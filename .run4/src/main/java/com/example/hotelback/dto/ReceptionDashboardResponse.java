package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ReceptionDashboardResponse {
    private Long hotelId;
    private LocalDate date;
    private long totalRooms;
    private long availableRooms;
    private long unavailableRooms;
    private long maintenanceRooms;
    private long arrivalsToday;
    private long departuresToday;
    private long activeStays;
    private long pendingBookings;
    private long confirmedBookings;
    private long paidBookings;
}
