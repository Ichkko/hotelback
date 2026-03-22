package com.example.hotelback.controller;

import com.example.hotelback.dto.NotificationRequest;
import com.example.hotelback.model.Notification;
import com.example.hotelback.model.User;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.NotificationService;
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
class NotificationControllerAuthorizationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private OwnershipAccessService ownershipAccessService;

    private NotificationController notificationController;
    private UserDetails principal;

    @BeforeEach
    void setUp() {
        notificationController = new NotificationController(notificationService, ownershipAccessService);
        principal = new org.springframework.security.core.userdetails.User(
                "user@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void getByUserRequiresCurrentUserOwnership() {
        when(notificationService.getNotificationsByUserId(15L)).thenReturn(List.of());

        notificationController.getByUser(15L, principal);

        verify(ownershipAccessService).assertCurrentUserOrAdmin(15L, principal);
    }

    @Test
    void markAsReadRequiresNotificationOwnership() {
        Notification notification = new Notification();
        User user = new User();
        user.setId(15L);
        notification.setUser(user);
        notification.setId(9L);
        when(notificationService.markAsRead(9L)).thenReturn(notification);

        notificationController.markAsRead(9L, principal);

        verify(ownershipAccessService).assertNotificationOwnerOrAdmin(9L, principal);
    }

    @Test
    void createNotificationAllowsOnlyOwnUserIdOrAdmin() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(15L);
        request.setTitle("title");
        request.setMessage("message");
        request.setType("INFO");

        Notification notification = new Notification();
        User user = new User();
        user.setId(15L);
        notification.setUser(user);
        notification.setTitle("title");
        notification.setMessage("message");
        notification.setType("INFO");
        when(notificationService.createNotification(15L, "title", "message", "INFO")).thenReturn(notification);

        Long responseUserId = notificationController.createNotification(request, principal).getBody().getUserId();

        assertThat(responseUserId).isEqualTo(15L);
        verify(ownershipAccessService).assertCurrentUserOrAdmin(15L, principal);
    }
}
