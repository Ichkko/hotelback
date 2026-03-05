package com.example.hotelback.repository;

import com.example.hotelback.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    // Жишээ: нэрээр хайлт хийх
    List<Hotel> findByNameContainingIgnoreCase(String name);


}