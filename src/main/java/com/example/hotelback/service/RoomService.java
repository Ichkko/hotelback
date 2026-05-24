package com.example.hotelback.service;

import com.example.hotelback.model.Room;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomService {

    Room createRoom(Room room, UserDetails principal);

    List<Room> getAllRooms();

    Optional<Room> getRoomById(Long id);

    List<Room> getRoomsByHotelId(Long hotelId);

    default List<Room> getRoomsByHotelId(Long hotelId, UserDetails principal) {
        return getRoomsByHotelId(hotelId);
    }

    Room updateRoom(Long id, Room room, UserDetails principal);

    void deleteRoomById(Long id, UserDetails principal);

    List<Room> getAvailableRooms(Long hotelId, LocalDate checkin, LocalDate checkout);
}
