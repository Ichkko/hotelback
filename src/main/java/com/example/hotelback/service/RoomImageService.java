package com.example.hotelback.service;

import com.example.hotelback.model.RoomImage;

import java.util.List;
import java.util.Optional;

public interface RoomImageService {

    RoomImage createRoomImage(RoomImage roomImage);

    List<RoomImage> getAllRoomImages();

    List<RoomImage> getRoomImagesByRoomId(Long roomId);

    Optional<RoomImage> getRoomImageById(Long id);

    RoomImage updateRoomImage(Long id, RoomImage roomImage);

    void deleteRoomImageById(Long id);
}
