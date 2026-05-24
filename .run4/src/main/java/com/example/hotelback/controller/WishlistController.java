package com.example.hotelback.controller;

import com.example.hotelback.dto.WishlistRequest;
import com.example.hotelback.dto.WishlistResponse;
import com.example.hotelback.model.Wishlist;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final OwnershipAccessService ownershipAccessService;

    public WishlistController(WishlistService wishlistService,
                              OwnershipAccessService ownershipAccessService) {
        this.wishlistService = wishlistService;
        this.ownershipAccessService = ownershipAccessService;
    }

    @PostMapping
    public ResponseEntity<WishlistResponse> addToWishlist(@Valid @RequestBody WishlistRequest request,
                                                          @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertCurrentUserOrAdmin(request.getUserId(), principal);
        Wishlist created = wishlistService.addToWishlist(request.getUserId(), request.getRoomId());
        return ResponseEntity.status(HttpStatus.CREATED).body(WishlistResponse.fromEntity(created));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WishlistResponse>> getWishlistByUser(@PathVariable Long userId,
                                                                    @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertCurrentUserOrAdmin(userId, principal);
        List<WishlistResponse> response = wishlistService.getWishlistByUserId(userId)
                .stream()
                .map(WishlistResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/room/{roomId}/exists")
    public ResponseEntity<Map<String, Boolean>> isInWishlist(@PathVariable Long userId,
                                                             @PathVariable Long roomId,
                                                             @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertCurrentUserOrAdmin(userId, principal);
        return ResponseEntity.ok(Map.of("exists", wishlistService.isInWishlist(userId, roomId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertWishlistOwnerOrAdmin(id, principal);
        wishlistService.removeById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/room/{roomId}")
    public ResponseEntity<Void> deleteByUserAndRoom(@PathVariable Long userId,
                                                    @PathVariable Long roomId,
                                                    @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertCurrentUserOrAdmin(userId, principal);
        wishlistService.removeByUserAndRoom(userId, roomId);
        return ResponseEntity.noContent().build();
    }
}
