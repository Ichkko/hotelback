package com.example.hotelback.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistRequest {

    @NotNull(message = "userId заавал бөглөгдөнө")
    private Long userId;

    @NotNull(message = "roomId заавал бөглөгдөнө")
    private Long roomId;
}
