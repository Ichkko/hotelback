package com.example.hotelback.service.impl;

import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Room;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.service.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    @Override
    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotel_Id(hotelId);
    }

    @Override
    public Room updateRoom(Long id, Room room) {
        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + id));
        existing.setRoomType(room.getRoomType());
        existing.setPrice(room.getPrice());
        existing.setCapacity(room.getCapacity());
        existing.setStatus(room.getStatus());

        existing.getDetails().clear();
        for (var detail : room.getDetails()) {
            detail.setRoom(existing);
            existing.getDetails().add(detail);
        }
        return roomRepository.save(existing);
    }

    @Override
    public void deleteRoomById(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + id);
        }
        roomRepository.deleteById(id);
    }

    @Override
    public List<Room> getAvailableRooms(Long hotelId, LocalDate checkin, LocalDate checkout) {
        if (checkin == null || checkout == null) {
            throw new IllegalArgumentException("Check-in, check-out хоёулаа заавал бөглөгдөнө");
        }
        if (!checkout.isAfter(checkin)) {
            throw new IllegalArgumentException("Check-out нь check-in-ээс хойшхи өдөр байх ёстой");
        }

        return roomRepository.findAvailableRoomsByHotelAndDates(
                hotelId,
                checkin,
                checkout,
                List.of(BookingStatus.NEW, BookingStatus.CONFIRMED, BookingStatus.PAID)
        );
    }
}
