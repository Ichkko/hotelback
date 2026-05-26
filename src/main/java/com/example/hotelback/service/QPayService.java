package com.example.hotelback.service;

import com.example.hotelback.dto.QPayInvoiceRequest;
import com.example.hotelback.dto.QPayInvoiceResponse;
import com.example.hotelback.dto.QPayPaymentCheckResponse;

public interface QPayService {

    QPayInvoiceResponse createInvoice(QPayInvoiceRequest request);

    QPayPaymentCheckResponse checkPayment(Long paymentId);

    QPayPaymentCheckResponse handleCallback(String qpayInvoiceId, String senderInvoiceNo, Long paymentId);
}
