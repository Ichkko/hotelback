package com.example.hotelback.controller;

import com.example.hotelback.dto.CreatePaymentRequest;
import com.example.hotelback.dto.PaymentResponse;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final DtoMapper dtoMapper;

    public PaymentController(PaymentService paymentService, DtoMapper dtoMapper) {
        this.paymentService = paymentService;
        this.dtoMapper = dtoMapper;
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
