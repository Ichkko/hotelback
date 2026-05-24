package com.example.hotelback.repository;

import com.example.hotelback.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndReadFalse(Long userId);

    @org.springframework.data.jpa.repository.Query("select n.user.id from Notification n where n.id = :id")
    java.util.Optional<Long> findUserIdById(@org.springframework.data.repository.query.Param("id") Long id);

}
