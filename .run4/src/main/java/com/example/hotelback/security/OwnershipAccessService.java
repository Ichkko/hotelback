package com.example.hotelback.security;

import com.example.hotelback.exception.ErrorCode;
import com.example.hotelback.exception.ForbiddenException;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.exception.UnauthorizedException;
import com.example.hotelback.repository.AmenityRepository;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.HighlightRepository;
import com.example.hotelback.repository.HotelRepository;
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
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;
    private final HighlightRepository highlightRepository;

    public OwnershipAccessService(UserRepository userRepository,
                                  BookingRepository bookingRepository,
                                  NotificationRepository notificationRepository,
                                  WishlistRepository wishlistRepository,
                                  HotelRepository hotelRepository,
                                  RoomRepository roomRepository,
                                  AmenityRepository amenityRepository,
                                  HighlightRepository highlightRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
        this.wishlistRepository = wishlistRepository;
        this.hotelRepository = hotelRepository;
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

    public void assertBookingHotelOwnerOrAdmin(Long bookingId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId);
        }

        if (!bookingRepository.existsByIdAndHotelOwnerId(bookingId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын захиалгад хандах эрхтэй");
        }
    }

    public void assertBookingHotelStaffOrAdmin(Long bookingId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId);
        }

        if (!bookingRepository.existsByIdAndHotelOwnerId(bookingId, currentUserId)
                && !bookingRepository.existsByIdAndHotelReceptionistId(bookingId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын ажилтны хүрээнд хандах эрхтэй");
        }
    }

    public void assertBookingCustomerOrHotelStaffOrAdmin(Long bookingId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        Long bookingUserId = bookingRepository.findUserIdById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));

        if (currentUserId.equals(bookingUserId)) {
            return;
        }

        if (bookingRepository.existsByIdAndHotelOwnerId(bookingId, currentUserId)
                || bookingRepository.existsByIdAndHotelReceptionistId(bookingId, currentUserId)) {
            return;
        }

        throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та энэ захиалгад хандах эрхгүй");
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
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + hotelId);
        }

        if (!hotelRepository.existsByIdAndOwners_Id(hotelId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлыг удирдах эрхтэй");
        }
    }

    public void assertHotelStaffOrAdmin(Long hotelId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + hotelId);
        }

        if (!hotelRepository.existsByIdAndOwners_Id(hotelId, currentUserId)
                && !hotelRepository.existsByIdAndReceptionists_Id(hotelId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын staff хүрээнд хандах эрхтэй");
        }
    }

    public void assertRoomHotelOwnerOrAdmin(Long roomId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + roomId);
        }

        if (!roomRepository.existsByIdAndHotelOwnerId(roomId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын өрөөг удирдах эрхтэй");
        }
    }

    public void assertRoomHotelStaffOrAdmin(Long roomId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + roomId);
        }

        if (!roomRepository.existsByIdAndHotelOwnerId(roomId, currentUserId)
                && !roomRepository.existsByIdAndHotelReceptionistId(roomId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын staff хүрээнд өрөөг удирдах эрхтэй");
        }
    }

    public void assertAmenityHotelOwnerOrAdmin(Long amenityId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        if (!amenityRepository.existsById(amenityId)) {
            throw new ResourceNotFoundException("Amenity олдсонгүй: ID=" + amenityId);
        }

        if (!amenityRepository.existsByIdAndHotelOwnerId(amenityId, currentUserId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын amenity-г удирдах эрхтэй");
        }
    }

    public void assertHighlightHotelOwnerOrAdmin(Long highlightId, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }

        Long currentUserId = resolveCurrentUserId(principal);
        if (!highlightRepository.existsById(highlightId)) {
            throw new ResourceNotFoundException("Highlight олдсонгүй: ID=" + highlightId);
        }

        if (!highlightRepository.existsByIdAndHotelOwnerId(highlightId, currentUserId)) {
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
