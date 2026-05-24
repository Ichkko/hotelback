package com.example.hotelback.controller;

import com.example.hotelback.dto.BookingResponse;
import com.example.hotelback.dto.PaymentResponse;
import com.example.hotelback.dto.ReceptionDashboardResponse;
import com.example.hotelback.dto.ReceptionPaymentRequest;
import com.example.hotelback.dto.RoomResponse;
import com.example.hotelback.dto.RoomStatusHistoryResponse;
import com.example.hotelback.dto.RoomStatusPeriodRequest;
import com.example.hotelback.dto.RoomStatusUpdateRequest;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.service.ReceptionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reception")
public class ReceptionController {

    private final ReceptionService receptionService;
    private final DtoMapper dtoMapper;

    public ReceptionController(ReceptionService receptionService,
                               DtoMapper dtoMapper) {
        this.receptionService = receptionService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping("/hotels/{hotelId}/dashboard")
    public ResponseEntity<ReceptionDashboardResponse> getDashboard(@PathVariable Long hotelId,
                                                                   @RequestParam(required = false)
                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                   @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(receptionService.getDashboard(hotelId, date, principal));
    }

    @PostMapping("/bookings/{bookingId}/check-in")
    public ResponseEntity<BookingResponse> checkIn(@PathVariable Long bookingId,
                                                   @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toBookingResponse(receptionService.checkIn(bookingId, principal)));
    }

    @PostMapping("/bookings/{bookingId}/check-out")
    public ResponseEntity<BookingResponse> checkOut(@PathVariable Long bookingId,
                                                    @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toBookingResponse(receptionService.checkOut(bookingId, principal)));
    }

    @PatchMapping("/rooms/{roomId}/status")
    public ResponseEntity<RoomResponse> updateRoomStatus(@PathVariable Long roomId,
                                                         @Valid @RequestBody RoomStatusUpdateRequest request,
                                                         @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toRoomResponse(receptionService.updateRoomStatus(roomId, request.getStatus(), principal)));
    }

    @PostMapping("/rooms/{roomId}/status-periods")
    public ResponseEntity<RoomStatusHistoryResponse> createRoomStatusPeriod(@PathVariable Long roomId,
                                                                            @Valid @RequestBody RoomStatusPeriodRequest request,
                                                                            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toRoomStatusHistoryResponse(
                receptionService.createRoomStatusPeriod(
                        roomId,
                        request.getStatus(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getNote(),
                        principal
                )
        ));
    }

    @GetMapping("/rooms/{roomId}/status-history")
    public ResponseEntity<List<RoomStatusHistoryResponse>> getRoomStatusHistory(@PathVariable Long roomId,
                                                                                @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(receptionService.getRoomStatusHistory(roomId, principal)
                .stream()
                .map(dtoMapper::toRoomStatusHistoryResponse)
                .toList());
    }

    @PostMapping("/bookings/{bookingId}/payments")
    public ResponseEntity<PaymentResponse> collectPayment(@PathVariable Long bookingId,
                                                          @Valid @RequestBody ReceptionPaymentRequest request,
                                                          @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toPaymentResponse(
                receptionService.collectPayment(bookingId, request.getAmount(), request.getPaymentMethod(), principal)
        ));
    }
}
