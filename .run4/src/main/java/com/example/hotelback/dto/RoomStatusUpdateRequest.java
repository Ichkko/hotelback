package com.example.hotelback.dto;

import com.example.hotelback.model.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomStatusUpdateRequest {
    @NotNull(message = "Өрөөний төлөв заавал байна")
    private RoomStatus status;
}
