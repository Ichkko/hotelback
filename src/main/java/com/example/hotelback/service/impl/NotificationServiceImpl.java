package com.example.hotelback.service.impl;

import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Notification;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.NotificationRepository;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Notification createNotification(Long userId, String title, String message, String type) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Гарчиг хоосон байж болохгүй");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Мэдэгдлийн текст хоосон байж болохгүй");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Хэрэглэгч олдсонгүй: ID=" + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Мэдэгдэл олдсонгүй: ID=" + notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }
        return notification;
    }

    @Override
    @Transactional
    public int markAllAsReadByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        int updated = 0;

        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                updated++;
            }
        }

        if (updated > 0) {
            notificationRepository.saveAll(notifications);
        }

        return updated;
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Мэдэгдэл олдсонгүй: ID=" + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }
}
