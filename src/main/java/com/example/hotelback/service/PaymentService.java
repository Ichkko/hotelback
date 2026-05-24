package com.example.hotelback.service;

import com.example.hotelback.model.Payment;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface PaymentService {

    Payment createPayment(Payment payment);

    List<Payment> getAllPayments();

    Optional<Payment> getPaymentById(Long id);

    default List<Payment> getPaymentsByBookingId(Long bookingId) {
        return getPaymentsByBookingId(bookingId, null);
    }

    List<Payment> getPaymentsByBookingId(Long bookingId, UserDetails principal);

    List<Payment> getPaymentsByHotelId(Long hotelId, UserDetails principal);

    Payment updatePayment(Long id, Payment payment);

    void deletePaymentById(Long id);
}
