package com.example.hotelback.security;

import com.example.hotelback.exception.ErrorCode;
import com.example.hotelback.exception.ForbiddenException;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.exception.UnauthorizedException;
import com.example.hotelback.model.HotelRole;
import com.example.hotelback.repository.AmenityRepository;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.HighlightRepository;
import com.example.hotelback.repository.HotelUserRoleRepository;
import com.example.hotelback.repository.NotificationRepository;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.repository.WishlistRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class OwnershipAccessService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final WishlistRepository wishlistRepository;
    private final HotelUserRoleRepository hotelUserRoleRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;
    private final HighlightRepository highlightRepository;

    public OwnershipAccessService(UserRepository userRepository,
                                  BookingRepository bookingRepository,
                                  NotificationRepository notificationRepository,
                                  WishlistRepository wishlistRepository,
                                  HotelUserRoleRepository hotelUserRoleRepository,
                                  RoomRepository roomRepository,
                                  AmenityRepository amenityRepository,
                                  HighlightRepository highlightRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
        this.wishlistRepository = wishlistRepository;
        this.hotelUserRoleRepository = hotelUserRoleRepository;
        this.roomRepository = roomRepository;
        this.amenityRepository = amenityRepository;
        this.highlightRepository = highlightRepository;
    }

    public Long resolveCurrentUserId(UserDetails principal) {
        UserDetails authenticatedUser = requirePrincipal(principal);
        return userRepository.findByEmail(authenticatedUser.getUsername())
                .map(user -> user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Хэрэглэгч олдсонгүй: email=" + authenticatedUser.getUsername()));
    }

    public boolean isAdmin(UserDetails principal) {
        UserDetails authenticatedUser = requirePrincipal(principal);
        return authenticatedUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    public void assertCurrentUserOrAdmin(Long targetUserId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        if (!currentUserId.equals(targetUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн мэдээлэлд хандах эрхтэй");
        }
    }

    public void assertBookingOwnerOrAdmin(Long bookingId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        Long ownerId = bookingRepository.findUserIdById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));
        if (!currentUserId.equals(ownerId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн захиалгад хандах эрхтэй");
        }
    }

    public void assertNotificationOwnerOrAdmin(Long notificationId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        Long ownerId = notificationRepository.findUserIdById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Мэдэгдэл олдсонгүй: ID=" + notificationId));
        if (!currentUserId.equals(ownerId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн мэдэгдэлд хандах эрхтэй");
        }
    }

    public void assertWishlistOwnerOrAdmin(Long wishlistId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        Long ownerId = wishlistRepository.findUserIdById(wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist бичлэг олдсонгүй: ID=" + wishlistId));
        if (!currentUserId.equals(ownerId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн wishlist-д хандах эрхтэй");
        }
    }

    public void assertHotelOwnerOrAdmin(Long hotelId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        if (!hotelUserRoleRepository.existsByHotelIdAndUserIdAndRole(hotelId, currentUserId, HotelRole.OWNER)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлыг удирдах эрхтэй");
        }
    }

    public void assertHotelStaffOrAdmin(Long hotelId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        boolean isStaff = hotelUserRoleRepository.existsByHotelIdAndUserIdAndRole(hotelId, currentUserId, HotelRole.OWNER)
                || hotelUserRoleRepository.existsByHotelIdAndUserIdAndRole(hotelId, currentUserId, HotelRole.RECEPTION);
        if (!isStaff) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та энэ буудлын ажилтан биш байна");
        }
    }

    public void assertRoomHotelOwnerOrAdmin(Long roomId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        Long hotelId = roomRepository.findHotelIdByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + roomId));
        if (!hotelUserRoleRepository.existsByHotelIdAndUserIdAndRole(hotelId, currentUserId, HotelRole.OWNER)
                && !hotelUserRoleRepository.existsByHotelIdAndUserIdAndRole(hotelId, currentUserId, HotelRole.RECEPTION)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын өрөөг удирдах эрхтэй");
        }
    }

    public void assertAmenityHotelOwnerOrAdmin(Long amenityId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        Long hotelId = amenityRepository.findHotelIdByAmenityId(amenityId)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity олдсонгүй: ID=" + amenityId));
        if (!hotelUserRoleRepository.existsByHotelIdAndUserIdAndRole(hotelId, currentUserId, HotelRole.OWNER)
                && !hotelUserRoleRepository.existsByHotelIdAndUserIdAndRole(hotelId, currentUserId, HotelRole.RECEPTION)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын amenity-г удирдах эрхтэй");
        }
    }

    public void assertHighlightHotelOwnerOrAdmin(Long highlightId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        Long hotelId = highlightRepository.findHotelIdByHighlightId(highlightId)
                .orElseThrow(() -> new ResourceNotFoundException("Highlight олдсонгүй: ID=" + highlightId));
        if (!hotelUserRoleRepository.existsByHotelIdAndUserIdAndRole(hotelId, currentUserId, HotelRole.OWNER)
                && !hotelUserRoleRepository.existsByHotelIdAndUserIdAndRole(hotelId, currentUserId, HotelRole.RECEPTION)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын highlight-ыг удирдах эрхтэй");
        }
    }

    private UserDetails requirePrincipal(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Нэвтрээгүй хэрэглэгч байна");
        }
        return principal;
    }
}
