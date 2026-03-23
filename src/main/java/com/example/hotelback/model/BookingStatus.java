package com.example.hotelback.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum BookingStatus {
    NEW,
    CONFIRMED,
    PAID,
    CANCELLED;

    @JsonCreator
    public static BookingStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.equalsIgnoreCase("PENDING")) {
            return NEW;
        }

        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking status: " + value));
    }
}
