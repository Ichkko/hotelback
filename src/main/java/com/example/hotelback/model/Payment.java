package com.example.hotelback.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "payments"})
    private Booking booking;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private PaymentStatus status;

    @Column(name = "payment_date")
    private Instant paymentDate;

    @Column(name = "qpay_invoice_id", length = 100)
    private String qpayInvoiceId;

    @Column(name = "qpay_sender_invoice_no", length = 100)
    private String qpaySenderInvoiceNo;

    @Column(name = "qpay_qr_text", length = 2000)
    private String qpayQrText;

    @Column(name = "qpay_qr_image", columnDefinition = "TEXT")
    private String qpayQrImage;

    @Column(name = "qpay_response", columnDefinition = "TEXT")
    private String qpayResponse;
}
