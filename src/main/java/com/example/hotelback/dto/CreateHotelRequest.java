package com.example.hotelback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateHotelRequest {

    @NotBlank(message = "Зочид буудлын нэр хоосон байж болохгүй")
    private String name;

    private String address;
    private String aimag;
    private String phone;
    private String description;

    @PositiveOrZero(message = "Эхлэх үнэ 0-ээс багагүй байна")
    private Double startingPrice;

    private String coverImageUrl;

    // Зөвхөн ADMIN үүсгэх үед өөр хэрэглэгчид OWNER оноох боломжтой
    private Long ownerId;
}
