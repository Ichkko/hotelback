package com.example.hotelback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {

    @NotNull(message = "userId заавал бөглөгдөнө")
    private Long userId;

    @NotBlank(message = "title заавал бөглөгдөнө")
    private String title;

    @NotBlank(message = "message заавал бөглөгдөнө")
    private String message;

    private String type;
}
