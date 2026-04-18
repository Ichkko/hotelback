package com.example.hotelback.controller;

import com.example.hotelback.dto.CreateHotelRequest;
import com.example.hotelback.dto.HotelResponse;
import com.example.hotelback.dto.RoomResponse;
import com.example.hotelback.dto.UpdateHotelRequest;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.HotelService;
import com.example.hotelback.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final DtoMapper dtoMapper;
    private final OwnershipAccessService ownershipAccessService;

    public HotelController(HotelService hotelService,
                           RoomService roomService,
                           DtoMapper dtoMapper,
                           OwnershipAccessService ownershipAccessService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.dtoMapper = dtoMapper;
        this.ownershipAccessService = ownershipAccessService;
    }

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@Valid @RequestBody CreateHotelRequest request,
                                                     @AuthenticationPrincipal UserDetails principal) {
        Hotel hotel = dtoMapper.toHotel(request);
        Long ownerUserId = ownershipAccessService.isAdmin(principal)
                ? request.getOwnerUserId()
                : ownershipAccessService.resolveCurrentUserId(principal);
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.createHotel(hotel, ownerUserId)));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<HotelResponse>> createHotels(@Valid @RequestBody List<CreateHotelRequest> requests,
                                                            @AuthenticationPrincipal UserDetails principal) {
        boolean admin = ownershipAccessService.isAdmin(principal);
        Long currentUserId = admin ? null : ownershipAccessService.resolveCurrentUserId(principal);

        List<Hotel> hotels = requests.stream().map(dtoMapper::toHotel).toList();
        Long ownerUserId = admin ? null : currentUserId;

        return ResponseEntity.ok(hotelService.createHotels(hotels, ownerUserId)
                .stream().map(dtoMapper::toHotelResponse).toList());
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels().stream().map(dtoMapper::toHotelResponse).toList());
    }

    @GetMapping("/my")
    public ResponseEntity<List<HotelResponse>> getMyHotels(@AuthenticationPrincipal UserDetails principal) {
        if (ownershipAccessService.isAdmin(principal)) {
            return ResponseEntity.ok(hotelService.getAllHotels().stream().map(dtoMapper::toHotelResponse).toList());
        }

        Long currentUserId = ownershipAccessService.resolveCurrentUserId(principal);
        return ResponseEntity.ok(hotelService.getHotelsByUserId(currentUserId)
                .stream().map(dtoMapper::toHotelResponse).toList());
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
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateHotelRequest request,
                                                     @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertHotelOwnerOrAdmin(id, principal);
        var hotel = dtoMapper.toHotel(request);
        hotel.setId(id);
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.updateHotel(hotel)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHotel(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertHotelOwnerOrAdmin(id, principal);
        hotelService.deleteHotelById(id);
        return ResponseEntity.ok("Зочид буудал устгагдлаа");
    }
}
