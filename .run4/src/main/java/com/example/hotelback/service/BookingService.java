package com.example.hotelback.service;

import com.example.hotelback.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingService {

    Booking createBooking(Booking booking);

    List<Booking> getAllBookings();

    Optional<Booking> getBookingById(Long id);

    List<Booking> getBookingsByUserId(Long userId);

    List<Booking> getBookingsByHotelId(Long hotelId);

    Booking updateBooking(Long id, Booking booking);

    void deleteBookingById(Long id);

    Booking confirmBooking(Long id);

    Booking cancelBooking(Long id);
}
