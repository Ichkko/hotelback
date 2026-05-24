package com.example.hotelback.controller;

import com.example.hotelback.dto.CreateRoomRequest;
import com.example.hotelback.dto.RoomResponse;
import com.example.hotelback.dto.UpdateRoomRequest;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final DtoMapper dtoMapper;
    private final OwnershipAccessService ownershipAccessService;

    public RoomController(RoomService roomService,
                          DtoMapper dtoMapper,
                          OwnershipAccessService ownershipAccessService) {
        this.roomService = roomService;
        this.dtoMapper = dtoMapper;
        this.ownershipAccessService = ownershipAccessService;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request,
                                                   @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertHotelOwnerOrAdmin(request.getHotelId(), principal);
        return ResponseEntity.ok(dtoMapper.toRoomResponse(roomService.createRoom(dtoMapper.toRoom(request))));
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms().stream().map(dtoMapper::toRoomResponse).toList());
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getRoomsByHotelId(hotelId).stream().map(dtoMapper::toRoomResponse).toList());
    }

    @GetMapping("/hotel/{hotelId}/available")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout
    ) {
        return ResponseEntity.ok(roomService.getAvailableRooms(hotelId, checkin, checkout).stream().map(dtoMapper::toRoomResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(dtoMapper::toRoomResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateRoomRequest request,
                                                   @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertRoomHotelOwnerOrAdmin(id, principal);
        return ResponseEntity.ok(dtoMapper.toRoomResponse(roomService.updateRoom(id, dtoMapper.toRoom(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertRoomHotelOwnerOrAdmin(id, principal);
        roomService.deleteRoomById(id);
        return ResponseEntity.ok("Өрөө устгагдлаа");
    }
}
