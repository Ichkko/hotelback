package com.example.hotelback.service;

import com.example.hotelback.model.Room;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomService {

    Room createRoom(Room room);

    List<Room> getAllRooms();

    Optional<Room> getRoomById(Long id);

    List<Room> getRoomsByHotelId(Long hotelId);

    Room updateRoom(Long id, Room room);

    void deleteRoomById(Long id);

    List<Room> getAvailableRooms(Long hotelId, LocalDate checkin, LocalDate checkout);
}
