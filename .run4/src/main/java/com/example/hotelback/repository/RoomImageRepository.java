package com.example.hotelback.repository;

import com.example.hotelback.model.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    @Query(value = "select ri from RoomImage ri where ri.room.id = :roomId", countQuery = "select count(ri) from RoomImage ri where ri.room.id = :roomId")
    List<RoomImage> findByRoomId(@Param("roomId") Long roomId);
}
