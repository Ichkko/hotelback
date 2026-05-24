package com.example.hotelback.controller;

import com.example.hotelback.dto.CreateHotelRequest;
import com.example.hotelback.dto.HotelResponse;
import com.example.hotelback.dto.RoomResponse;
import com.example.hotelback.dto.UpdateHotelRequest;
import com.example.hotelback.dto.UserSummaryResponse;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.User;
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
        if (!ownershipAccessService.isAdmin(principal)) {
            User owner = new User();
            owner.setId(ownershipAccessService.resolveCurrentUserId(principal));
            hotel.setOwners(List.of(owner));
        }
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.createHotel(hotel)));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<HotelResponse>> createHotels(@Valid @RequestBody List<CreateHotelRequest> requests,
                                                            @AuthenticationPrincipal UserDetails principal) {
        Long currentUserId = ownershipAccessService.resolveCurrentUserId(principal);
        boolean admin = ownershipAccessService.isAdmin(principal);

        List<Hotel> hotels = requests.stream().map(request -> {
            Hotel hotel = dtoMapper.toHotel(request);
            if (!admin) {
                User owner = new User();
                owner.setId(currentUserId);
                hotel.setOwners(List.of(owner));
            }
            return hotel;
        }).toList();

        return ResponseEntity.ok(hotelService.createHotels(hotels)
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
        return ResponseEntity.ok(hotelService.getHotelsByOwnerId(currentUserId)
                .stream().map(dtoMapper::toHotelResponse).toList());
    }

    @GetMapping("/my-access")
    public ResponseEntity<List<HotelResponse>> getMyAccessibleHotels(@AuthenticationPrincipal UserDetails principal) {
        if (ownershipAccessService.isAdmin(principal)) {
            return ResponseEntity.ok(hotelService.getAllHotels().stream().map(dtoMapper::toHotelResponse).toList());
        }

        Long currentUserId = ownershipAccessService.resolveCurrentUserId(principal);
        return ResponseEntity.ok(hotelService.getAccessibleHotelsByUserId(currentUserId)
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

    @GetMapping("/{id}/receptionists")
    public ResponseEntity<List<UserSummaryResponse>> getHotelReceptionists(@PathVariable Long id,
                                                                           @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertHotelStaffOrAdmin(id, principal);
        return ResponseEntity.ok(hotelService.getReceptionistsByHotelId(id)
                .stream().map(dtoMapper::toUserSummaryResponse).toList());
    }

    @PostMapping("/{id}/receptionists/{userId}")
    public ResponseEntity<HotelResponse> addReceptionist(@PathVariable Long id,
                                                         @PathVariable Long userId,
                                                         @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertHotelOwnerOrAdmin(id, principal);
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.addReceptionist(id, userId)));
    }

    @DeleteMapping("/{id}/receptionists/{userId}")
    public ResponseEntity<HotelResponse> removeReceptionist(@PathVariable Long id,
                                                            @PathVariable Long userId,
                                                            @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertHotelOwnerOrAdmin(id, principal);
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.removeReceptionist(id, userId)));
    }
}
