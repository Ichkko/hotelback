package com.example.hotelback.controller;

import com.example.hotelback.model.Wishlist;
import com.example.hotelback.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    public ResponseEntity<Wishlist> addToWishlist(@RequestParam Long userId, @RequestParam Long roomId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(userId, roomId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Wishlist>> getWishlistByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(wishlistService.getWishlistByUserId(userId));
    }

    @GetMapping("/user/{userId}/room/{roomId}/exists")
    public ResponseEntity<Map<String, Boolean>> isInWishlist(@PathVariable Long userId, @PathVariable Long roomId) {
        return ResponseEntity.ok(Map.of("exists", wishlistService.isInWishlist(userId, roomId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        wishlistService.removeById(id);
        return ResponseEntity.ok("Wishlist-с устгагдлаа");
    }

    @DeleteMapping("/user/{userId}/room/{roomId}")
    public ResponseEntity<String> deleteByUserAndRoom(@PathVariable Long userId, @PathVariable Long roomId) {
        wishlistService.removeByUserAndRoom(userId, roomId);
        return ResponseEntity.ok("Wishlist-с устгагдлаа");
    }
}
