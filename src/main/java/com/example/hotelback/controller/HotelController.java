package com.example.hotelback.controller;

import com.example.hotelback.dto.CreateHotelRequest;
import com.example.hotelback.dto.HotelResponse;
import com.example.hotelback.dto.RoomResponse;
import com.example.hotelback.dto.UpdateHotelRequest;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.service.HotelService;
import com.example.hotelback.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final DtoMapper dtoMapper;

    public HotelController(HotelService hotelService, RoomService roomService, DtoMapper dtoMapper) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@Valid @RequestBody CreateHotelRequest request) {
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.createHotel(dtoMapper.toHotel(request))));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<HotelResponse>> createHotels(@Valid @RequestBody List<CreateHotelRequest> requests) {
        return ResponseEntity.ok(hotelService.createHotels(requests.stream().map(dtoMapper::toHotel).toList())
                .stream().map(dtoMapper::toHotelResponse).toList());
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels().stream().map(dtoMapper::toHotelResponse).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> searchHotels(@RequestParam String name) {
        return ResponseEntity.ok(hotelService.searchHotelsByName(name).stream().map(dtoMapper::toHotelResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        return hotelService.getHotelById(id)
                .map(dtoMapper::toHotelResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<RoomResponse>> getHotelRooms(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomsByHotelId(id).stream().map(dtoMapper::toRoomResponse).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long id, @Valid @RequestBody UpdateHotelRequest request) {
        var hotel = dtoMapper.toHotel(request);
        hotel.setId(id);
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.updateHotel(hotel)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotelById(id);
        return ResponseEntity.ok("Зочид буудал устгагдлаа");
    }
}
