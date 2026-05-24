package com.example.hotelback.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAuthRequest {

    @NotBlank(message = "Google id token хоосон байж болохгүй")
    private String idToken;
}
