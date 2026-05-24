package com.example.hotelback.dto;

import com.example.hotelback.model.Wishlist;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WishlistResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long roomId;
    private String roomType;
    private Double roomPrice;
    private Integer roomCapacity;
    private LocalDateTime createdAt;

    public static WishlistResponse fromEntity(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser() != null ? wishlist.getUser().getId() : null)
                .userName(wishlist.getUser() != null ? wishlist.getUser().getName() : null)
                .roomId(wishlist.getRoom() != null ? wishlist.getRoom().getId() : null)
                .roomType(wishlist.getRoom() != null ? wishlist.getRoom().getRoomType() : null)
                .roomPrice(wishlist.getRoom() != null ? wishlist.getRoom().getPrice() : null)
                .roomCapacity(wishlist.getRoom() != null ? wishlist.getRoom().getCapacity() : null)
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}
