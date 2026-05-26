package com.example.hotelback.dto;

import com.example.hotelback.model.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class QPayInvoiceResponse {
    private Long paymentId;
    private Long bookingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String qpayInvoiceId;
    private String senderInvoiceNo;
    private String qrText;
    private String qrImage;
    private List<QPayUrlResponse> urls;
}
