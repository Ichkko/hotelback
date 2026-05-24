package com.example.hotelback.security;

import com.example.hotelback.exception.ForbiddenException;
import com.example.hotelback.model.HotelPermission;
import com.example.hotelback.model.HotelRole;
import com.example.hotelback.model.HotelUserRole;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.AmenityRepository;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.HighlightRepository;
import com.example.hotelback.repository.HotelRepository;
import com.example.hotelback.repository.HotelUserRoleRepository;
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
    private HotelUserRoleRepository hotelUserRoleRepository;

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
                hotelUserRoleRepository,
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
        HotelUserRole hotelUserRole = new HotelUserRole();
        hotelUserRole.setRole(HotelRole.RECEPTION);
        when(hotelUserRoleRepository.findByHotelIdAndUserId(55L, 15L)).thenReturn(List.of(hotelUserRole));

        assertThatCode(() -> ownershipAccessService.assertHotelStaffOrAdmin(55L, userPrincipal))
                .doesNotThrowAnyException();
    }

    @Test
    void assertBookingCustomerOrHotelStaffOrAdminAllowsReceptionist() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findUserIdById(20L)).thenReturn(Optional.of(99L));
        when(bookingRepository.existsByIdAndHotelStaffId(20L, 15L)).thenReturn(true);

        assertThatCode(() -> ownershipAccessService.assertBookingCustomerOrHotelStaffOrAdmin(20L, userPrincipal))
                .doesNotThrowAnyException();
    }

    @Test
    void assertRoomHotelStaffOrAdminAllowsReceptionist() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        com.example.hotelback.model.Room room = new com.example.hotelback.model.Room();
        com.example.hotelback.model.Hotel hotel = new com.example.hotelback.model.Hotel();
        hotel.setId(44L);
        room.setHotel(hotel);
        HotelUserRole hotelUserRole = new HotelUserRole();
        hotelUserRole.setRole(HotelRole.RECEPTION);
        when(roomRepository.findById(81L)).thenReturn(Optional.of(room));
        when(hotelRepository.existsById(44L)).thenReturn(true);
        when(hotelUserRoleRepository.findByHotelIdAndUserId(44L, 15L)).thenReturn(List.of(hotelUserRole));

        assertThatCode(() -> ownershipAccessService.assertRoomHotelStaffOrAdmin(81L, userPrincipal))
                .doesNotThrowAnyException();
    }

    @Test
    void assertHotelAnyPermissionAllowsWhenOnePermissionMatches() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(hotelRepository.existsById(55L)).thenReturn(true);
        HotelUserRole hotelUserRole = new HotelUserRole();
        hotelUserRole.setRole(HotelRole.ACCOUNTANT);
        when(hotelUserRoleRepository.findByHotelIdAndUserId(55L, 15L)).thenReturn(List.of(hotelUserRole));

        assertThatCode(() -> ownershipAccessService.assertHotelAnyPermission(
                55L,
                userPrincipal,
                List.of(HotelPermission.PAYMENT_MANAGE, HotelPermission.REPORT_VIEW),
                "forbidden"
        )).doesNotThrowAnyException();
    }

    @Test
    void assertBookingPermissionRejectsWhenRequiredPermissionMissing() {
        User user = new User();
        user.setId(15L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findHotelIdById(20L)).thenReturn(Optional.of(55L));
        when(hotelRepository.existsById(55L)).thenReturn(true);
        HotelUserRole hotelUserRole = new HotelUserRole();
        hotelUserRole.setRole(HotelRole.ACCOUNTANT);
        when(hotelUserRoleRepository.findByHotelIdAndUserId(55L, 15L)).thenReturn(List.of(hotelUserRole));

        assertThatThrownBy(() -> ownershipAccessService.assertBookingPermission(
                20L,
                userPrincipal,
                HotelPermission.BOOKING_UPDATE,
                "forbidden"
        )).isInstanceOf(ForbiddenException.class);
    }
}
