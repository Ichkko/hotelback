package com.example.hotelback.dto;

import com.example.hotelback.model.BookingStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateBookingRequest {

    private Long userId;

    @NotNull(message = "Өрөөний ID заавал өгнө")
    private Long roomId;

    @NotNull(message = "Check-in өдөр заавал өгнө")
    @FutureOrPresent(message = "Check-in өнөөдрөөс өмнөх байж болохгүй")
    private LocalDate checkinDate;

    @NotNull(message = "Check-out өдөр заавал өгнө")
    private LocalDate checkoutDate;

    @NotBlank(message = "Нэр хоосон байж болохгүй")
    private String firstName;

    @NotBlank(message = "Овог хоосон байж болохгүй")
    private String lastName;

    @Email(message = "Email формат буруу байна")
    @NotBlank(message = "Email хоосон байж болохгүй")
    private String email;

    @NotBlank(message = "Утасны дугаар хоосон байж болохгүй")
    private String phone;

    @NotNull(message = "Зочдын тоо заавал өгнө")
    @Min(value = 1, message = "Зочдын тоо 1-ээс багагүй байна")
    private Integer guestCount;

    private String specialRequests;

    private BigDecimal roomPrice;

    private BookingStatus status;
}
