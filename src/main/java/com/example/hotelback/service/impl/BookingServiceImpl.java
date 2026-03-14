package com.example.hotelback.service.impl;

import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public Booking createBooking(Booking booking) {
        validateDates(booking.getCheckinDate(), booking.getCheckoutDate());

        if (booking.getRoom() == null || booking.getRoom().getId() == null) {
            throw new IllegalArgumentException("Өрөөний мэдээлэл дутуу байна");
        }

        // Давхцах booking байгаа эсэхийг шалгах
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                booking.getRoom().getId(),
                booking.getCheckinDate(),
                booking.getCheckoutDate(),
                List.of(BookingStatus.NEW, BookingStatus.CONFIRMED, BookingStatus.PAID)
        );
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Энэ хугацаанд өрөө аль хэдийнэ захиалгатай байна");
        }

        // Анхны төлөв
        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.NEW);
        }

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
    @Transactional
    public Booking updateBooking(Long id, Booking booking) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + id));

        validateDates(booking.getCheckinDate(), booking.getCheckoutDate());

        existing.setCheckinDate(booking.getCheckinDate());
        existing.setCheckoutDate(booking.getCheckoutDate());
        existing.setStatus(booking.getStatus());
        return bookingRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteBookingById(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + id);
        }
        bookingRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Booking confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + id));

        if (booking.getStatus() != BookingStatus.NEW) {
            throw new IllegalStateException("Зөвхөн NEW төлөвтэй захиалгыг CONFIRMED болгож болно");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + id));

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new IllegalStateException("PAID төлөвтэй захиалгыг цуцлах боломжгүй");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    private void validateDates(LocalDate checkin, LocalDate checkout) {
        if (checkin == null || checkout == null) {
            throw new IllegalArgumentException("Check-in, check-out хоёулаа заавал бөглөгдөнө");
        }
        if (!checkout.isAfter(checkin)) {
            throw new IllegalArgumentException("Check-out нь check-in-ээс хойшхи өдөр байх ёстой");
        }
        if (checkin.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Өнгөрсөн өдрөөр захиалга хийх боломжгүй");
        }
    }
}
