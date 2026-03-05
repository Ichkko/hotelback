package com.example.hotelback.service.impl;

import com.example.hotelback.model.RoomImage;
import com.example.hotelback.repository.RoomImageRepository;
import com.example.hotelback.service.RoomImageService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomImageServiceImpl implements RoomImageService {

    private final RoomImageRepository roomImageRepository;

    public RoomImageServiceImpl(RoomImageRepository roomImageRepository) {
        this.roomImageRepository = roomImageRepository;
    }

    @Override
    public RoomImage createRoomImage(RoomImage roomImage) {
        return roomImageRepository.save(roomImage);
    }

    @Override
    public List<RoomImage> getAllRoomImages() {
        return roomImageRepository.findAll();
    }

    @Override
    public List<RoomImage> getRoomImagesByRoomId(Long roomId) {
        return roomImageRepository.findByRoomId(roomId);
    }

    @Override
    public Optional<RoomImage> getRoomImageById(Long id) {
        return roomImageRepository.findById(id);
    }

    @Override
    public RoomImage updateRoomImage(Long id, RoomImage roomImage) {
        RoomImage existing = roomImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room image not found"));
        BeanUtils.copyProperties(roomImage, existing, "id", "createdAt", "updatedAt");
        return roomImageRepository.save(existing);
    }

    @Override
    public void deleteRoomImageById(Long id) {
        roomImageRepository.deleteById(id);
    }
}
