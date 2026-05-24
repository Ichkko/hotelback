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
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(dtoMapper.toPaymentResponse(paymentService.createPayment(dtoMapper.toPayment(request))));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments().stream().map(dtoMapper::toPaymentResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(dtoMapper::toPaymentResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBooking(@PathVariable Long bookingId,
                                                                      @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingHotelStaffOrAdmin(bookingId, principal);
        return ResponseEntity.ok(paymentService.getPaymentsByBookingId(bookingId)
                .stream()
                .map(dtoMapper::toPaymentResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(@PathVariable Long id, @Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(dtoMapper.toPaymentResponse(paymentService.updatePayment(id, dtoMapper.toPayment(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePayment(@PathVariable Long id) {
        paymentService.deletePaymentById(id);
        return ResponseEntity.ok("Payment with ID " + id + " deleted successfully");
    }
}
