package com.example.hotelback.dto;

import com.example.hotelback.model.HotelRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignHotelRoleRequest {

    @NotNull(message = "Хэрэглэгчийн ID хоосон байж болохгүй")
    private Long userId;

    @NotNull(message = "Үүрэг хоосон байж болохгүй")
    private HotelRole role;
}
