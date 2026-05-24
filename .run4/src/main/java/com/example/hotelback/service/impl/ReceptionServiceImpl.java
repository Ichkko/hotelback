package com.example.hotelback.service.impl;

import com.example.hotelback.dto.ReceptionDashboardResponse;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Payment;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.RoomStatus;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.service.PaymentService;
import com.example.hotelback.service.ReceptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReceptionServiceImpl implements ReceptionService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final PaymentService paymentService;

    public ReceptionServiceImpl(BookingRepository bookingRepository,
                                RoomRepository roomRepository,
                                PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    public Booking checkIn(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));

        validateCheckIn(booking);

        Room room = roomRepository.findByIdForUpdate(booking.getRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + booking.getRoom().getId()));
        room.setStatus(RoomStatus.UNAVAILABLE);
        roomRepository.save(room);
        return booking;
    }

    @Override
    @Transactional
    public Booking checkOut(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));

        validateCheckOut(booking);

        Room room = roomRepository.findByIdForUpdate(booking.getRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + booking.getRoom().getId()));
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);
        return booking;
    }

    @Override
    @Transactional
    public Room updateRoomStatus(Long roomId, RoomStatus status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + roomId));
        room.setStatus(status);
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public Payment collectPayment(Long bookingId, BigDecimal amount, String paymentMethod) {
        Payment payment = new Payment();
        Booking booking = new Booking();
        booking.setId(bookingId);
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        return paymentService.createPayment(payment);
    }

    @Override
    public ReceptionDashboardResponse getDashboard(Long hotelId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
        List<Booking> bookings = bookingRepository.findByHotelId(hotelId);

        long arrivalsToday = bookings.stream()
                .filter(booking -> isOperationalBooking(booking.getStatus()))
                .filter(booking -> targetDate.equals(booking.getCheckinDate()))
                .count();
        long departuresToday = bookings.stream()
                .filter(booking -> isOperationalBooking(booking.getStatus()))
                .filter(booking -> targetDate.equals(booking.getCheckoutDate()))
                .count();
        long activeStays = bookings.stream()
                .filter(booking -> isOperationalBooking(booking.getStatus()))
                .filter(booking -> !targetDate.isBefore(booking.getCheckinDate()))
                .filter(booking -> targetDate.isBefore(booking.getCheckoutDate()))
                .count();

        return ReceptionDashboardResponse.builder()
                .hotelId(hotelId)
                .date(targetDate)
                .totalRooms(rooms.size())
                .availableRooms(rooms.stream().filter(room -> room.getStatus() == RoomStatus.AVAILABLE).count())
                .unavailableRooms(rooms.stream().filter(room -> room.getStatus() == RoomStatus.UNAVAILABLE).count())
                .maintenanceRooms(rooms.stream().filter(room -> room.getStatus() == RoomStatus.MAINTENANCE).count())
                .arrivalsToday(arrivalsToday)
                .departuresToday(departuresToday)
                .activeStays(activeStays)
                .pendingBookings(bookings.stream().filter(booking -> booking.getStatus() == BookingStatus.NEW).count())
                .confirmedBookings(bookings.stream().filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED).count())
                .paidBookings(bookings.stream().filter(booking -> booking.getStatus() == BookingStatus.PAID).count())
                .build();
    }

    private void validateCheckIn(Booking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PAID) {
            throw new IllegalStateException("Зөвхөн CONFIRMED эсвэл PAID захиалгаар check-in хийж болно");
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(booking.getCheckinDate()) || !today.isBefore(booking.getCheckoutDate())) {
            throw new IllegalStateException("Check-in хийх өдөр эсвэл хугацаа тохирохгүй байна");
        }
    }

    private void validateCheckOut(Booking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PAID) {
            throw new IllegalStateException("Зөвхөн CONFIRMED эсвэл PAID захиалгаар check-out хийж болно");
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(booking.getCheckinDate())) {
            throw new IllegalStateException("Check-in эхлээгүй захиалгаар check-out хийж болохгүй");
        }
    }

    private boolean isOperationalBooking(BookingStatus status) {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.PAID;
    }
}
