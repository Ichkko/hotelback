package com.example.hotelback.security;

import com.example.hotelback.exception.ErrorCode;
import com.example.hotelback.exception.ForbiddenException;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.exception.UnauthorizedException;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.NotificationRepository;
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

    public OwnershipAccessService(UserRepository userRepository,
                                  BookingRepository bookingRepository,
                                  NotificationRepository notificationRepository,
                                  WishlistRepository wishlistRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
        this.wishlistRepository = wishlistRepository;
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

    private UserDetails requirePrincipal(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Нэвтрээгүй хэрэглэгч байна");
        }
        return principal;
    }
}
