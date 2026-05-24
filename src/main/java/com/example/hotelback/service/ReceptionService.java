package com.example.hotelback.service;

import com.example.hotelback.dto.ReceptionDashboardResponse;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.Payment;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.RoomStatus;
import com.example.hotelback.model.RoomStatusHistory;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReceptionService {

    Booking checkIn(Long bookingId, UserDetails principal);

    Booking checkOut(Long bookingId, UserDetails principal);

    Room updateRoomStatus(Long roomId, RoomStatus status, UserDetails principal);

    RoomStatusHistory createRoomStatusPeriod(Long roomId, RoomStatus status, LocalDate startDate, LocalDate endDate,
                                             String note, UserDetails principal);

    List<RoomStatusHistory> getRoomStatusHistory(Long roomId, UserDetails principal);

    Payment collectPayment(Long bookingId, BigDecimal amount, String paymentMethod, UserDetails principal);

    ReceptionDashboardResponse getDashboard(Long hotelId, LocalDate date, UserDetails principal);
}
