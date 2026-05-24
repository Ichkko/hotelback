package com.example.hotelback.service.impl;

import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.HotelPermission;
import com.example.hotelback.model.Room;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.RoomService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final OwnershipAccessService ownershipAccessService;

    public RoomServiceImpl(RoomRepository roomRepository,
                           OwnershipAccessService ownershipAccessService) {
        this.roomRepository = roomRepository;
        this.ownershipAccessService = ownershipAccessService;
    }

    @Override
    public Room createRoom(Room room, UserDetails principal) {
        ownershipAccessService.assertHotelPermission(
                room.getHotelId(),
                principal,
                HotelPermission.ROOM_MANAGE,
                "Та зөвхөн өөрийн буудлын өрөөг удирдах эрхтэй"
        );
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
    public List<Room> getRoomsByHotelId(Long hotelId, UserDetails principal) {
        ownershipAccessService.assertHotelAnyPermission(
                hotelId,
                principal,
                List.of(HotelPermission.ROOM_MANAGE, HotelPermission.HOTEL_UPDATE),
                "Та энэ буудлын өрөөнүүдийг харах эрхгүй"
        );
        return roomRepository.findByHotel_Id(hotelId);
    }

    @Override
    public Room updateRoom(Long id, Room room, UserDetails principal) {
        ownershipAccessService.assertRoomPermission(
                id,
                principal,
                HotelPermission.ROOM_MANAGE,
                "Та зөвхөн өөрийн буудлын өрөөг удирдах эрхтэй"
        );
        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + id));
        existing.setRoomType(room.getRoomType());
        existing.setPrice(room.getPrice());
        existing.setCapacity(room.getCapacity());
        existing.setRoomNumber(room.getRoomNumber());
        existing.setFloor(room.getFloor());
        existing.setWing(room.getWing());
        existing.setSection(room.getSection());
        existing.setPositionX(room.getPositionX());
        existing.setPositionY(room.getPositionY());
        existing.setStatus(room.getStatus());

        existing.getDetails().clear();
        for (var detail : room.getDetails()) {
            detail.setRoom(existing);
            existing.getDetails().add(detail);
        }

        existing.setRoomDetails(room.getRoomDetails());

        return roomRepository.save(existing);
    }

    @Override
    public void deleteRoomById(Long id, UserDetails principal) {
        ownershipAccessService.assertRoomPermission(
                id,
                principal,
                HotelPermission.ROOM_MANAGE,
                "Та зөвхөн өөрийн буудлын өрөөг удирдах эрхтэй"
        );
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
