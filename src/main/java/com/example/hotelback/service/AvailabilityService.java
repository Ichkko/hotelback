package com.example.hotelback.service;

import com.example.hotelback.dto.RoomAvailabilityResponse;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityService {

    List<RoomAvailabilityResponse> getHotelAvailability(Long hotelId, LocalDate from, LocalDate to);
}
