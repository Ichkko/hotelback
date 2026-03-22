package com.example.hotelback.security;

import com.example.hotelback.exception.ForbiddenException;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.NotificationRepository;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipAccessServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    private OwnershipAccessService ownershipAccessService;
    private UserDetails userPrincipal;
    private UserDetails adminPrincipal;

    @BeforeEach
    void setUp() {
        ownershipAccessService = new OwnershipAccessService(userRepository, bookingRepository, notificationRepository, wishlistRepository);
        userPrincipal = new org.springframework.security.core.userdetails.User(
                "user@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        adminPrincipal = new org.springframework.security.core.userdetails.User(
                "admin@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void assertCurrentUserOrAdminAllowsSameUser() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatCode(() -> ownershipAccessService.assertCurrentUserOrAdmin(15L, userPrincipal))
                .doesNotThrowAnyException();
    }

    @Test
    void assertCurrentUserOrAdminRejectsDifferentUser() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> ownershipAccessService.assertCurrentUserOrAdmin(99L, userPrincipal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("өөрийн мэдээлэл");
    }

    @Test
    void adminOverrideSkipsUserOwnershipLookup() {
        assertThatCode(() -> ownershipAccessService.assertCurrentUserOrAdmin(99L, adminPrincipal))
                .doesNotThrowAnyException();

        verify(userRepository, never()).findByEmail("admin@example.com");
    }

    @Test
    void assertBookingOwnerOrAdminRejectsDifferentBookingOwner() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findUserIdById(20L)).thenReturn(Optional.of(99L));

        assertThatThrownBy(() -> ownershipAccessService.assertBookingOwnerOrAdmin(20L, userPrincipal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("өөрийн захиалгад");
    }
}
