package com.example.hotelback.dto;

import com.example.hotelback.model.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class QPayPaymentCheckResponse {
    private Long paymentId;
    private Long bookingId;
    private String qpayInvoiceId;
    private PaymentStatus status;
    private BigDecimal paidAmount;
}
