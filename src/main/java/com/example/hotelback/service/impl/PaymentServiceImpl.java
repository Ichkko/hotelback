package com.example.hotelback.service.impl;

import com.example.hotelback.model.Payment;
import com.example.hotelback.repository.PaymentRepository;
import com.example.hotelback.service.PaymentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Payment updatePayment(Long id, Payment payment) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        BeanUtils.copyProperties(payment, existing, "id", "createdAt", "updatedAt");
        return paymentRepository.save(existing);
    }

    @Override
    public void deletePaymentById(Long id) {
        paymentRepository.deleteById(id);
    }
}
