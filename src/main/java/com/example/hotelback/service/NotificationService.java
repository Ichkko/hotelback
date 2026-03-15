package com.example.hotelback.service;

import com.example.hotelback.model.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(Long userId, String title, String message, String type);

    List<Notification> getNotificationsByUserId(Long userId);

    Notification markAsRead(Long notificationId);

    int markAllAsReadByUserId(Long userId);

    long getUnreadCount(Long userId);

    void deleteNotification(Long notificationId);
}
