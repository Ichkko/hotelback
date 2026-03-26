package com.example.hotelback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomDetailRequest {

    @Size(max = 120, message = "Ангилал 120 тэмдэгтээс ихгүй байна")
    private String category;

    @NotBlank(message = "Мэдээллийн гарчиг хоосон байж болохгүй")
    @Size(max = 255, message = "Мэдээллийн гарчиг 255 тэмдэгтээс ихгүй байна")
    private String label;

    @Size(max = 1000, message = "Мэдээллийн утга 1000 тэмдэгтээс ихгүй байна")
    private String value;

    private Integer displayOrder;
}
