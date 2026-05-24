package com.example.hotelback.service;

import com.example.hotelback.dto.ReceptionDashboardResponse;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.Payment;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.RoomStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ReceptionService {

    Booking checkIn(Long bookingId);

    Booking checkOut(Long bookingId);

    Room updateRoomStatus(Long roomId, RoomStatus status);

    Payment collectPayment(Long bookingId, BigDecimal amount, String paymentMethod);

    ReceptionDashboardResponse getDashboard(Long hotelId, LocalDate date);
}
