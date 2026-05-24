package com.example.hotelback.security;

import com.example.hotelback.exception.ErrorCode;
import com.example.hotelback.exception.ForbiddenException;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.exception.UnauthorizedException;
import com.example.hotelback.model.HotelPermission;
import com.example.hotelback.model.HotelRole;
import com.example.hotelback.model.HotelUserRole;
import com.example.hotelback.repository.AmenityRepository;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.HighlightRepository;
import com.example.hotelback.repository.HotelRepository;
import com.example.hotelback.repository.HotelUserRoleRepository;
import com.example.hotelback.repository.NotificationRepository;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.repository.WishlistRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OwnershipAccessService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final WishlistRepository wishlistRepository;
    private final HotelRepository hotelRepository;
    private final HotelUserRoleRepository hotelUserRoleRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;
    private final HighlightRepository highlightRepository;

    public OwnershipAccessService(UserRepository userRepository,
                                  BookingRepository bookingRepository,
                                  NotificationRepository notificationRepository,
                                  WishlistRepository wishlistRepository,
                                  HotelRepository hotelRepository,
                                  HotelUserRoleRepository hotelUserRoleRepository,
                                  RoomRepository roomRepository,
                                  AmenityRepository amenityRepository,
                                  HighlightRepository highlightRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
        this.wishlistRepository = wishlistRepository;
        this.hotelRepository = hotelRepository;
        this.hotelUserRoleRepository = hotelUserRoleRepository;
        this.roomRepository = roomRepository;
        this.amenityRepository = amenityRepository;
        this.highlightRepository = highlightRepository;
    }

    public Long resolveCurrentUserId(UserDetails principal) {
        return userRepository.findByEmail(requirePrincipal(principal).getUsername())
                .map(user -> user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Хэрэглэгч олдсонгүй"));
    }

    public boolean isAdmin(UserDetails principal) {
        return requirePrincipal(principal).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    public void assertAdmin(UserDetails principal) {
        if (!isAdmin(principal)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Зөвхөн системийн администратор хандах эрхтэй");
        }
    }

    public void assertCurrentUserOrAdmin(Long targetUserId, UserDetails principal) {
        if (isAdmin(principal)) return;
        Long currentUserId = resolveCurrentUserId(principal);
        if (!currentUserId.equals(targetUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн мэдээлэлд хандах эрхтэй");
        }
    }

    public void assertBookingOwnerOrAdmin(Long bookingId, UserDetails principal) {
        if (isAdmin(principal)) return;
        Long currentUserId = resolveCurrentUserId(principal);
        Long ownerId = bookingRepository.findUserIdById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));
        if (!currentUserId.equals(ownerId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн захиалгад хандах эрхтэй");
        }
    }

    public void assertBookingHotelOwnerOrAdmin(Long bookingId, UserDetails principal) {
        if (isAdmin(principal)) return;
        Long currentUserId = resolveCurrentUserId(principal);
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId);
        }
        if (!bookingRepository.existsByIdAndHotelOwnerId(bookingId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын захиалгад хандах эрхтэй");
        }
    }

    public void assertBookingHotelStaffOrAdmin(Long bookingId, UserDetails principal) {
        if (isAdmin(principal)) return;
        Long currentUserId = resolveCurrentUserId(principal);
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId);
        }
        if (!bookingRepository.existsByIdAndHotelStaffId(bookingId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын ажилтны хүрээнд хандах эрхтэй");
        }
    }

    public void assertBookingCustomerOrHotelStaffOrAdmin(Long bookingId, UserDetails principal) {
        if (isAdmin(principal)) return;
        Long currentUserId = resolveCurrentUserId(principal);
        Long bookingUserId = bookingRepository.findUserIdById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));
        if (currentUserId.equals(bookingUserId)) return;
        if (bookingRepository.existsByIdAndHotelStaffId(bookingId, currentUserId)) return;
        throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та энэ захиалгад хандах эрхгүй");
    }

    public void assertNotificationOwnerOrAdmin(Long notificationId, UserDetails principal) {
        if (isAdmin(principal)) return;
        Long currentUserId = resolveCurrentUserId(principal);
        Long ownerId = notificationRepository.findUserIdById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Мэдэгдэл олдсонгүй: ID=" + notificationId));
        if (!currentUserId.equals(ownerId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн мэдэгдэлд хандах эрхтэй");
        }
    }

    public void assertWishlistOwnerOrAdmin(Long wishlistId, UserDetails principal) {
        if (isAdmin(principal)) return;
        Long currentUserId = resolveCurrentUserId(principal);
        Long ownerId = wishlistRepository.findUserIdById(wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist бичлэг олдсонгүй: ID=" + wishlistId));
        if (!currentUserId.equals(ownerId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн wishlist-д хандах эрхтэй");
        }
    }

    public void assertHotelOwnerOrAdmin(Long hotelId, UserDetails principal) {
        assertHotelPermission(hotelId, principal, HotelPermission.HOTEL_UPDATE,
                "Та зөвхөн өөрийн буудлыг удирдах эрхтэй");
    }

    public void assertHotelStaffOrAdmin(Long hotelId, UserDetails principal) {
        assertHotelPermission(hotelId, principal, HotelPermission.HOTEL_VIEW,
                "Та зөвхөн өөрийн буудлын staff хүрээнд хандах эрхтэй");
    }

    public void assertHotelPermission(Long hotelId,
                                      UserDetails principal,
                                      HotelPermission permission,
                                      String message) {
        if (isAdmin(principal)) return;
        Long currentUserId = resolveCurrentUserId(principal);
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + hotelId);
        }
        if (!hasHotelPermission(hotelId, currentUserId, permission)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, message);
        }
    }

    public void assertHotelAnyPermission(Long hotelId,
                                         UserDetails principal,
                                         List<HotelPermission> permissions,
                                         String message) {
        if (isAdmin(principal)) return;
        Long currentUserId = resolveCurrentUserId(principal);
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + hotelId);
        }
        if (!permissions.stream().anyMatch(permission -> hasHotelPermission(hotelId, currentUserId, permission))) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, message);
        }
    }

    public void assertAmenityPermission(Long amenityId,
                                        UserDetails principal,
                                        HotelPermission permission,
                                        String message) {
        if (isAdmin(principal)) return;
        Long hotelId = amenityRepository.findById(amenityId)
                .map(amenity -> amenity.getHotel() != null ? amenity.getHotel().getId() : null)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity олдсонгүй: ID=" + amenityId));
        if (hotelId == null) {
            throw new ResourceNotFoundException("Amenity-д холбогдсон зочид буудал олдсонгүй: ID=" + amenityId);
        }
        assertHotelPermission(hotelId, principal, permission, message);
    }

    public void assertHighlightPermission(Long highlightId,
                                          UserDetails principal,
                                          HotelPermission permission,
                                          String message) {
        if (isAdmin(principal)) return;
        Long hotelId = highlightRepository.findById(highlightId)
                .map(highlight -> highlight.getHotel() != null ? highlight.getHotel().getId() : null)
                .orElseThrow(() -> new ResourceNotFoundException("Highlight олдсонгүй: ID=" + highlightId));
        if (hotelId == null) {
            throw new ResourceNotFoundException("Highlight-д холбогдсон зочид буудал олдсонгүй: ID=" + highlightId);
        }
        assertHotelPermission(hotelId, principal, permission, message);
    }

    public void assertRoomHotelOwnerOrAdmin(Long roomId, UserDetails principal) {
        assertRoomPermission(roomId, principal, HotelPermission.ROOM_MANAGE,
                "Та зөвхөн өөрийн буудлын өрөөг удирдах эрхтэй");
    }

    public void assertRoomHotelStaffOrAdmin(Long roomId, UserDetails principal) {
        assertRoomPermission(roomId, principal, HotelPermission.BOOKING_UPDATE,
                "Та зөвхөн өөрийн буудлын staff хүрээнд өрөөг удирдах эрхтэй");
    }

    public void assertRoomPermission(Long roomId,
                                     UserDetails principal,
                                     HotelPermission permission,
                                     String message) {
        if (isAdmin(principal)) return;
        Long hotelId = roomRepository.findById(roomId)
                .map(room -> room.getHotel() != null ? room.getHotel().getId() : null)
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + roomId));
        if (hotelId == null) {
            throw new ResourceNotFoundException("Өрөөний буудал олдсонгүй: ID=" + roomId);
        }
        assertHotelPermission(hotelId, principal, permission, message);
    }

    public void assertRoomAnyPermission(Long roomId,
                                        UserDetails principal,
                                        List<HotelPermission> permissions,
                                        String message) {
        if (isAdmin(principal)) return;
        Long hotelId = roomRepository.findById(roomId)
                .map(room -> room.getHotel() != null ? room.getHotel().getId() : null)
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + roomId));
        if (hotelId == null) {
            throw new ResourceNotFoundException("Өрөөний буудал олдсонгүй: ID=" + roomId);
        }
        assertHotelAnyPermission(hotelId, principal, permissions, message);
    }

    public void assertBookingPermission(Long bookingId,
                                        UserDetails principal,
                                        HotelPermission permission,
                                        String message) {
        if (isAdmin(principal)) return;
        Long hotelId = bookingRepository.findHotelIdById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));
        assertHotelPermission(hotelId, principal, permission, message);
    }

    public void assertBookingAnyPermission(Long bookingId,
                                           UserDetails principal,
                                           List<HotelPermission> permissions,
                                           String message) {
        if (isAdmin(principal)) return;
        Long hotelId = bookingRepository.findHotelIdById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));
        assertHotelAnyPermission(hotelId, principal, permissions, message);
    }

    public void assertAmenityHotelOwnerOrAdmin(Long amenityId, UserDetails principal) {
        assertAmenityPermission(amenityId, principal, HotelPermission.HOTEL_UPDATE,
                "Та зөвхөн өөрийн буудлын amenity-г удирдах эрхтэй");
    }

    public void assertHighlightHotelOwnerOrAdmin(Long highlightId, UserDetails principal) {
        assertHighlightPermission(highlightId, principal, HotelPermission.HOTEL_UPDATE,
                "Та зөвхөн өөрийн буудлын highlight-ыг удирдах эрхтэй");
    }

    private boolean hasHotelPermission(Long hotelId, Long userId, HotelPermission permission) {
        List<HotelUserRole> roles = hotelUserRoleRepository.findByHotelIdAndUserId(hotelId, userId);
        return roles.stream()
                .map(HotelUserRole::getRole)
                .anyMatch(role -> role != null && role.hasPermission(permission));
    }

    private UserDetails requirePrincipal(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Нэвтрээгүй хэрэглэгч байна");
        }
        return principal;
    }
}
