package com.example.hotelback.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email хоосон байж болохгүй")
    @Email(message = "Email буруу форматтай")
    private String email;

    @NotBlank(message = "Нууц үг хоосон байж болохгүй")
    private String password;
}
