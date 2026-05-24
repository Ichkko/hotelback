package com.example.hotelback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HotelPermissionsResponse {
    private boolean canManageHotel;
    private boolean canManageRooms;
    private boolean canViewBookings;
    private boolean canUpdateBookings;
    private boolean canManagePayments;
    private boolean canManageStaff;
    private boolean canViewReports;
}
