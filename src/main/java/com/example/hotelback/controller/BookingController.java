package com.example.hotelback.controller;

import com.example.hotelback.dto.BookingResponse;
import com.example.hotelback.dto.CreateBookingRequest;
import com.example.hotelback.dto.UpdateBookingRequest;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final DtoMapper dtoMapper;
    private final OwnershipAccessService ownershipAccessService;

    public BookingController(BookingService bookingService,
                             DtoMapper dtoMapper,
                             OwnershipAccessService ownershipAccessService) {
        this.bookingService = bookingService;
        this.dtoMapper = dtoMapper;
        this.ownershipAccessService = ownershipAccessService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request,
                                                         @AuthenticationPrincipal UserDetails principal) {
        Long effectiveUserId = request.getUserId() != null
                ? request.getUserId()
                : ownershipAccessService.resolveCurrentUserId(principal);
        ownershipAccessService.assertCurrentUserOrAdmin(effectiveUserId, principal);
        request.setUserId(effectiveUserId);
        return ResponseEntity.ok(dtoMapper.toBookingResponse(bookingService.createBooking(dtoMapper.toBooking(request))));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings(@AuthenticationPrincipal UserDetails principal) {
        List<BookingResponse> response = ownershipAccessService.isAdmin(principal)
                ? bookingService.getAllBookings().stream().map(dtoMapper::toBookingResponse).toList()
                : bookingService.getBookingsByUserId(ownershipAccessService.resolveCurrentUserId(principal))
                .stream().map(dtoMapper::toBookingResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable Long userId,
                                                                   @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertCurrentUserOrAdmin(userId, principal);
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId).stream().map(dtoMapper::toBookingResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingOwnerOrAdmin(id, principal);
        return bookingService.getBookingById(id)
                .map(dtoMapper::toBookingResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateBookingRequest request,
                                                         @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingOwnerOrAdmin(id, principal);
        return ResponseEntity.ok(dtoMapper.toBookingResponse(bookingService.updateBooking(id, dtoMapper.toBooking(request))));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingOwnerOrAdmin(id, principal);
        return ResponseEntity.ok(dtoMapper.toBookingResponse(bookingService.confirmBooking(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingOwnerOrAdmin(id, principal);
        return ResponseEntity.ok(dtoMapper.toBookingResponse(bookingService.cancelBooking(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertBookingOwnerOrAdmin(id, principal);
        bookingService.deleteBookingById(id);
        return ResponseEntity.ok("Захиалга устгагдлаа");
    }
}
