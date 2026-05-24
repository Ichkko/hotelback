package com.example.hotelback.dto;

import com.example.hotelback.model.RoomStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateRoomRequest {

    @NotNull(message = "Hotel ID заавал өгнө")
    private Long hotelId;

    @NotBlank(message = "Өрөөний төрөл хоосон байж болохгүй")
    private String roomType;

    @NotNull(message = "Үнэ заавал өгнө")
    @PositiveOrZero(message = "Үнэ 0-ээс багагүй байна")
    private Double price;

    @NotNull(message = "Багтаамж заавал өгнө")
    @Min(value = 1, message = "Багтаамж 1-ээс багагүй байна")
    private Integer capacity;

    @NotBlank(message = "Өрөөний дугаар хоосон байж болохгүй")
    @Size(max = 50, message = "Өрөөний дугаар 50 тэмдэгтээс ихгүй байна")
    private String roomNumber;

    @NotNull(message = "Давхар заавал өгнө")
    private Integer floor;

    @Size(max = 100, message = "Wing 100 тэмдэгтээс ихгүй байна")
    private String wing;

    @Size(max = 100, message = "Section 100 тэмдэгтээс ихгүй байна")
    private String section;

    private Double positionX;

    private Double positionY;

    @NotNull(message = "Өрөөний төлөв заавал өгнө")
    private RoomStatus status;

    @Valid
    private List<RoomDetailRequest> details;

    @Size(max = 2000, message = "Өрөөний мэдээлэл 2000 тэмдэгтээс ихгүй байна")
    private String roomDetails;

}
