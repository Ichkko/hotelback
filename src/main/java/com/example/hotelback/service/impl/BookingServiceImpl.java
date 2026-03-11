package com.example.hotelback.service.impl;

import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Booking;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.service.BookingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_Id(userId);
    }

    @Override
    public Booking updateBooking(Long id, Booking booking) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + id));
        existing.setCheckinDate(booking.getCheckinDate());
        existing.setCheckoutDate(booking.getCheckoutDate());
        existing.setStatus(booking.getStatus());
        return bookingRepository.save(existing);
    }

    @Override
    public void deleteBookingById(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + id);
        }
        bookingRepository.deleteById(id);
    }
}
