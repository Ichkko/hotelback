package com.example.hotelback.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class QPayInvoiceRequest {

    @NotNull(message = "Booking ID заавал өгнө")
    private Long bookingId;

    @DecimalMin(value = "0.01", inclusive = true, message = "Төлбөрийн дүн 0-ээс их байна")
    private BigDecimal amount;
}
