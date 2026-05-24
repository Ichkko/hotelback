package com.example.hotelback.service;

import com.example.hotelback.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentService {

    Payment createPayment(Payment payment);

    List<Payment> getAllPayments();

    Optional<Payment> getPaymentById(Long id);

    List<Payment> getPaymentsByBookingId(Long bookingId);

    Payment updatePayment(Long id, Payment payment);

    void deletePaymentById(Long id);
}
