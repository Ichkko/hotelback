package com.example.hotelback.security;

import com.example.hotelback.exception.ForbiddenException;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.AmenityRepository;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.HighlightRepository;
import com.example.hotelback.repository.HotelRepository;
import com.example.hotelback.repository.NotificationRepository;
import com.example.hotelback.repository.RoomRepository;
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

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private HighlightRepository highlightRepository;

    private OwnershipAccessService ownershipAccessService;
    private UserDetails userPrincipal;
    private UserDetails adminPrincipal;

    @BeforeEach
    void setUp() {
        ownershipAccessService = new OwnershipAccessService(
                userRepository,
                bookingRepository,
                notificationRepository,
                wishlistRepository,
                hotelRepository,
                roomRepository,
                amenityRepository,
                highlightRepository
        );
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

    @Test
    void assertBookingHotelOwnerOrAdminRejectsCrossHotelAccess() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(bookingRepository.existsById(20L)).thenReturn(true);
        when(bookingRepository.existsByIdAndHotelOwnerId(20L, 15L)).thenReturn(false);

        assertThatThrownBy(() -> ownershipAccessService.assertBookingHotelOwnerOrAdmin(20L, userPrincipal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("өөрийн буудлын захиалгад");
    }

    @Test
    void assertHotelStaffOrAdminAllowsReceptionist() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(hotelRepository.existsById(55L)).thenReturn(true);
        when(hotelRepository.existsByIdAndOwners_Id(55L, 15L)).thenReturn(false);
        when(hotelRepository.existsByIdAndReceptionists_Id(55L, 15L)).thenReturn(true);

        assertThatCode(() -> ownershipAccessService.assertHotelStaffOrAdmin(55L, userPrincipal))
                .doesNotThrowAnyException();
    }

    @Test
    void assertBookingCustomerOrHotelStaffOrAdminAllowsReceptionist() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findUserIdById(20L)).thenReturn(Optional.of(99L));
        when(bookingRepository.existsByIdAndHotelOwnerId(20L, 15L)).thenReturn(false);
        when(bookingRepository.existsByIdAndHotelReceptionistId(20L, 15L)).thenReturn(true);

        assertThatCode(() -> ownershipAccessService.assertBookingCustomerOrHotelStaffOrAdmin(20L, userPrincipal))
                .doesNotThrowAnyException();
    }

    @Test
    void assertRoomHotelStaffOrAdminAllowsReceptionist() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(roomRepository.existsById(81L)).thenReturn(true);
        when(roomRepository.existsByIdAndHotelOwnerId(81L, 15L)).thenReturn(false);
        when(roomRepository.existsByIdAndHotelReceptionistId(81L, 15L)).thenReturn(true);

        assertThatCode(() -> ownershipAccessService.assertRoomHotelStaffOrAdmin(81L, userPrincipal))
                .doesNotThrowAnyException();
    }
}
