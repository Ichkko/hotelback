package com.example.hotelback.controller;

import com.example.hotelback.dto.WishlistRequest;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.User;
import com.example.hotelback.model.Wishlist;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistControllerAuthorizationTest {

    @Mock
    private WishlistService wishlistService;

    @Mock
    private OwnershipAccessService ownershipAccessService;

    private WishlistController wishlistController;
    private UserDetails principal;

    @BeforeEach
    void setUp() {
        wishlistController = new WishlistController(wishlistService, ownershipAccessService);
        principal = new org.springframework.security.core.userdetails.User(
                "user@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void addToWishlistRequiresCurrentUserOwnership() {
        WishlistRequest request = new WishlistRequest();
        request.setUserId(15L);
        request.setRoomId(7L);

        Wishlist wishlist = new Wishlist();
        User user = new User();
        user.setId(15L);
        Room room = new Room();
        room.setId(7L);
        wishlist.setUser(user);
        wishlist.setRoom(room);
        when(wishlistService.addToWishlist(15L, 7L)).thenReturn(wishlist);

        Long responseUserId = wishlistController.addToWishlist(request, principal).getBody().getUserId();

        assertThat(responseUserId).isEqualTo(15L);
        verify(ownershipAccessService).assertCurrentUserOrAdmin(15L, principal);
    }

    @Test
    void getWishlistByUserRequiresCurrentUserOwnership() {
        when(wishlistService.getWishlistByUserId(15L)).thenReturn(List.of());

        wishlistController.getWishlistByUser(15L, principal);

        verify(ownershipAccessService).assertCurrentUserOrAdmin(15L, principal);
    }

    @Test
    void deleteByIdRequiresWishlistOwnership() {
        wishlistController.deleteById(11L, principal);

        verify(ownershipAccessService).assertWishlistOwnerOrAdmin(11L, principal);
        verify(wishlistService).removeById(11L);
    }
}
