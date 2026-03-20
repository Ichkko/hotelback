package com.example.hotelback.dto;

import com.example.hotelback.model.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class BookingResponse {
    private Long id;
    private UserSummaryResponse user;
    private RoomResponse room;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Integer guestCount;
    private String specialRequests;
    private Integer nights;
    private BigDecimal roomPrice;
    private BigDecimal serviceFee;
    private BigDecimal totalPrice;
    private String bookingNumber;
    private BookingStatus status;
}
