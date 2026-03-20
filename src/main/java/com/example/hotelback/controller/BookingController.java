package com.example.hotelback.controller;

import com.example.hotelback.dto.BookingResponse;
import com.example.hotelback.dto.CreateBookingRequest;
import com.example.hotelback.dto.UpdateBookingRequest;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final DtoMapper dtoMapper;

    public BookingController(BookingService bookingService, DtoMapper dtoMapper) {
        this.bookingService = bookingService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.ok(dtoMapper.toBookingResponse(bookingService.createBooking(dtoMapper.toBooking(request))));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings().stream().map(dtoMapper::toBookingResponse).toList());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId).stream().map(dtoMapper::toBookingResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(dtoMapper::toBookingResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id, @Valid @RequestBody UpdateBookingRequest request) {
        return ResponseEntity.ok(dtoMapper.toBookingResponse(bookingService.updateBooking(id, dtoMapper.toBooking(request))));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(dtoMapper.toBookingResponse(bookingService.confirmBooking(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(dtoMapper.toBookingResponse(bookingService.cancelBooking(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBookingById(id);
        return ResponseEntity.ok("Захиалга устгагдлаа");
    }
}
