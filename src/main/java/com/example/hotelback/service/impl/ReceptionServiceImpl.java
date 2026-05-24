package com.example.hotelback.service.impl;

import com.example.hotelback.dto.ReceptionDashboardResponse;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.HotelPermission;
import com.example.hotelback.model.Payment;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.RoomStatus;
import com.example.hotelback.model.RoomStatusHistory;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.repository.RoomStatusHistoryRepository;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.PaymentService;
import com.example.hotelback.service.ReceptionService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReceptionServiceImpl implements ReceptionService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomStatusHistoryRepository roomStatusHistoryRepository;
    private final PaymentService paymentService;
    private final OwnershipAccessService ownershipAccessService;

    public ReceptionServiceImpl(BookingRepository bookingRepository,
                                RoomRepository roomRepository,
                                RoomStatusHistoryRepository roomStatusHistoryRepository,
                                PaymentService paymentService,
                                OwnershipAccessService ownershipAccessService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.roomStatusHistoryRepository = roomStatusHistoryRepository;
        this.paymentService = paymentService;
        this.ownershipAccessService = ownershipAccessService;
    }

    @Override
    @Transactional
    public Booking checkIn(Long bookingId, UserDetails principal) {
        ownershipAccessService.assertBookingPermission(
                bookingId,
                principal,
                HotelPermission.BOOKING_UPDATE,
                "Та энэ захиалгад check-in хийх эрхгүй"
        );
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));

        validateCheckIn(booking);

        Room room = roomRepository.findByIdForUpdate(booking.getRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + booking.getRoom().getId()));
        room.setStatus(RoomStatus.UNAVAILABLE);
        saveStatusHistory(room, RoomStatus.UNAVAILABLE, LocalDate.now(), null, "Check-in booking ID=" + bookingId);
        roomRepository.save(room);
        return booking;
    }

    @Override
    @Transactional
    public Booking checkOut(Long bookingId, UserDetails principal) {
        ownershipAccessService.assertBookingPermission(
                bookingId,
                principal,
                HotelPermission.BOOKING_UPDATE,
                "Та энэ захиалгад check-out хийх эрхгүй"
        );
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + bookingId));

        validateCheckOut(booking);

        Room room = roomRepository.findByIdForUpdate(booking.getRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + booking.getRoom().getId()));
        room.setStatus(RoomStatus.AVAILABLE);
        saveStatusHistory(room, RoomStatus.AVAILABLE, LocalDate.now(), null, "Check-out booking ID=" + bookingId);
        roomRepository.save(room);
        return booking;
    }

    @Override
    @Transactional
    public Room updateRoomStatus(Long roomId, RoomStatus status, UserDetails principal) {
        ownershipAccessService.assertRoomPermission(
                roomId,
                principal,
                HotelPermission.BOOKING_UPDATE,
                "Та зөвхөн өөрийн буудлын staff хүрээнд өрөөг удирдах эрхтэй"
        );
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + roomId));
        room.setStatus(status);
        saveStatusHistory(room, status, LocalDate.now(), null, "Manual room status update");
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public RoomStatusHistory createRoomStatusPeriod(Long roomId,
                                                    RoomStatus status,
                                                    LocalDate startDate,
                                                    LocalDate endDate,
                                                    String note,
                                                    UserDetails principal) {
        ownershipAccessService.assertRoomPermission(
                roomId,
                principal,
                HotelPermission.BOOKING_UPDATE,
                "Та зөвхөн өөрийн буудлын staff хүрээнд өрөөг удирдах эрхтэй"
        );
        validateStatusPeriod(status, startDate, endDate);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + roomId));
        return saveStatusHistory(room, status, startDate, endDate, note);
    }

    @Override
    public List<RoomStatusHistory> getRoomStatusHistory(Long roomId, UserDetails principal) {
        ownershipAccessService.assertRoomPermission(
                roomId,
                principal,
                HotelPermission.BOOKING_VIEW,
                "Та энэ өрөөний төлөвийн түүхийг харах эрхгүй"
        );
        return roomStatusHistoryRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional
    public Payment collectPayment(Long bookingId, BigDecimal amount, String paymentMethod, UserDetails principal) {
        ownershipAccessService.assertBookingPermission(
                bookingId,
                principal,
                HotelPermission.PAYMENT_MANAGE,
                "Та энэ захиалгын төлбөрийг удирдах эрхгүй"
        );
        Payment payment = new Payment();
        Booking booking = new Booking();
        booking.setId(bookingId);
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        return paymentService.createPayment(payment);
    }

    @Override
    public ReceptionDashboardResponse getDashboard(Long hotelId, LocalDate date, UserDetails principal) {
        ownershipAccessService.assertHotelPermission(
                hotelId,
                principal,
                HotelPermission.BOOKING_VIEW,
                "Та энэ буудлын dashboard-ийг харах эрхгүй"
        );
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

    private RoomStatusHistory saveStatusHistory(Room room,
                                                RoomStatus status,
                                                LocalDate startDate,
                                                LocalDate endDate,
                                                String note) {
        validateStatusPeriod(status, startDate, endDate);
        RoomStatusHistory history = new RoomStatusHistory();
        history.setRoom(room);
        history.setStatus(status);
        history.setStartDate(startDate);
        history.setEndDate(endDate);
        history.setNote(note);
        return roomStatusHistoryRepository.save(history);
    }

    private void validateStatusPeriod(RoomStatus status, LocalDate startDate, LocalDate endDate) {
        if (status == null) {
            throw new IllegalArgumentException("Өрөөний төлөв заавал байна");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Эхлэх огноо заавал байна");
        }
        if (endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Дуусах огноо эхлэх огнооноос хойш байх ёстой");
        }
    }
}
