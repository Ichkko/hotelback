package com.example.hotelback.service;

import com.example.hotelback.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {

    Room createRoom(Room room);

    List<Room> getAllRooms();

    Optional<Room> getRoomById(Long id);

    Room updateRoom(Long id, Room room);

    void deleteRoomById(Long id);
}
