package com.example.hotelback.repository;

import com.example.hotelback.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUser_Id(Long userId);

    Optional<Wishlist> findByUser_IdAndRoom_Id(Long userId, Long roomId);

    boolean existsByUser_IdAndRoom_Id(Long userId, Long roomId);
}
