package com.example.hotelback.service.impl;

import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
        validateGuestCount(booking);

        if (booking.getRoom() == null || booking.getRoom().getId() == null) {
            throw new IllegalArgumentException("Өрөөний мэдээлэл дутуу байна");
        }

        if (booking.getRoom().getPrice() == null) {
            throw new IllegalArgumentException("Өрөөний үнэ дутуу байна");
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

        fillCalculatedFields(booking);

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
        validateGuestCount(booking);

        // Шинэ огноонууд өөр booking-үүдтэй давхцаж байгаа эсэхийг шалгах
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                existing.getRoom().getId(),
                booking.getCheckinDate(),
                booking.getCheckoutDate(),
                List.of(BookingStatus.NEW, BookingStatus.CONFIRMED, BookingStatus.PAID)
        );
        boolean hasConflict = overlaps.stream().anyMatch(b -> !b.getId().equals(id));
        if (hasConflict) {
            throw new IllegalStateException("Энэ хугацаанд өрөө аль хэдийнэ өөр захиалгатай байна");
        }

        existing.setCheckinDate(booking.getCheckinDate());
        existing.setCheckoutDate(booking.getCheckoutDate());
        existing.setFirstName(booking.getFirstName());
        existing.setLastName(booking.getLastName());
        existing.setEmail(booking.getEmail());
        existing.setPhone(booking.getPhone());
        existing.setGuestCount(booking.getGuestCount());
        existing.setSpecialRequests(booking.getSpecialRequests());

        if (booking.getRoomPrice() != null) {
            existing.setRoomPrice(booking.getRoomPrice());
        }

        fillCalculatedFields(existing);
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

    private void validateGuestCount(Booking booking) {
        if (booking.getGuestCount() == null || booking.getGuestCount() < 1) {
            throw new IllegalArgumentException("Зочдын тоо 1-ээс багагүй байх ёстой");
        }

        Integer capacity = booking.getRoom() != null ? booking.getRoom().getCapacity() : null;
        if (capacity != null && booking.getGuestCount() > capacity) {
            throw new IllegalArgumentException("Зочдын тоо өрөөний багтаамжаас хэтэрсэн байна");
        }
    }

    private void fillCalculatedFields(Booking booking) {
        int nights = (int) ChronoUnit.DAYS.between(booking.getCheckinDate(), booking.getCheckoutDate());
        booking.setNights(nights);

        BigDecimal nightlyPrice = booking.getRoomPrice() != null
                ? booking.getRoomPrice()
                : BigDecimal.valueOf(booking.getRoom().getPrice());
        booking.setRoomPrice(nightlyPrice);

        BigDecimal subtotal = nightlyPrice.multiply(BigDecimal.valueOf(nights));
        BigDecimal serviceFee = subtotal.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.FLOOR);
        booking.setServiceFee(serviceFee);
        booking.setTotalPrice(subtotal.add(serviceFee));

        if (booking.getBookingNumber() == null || booking.getBookingNumber().isBlank()) {
            booking.setBookingNumber(String.format(
                    "BK-%s-%s-%s-%s",
                    booking.getRoom().getHotelId(),
                    booking.getRoom().getId(),
                    booking.getCheckinDate(),
                    booking.getCheckoutDate()
            ));
        }
    }
}
