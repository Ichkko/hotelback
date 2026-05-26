package com.example.hotelback.controller;

import com.example.hotelback.dto.CreatePaymentRequest;
import com.example.hotelback.dto.PaymentResponse;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final DtoMapper dtoMapper;
    private final OwnershipAccessService ownershipAccessService;

    public PaymentController(PaymentService paymentService,
                             DtoMapper dtoMapper,
                             OwnershipAccessService ownershipAccessService) {
        this.paymentService = paymentService;
        this.dtoMapper = dtoMapper;
        this.ownershipAccessService = ownershipAccessService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                         @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingHotelStaffOrAdmin(request.getBookingId(), principal);
        return ResponseEntity.ok(dtoMapper.toPaymentResponse(paymentService.createPayment(dtoMapper.toPayment(request))));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(@AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertAdmin(principal);
        return ResponseEntity.ok(paymentService.getAllPayments().stream().map(dtoMapper::toPaymentResponse).toList());
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByHotel(@PathVariable Long hotelId,
                                                                    @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paymentService.getPaymentsByHotelId(hotelId, principal)
                .stream().map(dtoMapper::toPaymentResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails principal) {
        return paymentService.getPaymentById(id)
                .map(payment -> {
                    Long bookingId = payment.getBooking() != null ? payment.getBooking().getId() : null;
                    if (bookingId != null) {
                        ownershipAccessService.assertBookingCustomerOrHotelStaffOrAdmin(bookingId, principal);
                    } else {
                        ownershipAccessService.assertAdmin(principal);
                    }
                    return ResponseEntity.ok(dtoMapper.toPaymentResponse(payment));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBooking(@PathVariable Long bookingId,
                                                                      @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paymentService.getPaymentsByBookingId(bookingId, principal)
                .stream()
                .map(dtoMapper::toPaymentResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(@PathVariable Long id,
                                                         @Valid @RequestBody CreatePaymentRequest request,
                                                         @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertAdmin(principal);
        return ResponseEntity.ok(dtoMapper.toPaymentResponse(paymentService.updatePayment(id, dtoMapper.toPayment(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePayment(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertAdmin(principal);
        paymentService.deletePaymentById(id);
        return ResponseEntity.ok("Payment with ID " + id + " deleted successfully");
    }
}
