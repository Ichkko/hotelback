package com.example.hotelback.model;

import java.util.EnumSet;
import java.util.Set;

public enum HotelRole {
    OWNER(EnumSet.allOf(HotelPermission.class)),
    MANAGER(EnumSet.of(
            HotelPermission.HOTEL_VIEW,
            HotelPermission.HOTEL_UPDATE,
            HotelPermission.ROOM_MANAGE,
            HotelPermission.BOOKING_VIEW,
            HotelPermission.BOOKING_UPDATE
    )),
    RECEPTION(EnumSet.of(
            HotelPermission.HOTEL_VIEW,
            HotelPermission.BOOKING_VIEW,
            HotelPermission.BOOKING_UPDATE
    )),
    ACCOUNTANT(EnumSet.of(
            HotelPermission.HOTEL_VIEW,
            HotelPermission.PAYMENT_VIEW,
            HotelPermission.PAYMENT_MANAGE,
            HotelPermission.REPORT_VIEW
    ));

    private final Set<HotelPermission> permissions;

    HotelRole(Set<HotelPermission> permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(HotelPermission permission) {
        return permissions.contains(permission);
    }
}
