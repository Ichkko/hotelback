package com.example.hotelback.service;

import com.example.hotelback.model.Wishlist;

import java.util.List;

public interface WishlistService {
    Wishlist addToWishlist(Long userId, Long roomId);

    List<Wishlist> getWishlistByUserId(Long userId);

    boolean isInWishlist(Long userId, Long roomId);

    void removeById(Long id);

    void removeByUserAndRoom(Long userId, Long roomId);
}
