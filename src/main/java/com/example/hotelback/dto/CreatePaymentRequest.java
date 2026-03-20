package com.example.hotelback.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotNull(message = "Booking ID заавал өгнө")
    private Long bookingId;

    @NotNull(message = "Төлбөрийн дүн заавал өгнө")
    @DecimalMin(value = "0.01", inclusive = true, message = "Төлбөрийн дүн 0-ээс их байна")
    private BigDecimal amount;

    @NotBlank(message = "Төлбөрийн арга заавал өгнө")
    private String paymentMethod;
}
