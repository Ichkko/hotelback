package com.example.hotelback.service.impl;

import com.example.hotelback.config.QPayProperties;
import com.example.hotelback.dto.QPayInvoiceRequest;
import com.example.hotelback.dto.QPayInvoiceResponse;
import com.example.hotelback.dto.QPayPaymentCheckResponse;
import com.example.hotelback.dto.QPayUrlResponse;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Payment;
import com.example.hotelback.model.PaymentStatus;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.PaymentRepository;
import com.example.hotelback.service.NotificationService;
import com.example.hotelback.service.QPayService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QPayServiceImpl implements QPayService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final QPayProperties qPayProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private Instant accessTokenExpiresAt;

    public QPayServiceImpl(PaymentRepository paymentRepository,
                           BookingRepository bookingRepository,
                           NotificationService notificationService,
                           QPayProperties qPayProperties,
                           RestClient.Builder restClientBuilder,
                           ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
        this.qPayProperties = qPayProperties;
        this.restClient = restClientBuilder.baseUrl(qPayProperties.baseUrl()).build();
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public QPayInvoiceResponse createInvoice(QPayInvoiceRequest request) {
        Booking booking = bookingRepository.findByIdForUpdate(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + request.getBookingId()));
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Зөвхөн CONFIRMED төлөвтэй захиалгад QPay нэхэмжлэл үүсгэнэ");
        }

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : remainingAmount(booking);
        if (amount.signum() <= 0) {
            throw new IllegalStateException("Төлөх үлдэгдэл дүн байхгүй байна");
        }
        if (alreadyPaid(booking.getId()).add(amount).compareTo(expectedTotal(booking)) > 0) {
            throw new IllegalStateException("Нийт төлбөрөөс илүү дүнгээр QPay нэхэмжлэл үүсгэж байна");
        }

        String senderInvoiceNo = "BOOKING-" + booking.getId() + "-" + System.currentTimeMillis();
        JsonNode invoice = createQPayInvoice(booking, amount, senderInvoiceNo);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentMethod("QPAY");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setQpaySenderInvoiceNo(senderInvoiceNo);
        payment.setQpayInvoiceId(text(invoice, "invoice_id"));
        payment.setQpayQrText(text(invoice, "qr_text"));
        payment.setQpayQrImage(text(invoice, "qr_image"));
        payment.setQpayResponse(toJson(invoice));

        return toInvoiceResponse(paymentRepository.save(payment), invoice);
    }

    @Override
    @Transactional
    public QPayPaymentCheckResponse checkPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Төлбөр олдсонгүй: ID=" + paymentId));
        return checkAndApply(payment);
    }

    @Override
    @Transactional
    public QPayPaymentCheckResponse handleCallback(String qpayInvoiceId, String senderInvoiceNo, Long paymentId) {
        Payment payment;
        if (paymentId != null) {
            payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Төлбөр олдсонгүй: ID=" + paymentId));
        } else if (StringUtils.hasText(qpayInvoiceId)) {
            payment = paymentRepository.findByQpayInvoiceId(qpayInvoiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("QPay invoice олдсонгүй: " + qpayInvoiceId));
        } else {
            payment = paymentRepository.findByQpaySenderInvoiceNo(senderInvoiceNo)
                    .orElseThrow(() -> new ResourceNotFoundException("QPay sender invoice олдсонгүй: " + senderInvoiceNo));
        }
        return checkAndApply(payment);
    }

    private QPayPaymentCheckResponse checkAndApply(Payment payment) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return toCheckResponse(payment, payment.getAmount());
        }
        if (!StringUtils.hasText(payment.getQpayInvoiceId())) {
            throw new IllegalStateException("QPay invoice ID хадгалагдаагүй байна");
        }
        JsonNode check = checkQPayPayment(payment.getQpayInvoiceId());
        payment.setQpayResponse(toJson(check));

        BigDecimal paidAmount = paidAmount(check);
        if (paidAmount.compareTo(payment.getAmount()) >= 0) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(paidDate(check));
            completeBookingIfFullyPaid(payment);
        }
        paymentRepository.save(payment);
        return toCheckResponse(payment, paidAmount);
    }

    private JsonNode createQPayInvoice(Booking booking, BigDecimal amount, String senderInvoiceNo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("invoice_code", qPayProperties.invoiceCode());
        body.put("sender_invoice_no", senderInvoiceNo);
        body.put("invoice_receiver_code", "terminal");
        body.put("sender_branch_code", qPayProperties.senderBranchCode());
        body.put("invoice_description", "Hotel booking " + booking.getBookingNumber());
        body.put("amount", amount);
        body.put("callback_url", callbackUrl(senderInvoiceNo));
        body.put("sender_staff_code", qPayProperties.senderStaffCode());

        return restClient.post()
                .uri("/v2/invoice")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private JsonNode checkQPayPayment(String qpayInvoiceId) {
        Map<String, Object> offset = Map.of("page_number", 1, "page_limit", 100);
        Map<String, Object> body = Map.of(
                "object_type", "INVOICE",
                "object_id", qpayInvoiceId,
                "offset", offset
        );

        return restClient.post()
                .uri("/v2/payment/check")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private String accessToken() {
        if (StringUtils.hasText(accessToken) && accessTokenExpiresAt != null && Instant.now().isBefore(accessTokenExpiresAt)) {
            return accessToken;
        }
        String basic = Base64.getEncoder()
                .encodeToString((qPayProperties.username() + ":" + qPayProperties.password()).getBytes(StandardCharsets.UTF_8));
        JsonNode token = restClient.post()
                .uri("/v2/auth/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .retrieve()
                .body(JsonNode.class);

        accessToken = text(token, "access_token");
        long expiresIn = token != null && token.has("expires_in") ? token.get("expires_in").asLong() : 3600;
        accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn - 60));
        return accessToken;
    }

    private String callbackUrl(String senderInvoiceNo) {
        String base = qPayProperties.callbackUrl();
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "sender_invoice_no=" + senderInvoiceNo;
    }

    private BigDecimal expectedTotal(Booking booking) {
        if (booking.getTotalPrice() != null) {
            return booking.getTotalPrice();
        }
        long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckinDate(), booking.getCheckoutDate());
        if (nights <= 0) {
            throw new IllegalStateException("Захиалгын огноо буруу тул төлбөр тооцоолох боломжгүй");
        }
        return BigDecimal.valueOf(booking.getRoom().getPrice()).multiply(BigDecimal.valueOf(nights));
    }

    private BigDecimal remainingAmount(Booking booking) {
        return expectedTotal(booking).subtract(alreadyPaid(booking.getId()));
    }

    private BigDecimal alreadyPaid(Long bookingId) {
        return paymentRepository.findByBooking_Id(bookingId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void completeBookingIfFullyPaid(Payment payment) {
        Booking booking = payment.getBooking();
        BigDecimal paid = paymentRepository.findByBooking_Id(booking.getId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .filter(p -> payment.getId() == null || !payment.getId().equals(p.getId()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(payment.getAmount());
        if (paid.compareTo(expectedTotal(booking)) >= 0) {
            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);
            notifyBookingUser(booking, "BOOKING_PAID", "Захиалга бүрэн төлөгдлөө",
                    "Таны захиалга бүрэн төлөгдөж PAID төлөвт шилжлээ. Дугаар: " + booking.getBookingNumber());
        }
        notifyBookingUser(booking, "PAYMENT_SUCCESS", "Төлбөр амжилттай",
                "Таны QPay төлбөр амжилттай бүртгэгдлээ. Дүн: " + payment.getAmount());
    }

    private void notifyBookingUser(Booking booking, String type, String title, String message) {
        if (booking.getUser() != null && booking.getUser().getId() != null) {
            notificationService.createNotification(booking.getUser().getId(), title, message, type);
        }
    }

    private QPayInvoiceResponse toInvoiceResponse(Payment payment, JsonNode invoice) {
        return QPayInvoiceResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .qpayInvoiceId(payment.getQpayInvoiceId())
                .senderInvoiceNo(payment.getQpaySenderInvoiceNo())
                .qrText(payment.getQpayQrText())
                .qrImage(payment.getQpayQrImage())
                .urls(urls(invoice))
                .build();
    }

    private QPayPaymentCheckResponse toCheckResponse(Payment payment, BigDecimal paidAmount) {
        return QPayPaymentCheckResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBooking().getId())
                .qpayInvoiceId(payment.getQpayInvoiceId())
                .status(payment.getStatus())
                .paidAmount(paidAmount)
                .build();
    }

    private List<QPayUrlResponse> urls(JsonNode invoice) {
        List<QPayUrlResponse> result = new ArrayList<>();
        JsonNode urls = invoice != null ? invoice.get("urls") : null;
        if (urls != null && urls.isArray()) {
            for (JsonNode url : urls) {
                result.add(QPayUrlResponse.builder()
                        .name(text(url, "name"))
                        .description(text(url, "description"))
                        .logo(text(url, "logo"))
                        .link(text(url, "link"))
                        .build());
            }
        }
        return result;
    }

    private BigDecimal paidAmount(JsonNode check) {
        if (check == null) {
            return BigDecimal.ZERO;
        }
        if (check.has("paid_amount")) {
            return decimal(check.get("paid_amount"));
        }
        JsonNode rows = check.get("rows");
        if (rows != null && rows.isArray()) {
            BigDecimal total = BigDecimal.ZERO;
            for (JsonNode row : rows) {
                if (row.has("payment_status") && !"PAID".equalsIgnoreCase(row.get("payment_status").asText())) {
                    continue;
                }
                if (row.has("payment_amount")) {
                    total = total.add(decimal(row.get("payment_amount")));
                } else if (row.has("amount")) {
                    total = total.add(decimal(row.get("amount")));
                }
            }
            return total;
        }
        return BigDecimal.ZERO;
    }

    private Instant paidDate(JsonNode check) {
        JsonNode rows = check != null ? check.get("rows") : null;
        if (rows != null && rows.isArray()) {
            for (JsonNode row : rows) {
                if (row.has("payment_status") && !"PAID".equalsIgnoreCase(row.get("payment_status").asText())) {
                    continue;
                }
                String paymentDate = text(row, "payment_date");
                if (StringUtils.hasText(paymentDate)) {
                    try {
                        return Instant.parse(paymentDate);
                    } catch (RuntimeException ignored) {
                        return Instant.now();
                    }
                }
            }
        }
        return Instant.now();
    }

    private BigDecimal decimal(JsonNode node) {
        if (node == null || node.isNull()) {
            return BigDecimal.ZERO;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual() && StringUtils.hasText(node.asText())) {
            try {
                return new BigDecimal(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node != null ? node.get(field) : null;
        return value != null && !value.isNull() ? value.asText() : null;
    }

    private String toJson(JsonNode node) {
        try {
            return node != null ? objectMapper.writeValueAsString(node) : null;
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
