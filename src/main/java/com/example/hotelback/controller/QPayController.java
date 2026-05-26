package com.example.hotelback.controller;

import com.example.hotelback.dto.QPayInvoiceRequest;
import com.example.hotelback.dto.QPayInvoiceResponse;
import com.example.hotelback.dto.QPayPaymentCheckResponse;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.repository.PaymentRepository;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.QPayService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/qpay")
public class QPayController {

    private final QPayService qPayService;
    private final OwnershipAccessService ownershipAccessService;
    private final PaymentRepository paymentRepository;

    public QPayController(QPayService qPayService,
                          OwnershipAccessService ownershipAccessService,
                          PaymentRepository paymentRepository) {
        this.qPayService = qPayService;
        this.ownershipAccessService = ownershipAccessService;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/invoice")
    public ResponseEntity<QPayInvoiceResponse> createInvoice(@Valid @RequestBody QPayInvoiceRequest request,
                                                             @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingCustomerOrHotelStaffOrAdmin(request.getBookingId(), principal);
        return ResponseEntity.ok(qPayService.createInvoice(request));
    }

    @PostMapping("/check")
    public ResponseEntity<QPayPaymentCheckResponse> checkPayment(@RequestParam Long paymentId,
                                                                 @AuthenticationPrincipal UserDetails principal) {
        Long bookingId = paymentRepository.findById(paymentId)
                .map(payment -> payment.getBooking().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Төлбөр олдсонгүй: ID=" + paymentId));
        ownershipAccessService.assertBookingCustomerOrHotelStaffOrAdmin(bookingId, principal);
        return ResponseEntity.ok(qPayService.checkPayment(paymentId));
    }

    @RequestMapping(value = "/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<QPayPaymentCheckResponse> callback(@RequestParam(required = false) String invoice_id,
                                                             @RequestParam(required = false) String sender_invoice_no,
                                                             @RequestParam(required = false) Long payment_id) {
        return ResponseEntity.ok(qPayService.handleCallback(invoice_id, sender_invoice_no, payment_id));
    }
}
