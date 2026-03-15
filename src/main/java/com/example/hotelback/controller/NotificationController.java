package com.example.hotelback.controller;

import com.example.hotelback.dto.NotificationRequest;
import com.example.hotelback.dto.NotificationResponse;
import com.example.hotelback.model.Notification;
import com.example.hotelback.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        Notification notification = notificationService.createNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(NotificationResponse.fromEntity(notification));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getByUser(@PathVariable Long userId) {
        List<NotificationResponse> response = notificationService.getNotificationsByUserId(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(userId)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(NotificationResponse.fromEntity(notificationService.markAsRead(id)));
    }

    @PostMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("updated", notificationService.markAllAsReadByUserId(userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
