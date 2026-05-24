package com.example.hotelback.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Нэр хоосон байж болохгүй")
    private String name;

    @NotBlank(message = "Email хоосон байж болохгүй")
    @Email(message = "Email буруу форматтай")
    private String email;

    @NotBlank(message = "Нууц үг хоосон байж болохгүй")
    @Size(min = 6, message = "Нууц үг хамгийн багадаа 6 тэмдэгт байх ёстой")
    private String password;

    private String phone;

    private String role;

    private String userRole;
}
