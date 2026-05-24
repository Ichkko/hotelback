package com.example.hotelback.controller;

import com.example.hotelback.dto.AssignHotelRoleRequest;
import com.example.hotelback.dto.CreateHotelRequest;
import com.example.hotelback.dto.HotelReportSummaryResponse;
import com.example.hotelback.dto.HotelResponse;
import com.example.hotelback.dto.HotelStaffResponse;
import com.example.hotelback.dto.RoomAvailabilityResponse;
import com.example.hotelback.dto.RoomResponse;
import com.example.hotelback.dto.UpdateHotelRequest;
import com.example.hotelback.dto.UserSummaryResponse;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.HotelRole;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.PaymentRepository;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.AvailabilityService;
import com.example.hotelback.service.HotelService;
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
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final AvailabilityService availabilityService;
    private final DtoMapper dtoMapper;
    private final OwnershipAccessService ownershipAccessService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;

    public HotelController(HotelService hotelService,
                           RoomService roomService,
                           AvailabilityService availabilityService,
                           DtoMapper dtoMapper,
                           OwnershipAccessService ownershipAccessService,
                           BookingRepository bookingRepository,
                           PaymentRepository paymentRepository,
                           RoomRepository roomRepository) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.availabilityService = availabilityService;
        this.dtoMapper = dtoMapper;
        this.ownershipAccessService = ownershipAccessService;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.roomRepository = roomRepository;
    }

    // ── Hotel CRUD ────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@Valid @RequestBody CreateHotelRequest request,
                                                     @AuthenticationPrincipal UserDetails principal) {
        Hotel hotel = dtoMapper.toHotel(request);
        Long ownerUserId = ownershipAccessService.isAdmin(principal)
                ? request.getOwnerId()
                : ownershipAccessService.resolveCurrentUserId(principal);
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.createHotel(hotel, ownerUserId, principal)));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<HotelResponse>> createHotels(@Valid @RequestBody List<CreateHotelRequest> requests,
                                                            @AuthenticationPrincipal UserDetails principal) {
        boolean admin = ownershipAccessService.isAdmin(principal);
        Long ownerUserId = admin ? null : ownershipAccessService.resolveCurrentUserId(principal);
        List<Hotel> hotels = requests.stream().map(dtoMapper::toHotel).toList();
        return ResponseEntity.ok(hotelService.createHotels(hotels, ownerUserId, principal)
                .stream().map(dtoMapper::toHotelResponse).toList());
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels().stream().map(dtoMapper::toHotelResponse).toList());
    }

    @GetMapping("/my")
    public ResponseEntity<List<HotelResponse>> getMyHotels(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(hotelService.getHotelsByOwnerId(principal)
                .stream().map(dtoMapper::toHotelResponse).toList());
    }

    @GetMapping("/my-access")
    public ResponseEntity<List<HotelResponse>> getMyAccessibleHotels(@AuthenticationPrincipal UserDetails principal) {
        Long currentUserId = ownershipAccessService.resolveCurrentUserId(principal);

        return ResponseEntity.ok(hotelService.getAccessibleHotelsByUserId(principal)
                .stream()
                .map(hotel -> dtoMapper.toHotelAccessResponse(hotel, currentUserId))
                .toList());
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

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<RoomAvailabilityResponse>> getHotelAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(availabilityService.getHotelAvailability(id, from, to));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateHotelRequest request,
                                                     @AuthenticationPrincipal UserDetails principal) {
        var hotel = dtoMapper.toHotel(request);
        hotel.setId(id);
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.updateHotel(hotel, principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHotel(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails principal) {
        hotelService.deleteHotelById(id, principal);
        return ResponseEntity.ok("Зочид буудал устгагдлаа");
    }

    // ── Staff management ──────────────────────────────────────────────────────

    @GetMapping("/{id}/staff")
    public ResponseEntity<List<HotelStaffResponse>> getHotelStaff(@PathVariable Long id,
                                                                   @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                hotelService.getStaffByHotelId(id, principal).stream()
                        .map(hur -> HotelStaffResponse.builder()
                                .userId(hur.getUser().getId())
                                .name(hur.getUser().getName())
                                .email(hur.getUser().getEmail())
                                .hotelRole(hur.getRole().name())
                                .build())
                        .toList());
    }

    @PostMapping("/{id}/staff")
    public ResponseEntity<HotelResponse> assignStaff(@PathVariable Long id,
                                                      @Valid @RequestBody AssignHotelRoleRequest request,
                                                      @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toHotelResponse(
                hotelService.assignStaff(id, request.getUserId(), request.getRole(), principal)));
    }

    @PutMapping("/{id}/staff/{userId}")
    public ResponseEntity<HotelResponse> updateStaffRole(@PathVariable Long id,
                                                          @PathVariable Long userId,
                                                          @RequestParam HotelRole role,
                                                          @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toHotelResponse(
                hotelService.updateStaffRole(id, userId, role, principal)));
    }

    @DeleteMapping("/{id}/staff/{userId}")
    public ResponseEntity<HotelResponse> removeStaff(@PathVariable Long id,
                                                       @PathVariable Long userId,
                                                       @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toHotelResponse(
                hotelService.removeStaff(id, userId, principal)));
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}/reports/summary")
    public ResponseEntity<HotelReportSummaryResponse> getReportSummary(@PathVariable Long id,
                                                                        @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertHotelStaffOrAdmin(id, principal);

        long totalRooms = roomRepository.countByHotelId(id);
        long occupied   = bookingRepository.countOccupiedRoomsToday(
                id,
                List.of(BookingStatus.NEW, BookingStatus.CONFIRMED, BookingStatus.PAID),
                LocalDate.now());

        HotelReportSummaryResponse summary = HotelReportSummaryResponse.builder()
                .hotelId(id)
                .totalBookings(
                        bookingRepository.countByHotelIdAndStatus(id, BookingStatus.NEW)
                        + bookingRepository.countByHotelIdAndStatus(id, BookingStatus.CONFIRMED)
                        + bookingRepository.countByHotelIdAndStatus(id, BookingStatus.PAID)
                        + bookingRepository.countByHotelIdAndStatus(id, BookingStatus.CANCELLED))
                .newBookings(bookingRepository.countByHotelIdAndStatus(id, BookingStatus.NEW))
                .confirmedBookings(bookingRepository.countByHotelIdAndStatus(id, BookingStatus.CONFIRMED))
                .paidBookings(bookingRepository.countByHotelIdAndStatus(id, BookingStatus.PAID))
                .cancelledBookings(bookingRepository.countByHotelIdAndStatus(id, BookingStatus.CANCELLED))
                .totalRevenue(bookingRepository.sumTotalPriceByHotelIdAndStatus(id, BookingStatus.PAID))
                .totalServiceFee(bookingRepository.sumServiceFeeByHotelIdAndStatus(id, BookingStatus.PAID))
                .totalPaymentsReceived(paymentRepository.sumSuccessfulPaymentsByHotelId(id))
                .totalRooms(totalRooms)
                .occupiedRooms(occupied)
                .build();

        return ResponseEntity.ok(summary);
    }

    // ── Deprecated receptionist shortcuts ────────────────────────────────────

    /**
     * @deprecated Use GET /{id}/staff instead. Will be removed in a future release.
     */
    @Deprecated
    @GetMapping("/{id}/receptionists")
    public ResponseEntity<List<UserSummaryResponse>> getHotelReceptionists(@PathVariable Long id,
                                                                           @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(hotelService.getReceptionistsByHotelId(id, principal)
                .stream().map(dtoMapper::toUserSummaryResponse).toList());
    }

    /**
     * @deprecated Use POST /{id}/staff with role=RECEPTION instead. Will be removed in a future release.
     */
    @Deprecated
    @PostMapping("/{id}/receptionists/{userId}")
    public ResponseEntity<HotelResponse> addReceptionist(@PathVariable Long id,
                                                         @PathVariable Long userId,
                                                         @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.addReceptionist(id, userId, principal)));
    }

    /**
     * @deprecated Use DELETE /{id}/staff/{userId} instead. Will be removed in a future release.
     */
    @Deprecated
    @DeleteMapping("/{id}/receptionists/{userId}")
    public ResponseEntity<HotelResponse> removeReceptionist(@PathVariable Long id,
                                                            @PathVariable Long userId,
                                                            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dtoMapper.toHotelResponse(hotelService.removeReceptionist(id, userId, principal)));
    }
}
