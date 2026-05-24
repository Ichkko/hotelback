package com.example.hotelback.controller;

import com.example.hotelback.model.RoomImage;
import com.example.hotelback.service.RoomImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-images")
public class RoomImageController {

    private final RoomImageService roomImageService;

    public RoomImageController(RoomImageService roomImageService) {
        this.roomImageService = roomImageService;
    }

    @PostMapping
    public ResponseEntity<RoomImage> createRoomImage(@RequestBody RoomImage roomImage) {
        return ResponseEntity.ok(roomImageService.createRoomImage(roomImage));
    }

    @GetMapping
    public ResponseEntity<List<RoomImage>> getAllRoomImages() {
        return ResponseEntity.ok(roomImageService.getAllRoomImages());
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<RoomImage>> getRoomImagesByRoomId(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomImageService.getRoomImagesByRoomId(roomId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomImage> getRoomImageById(@PathVariable Long id) {
        return roomImageService.getRoomImageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomImage> updateRoomImage(@PathVariable Long id, @RequestBody RoomImage roomImage) {
        return ResponseEntity.ok(roomImageService.updateRoomImage(id, roomImage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoomImage(@PathVariable Long id) {
        roomImageService.deleteRoomImageById(id);
        return ResponseEntity.ok("RoomImage with ID " + id + " deleted successfully");
    }
}
