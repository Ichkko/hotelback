package com.example.hotelback.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum RoomStatus {
    AVAILABLE,
    UNAVAILABLE,
    MAINTENANCE;

    @JsonCreator
    public static RoomStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid room status: " + value));
    }

    public static RoomStatus fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalizedValue = value.trim();
        if ("BOOKED".equalsIgnoreCase(normalizedValue)) {
            return UNAVAILABLE;
        }

        return fromValue(normalizedValue);
    }
}
