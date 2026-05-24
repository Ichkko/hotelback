package com.example.hotelback.controller;

import com.example.hotelback.dto.BookingResponse;
import com.example.hotelback.dto.PaymentResponse;
import com.example.hotelback.dto.ReceptionDashboardResponse;
import com.example.hotelback.dto.ReceptionPaymentRequest;
import com.example.hotelback.dto.RoomResponse;
import com.example.hotelback.dto.RoomStatusUpdateRequest;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.ReceptionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reception")
public class ReceptionController {

    private final ReceptionService receptionService;
    private final DtoMapper dtoMapper;
    private final OwnershipAccessService ownershipAccessService;

    public ReceptionController(ReceptionService receptionService,
                               DtoMapper dtoMapper,
                               OwnershipAccessService ownershipAccessService) {
        this.receptionService = receptionService;
        this.dtoMapper = dtoMapper;
        this.ownershipAccessService = ownershipAccessService;
    }

    @GetMapping("/hotels/{hotelId}/dashboard")
    public ResponseEntity<ReceptionDashboardResponse> getDashboard(@PathVariable Long hotelId,
                                                                   @RequestParam(required = false)
                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                   @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertHotelStaffOrAdmin(hotelId, principal);
        return ResponseEntity.ok(receptionService.getDashboard(hotelId, date));
    }

    @PostMapping("/bookings/{bookingId}/check-in")
    public ResponseEntity<BookingResponse> checkIn(@PathVariable Long bookingId,
                                                   @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingHotelStaffOrAdmin(bookingId, principal);
        return ResponseEntity.ok(dtoMapper.toBookingResponse(receptionService.checkIn(bookingId)));
    }

    @PostMapping("/bookings/{bookingId}/check-out")
    public ResponseEntity<BookingResponse> checkOut(@PathVariable Long bookingId,
                                                    @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingHotelStaffOrAdmin(bookingId, principal);
        return ResponseEntity.ok(dtoMapper.toBookingResponse(receptionService.checkOut(bookingId)));
    }

    @PatchMapping("/rooms/{roomId}/status")
    public ResponseEntity<RoomResponse> updateRoomStatus(@PathVariable Long roomId,
                                                         @Valid @RequestBody RoomStatusUpdateRequest request,
                                                         @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertRoomHotelStaffOrAdmin(roomId, principal);
        return ResponseEntity.ok(dtoMapper.toRoomResponse(receptionService.updateRoomStatus(roomId, request.getStatus())));
    }

    @PostMapping("/bookings/{bookingId}/payments")
    public ResponseEntity<PaymentResponse> collectPayment(@PathVariable Long bookingId,
                                                          @Valid @RequestBody ReceptionPaymentRequest request,
                                                          @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingHotelStaffOrAdmin(bookingId, principal);
        return ResponseEntity.ok(dtoMapper.toPaymentResponse(
                receptionService.collectPayment(bookingId, request.getAmount(), request.getPaymentMethod())
        ));
    }
}
