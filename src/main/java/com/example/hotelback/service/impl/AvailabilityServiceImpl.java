package com.example.hotelback.service.impl;

import com.example.hotelback.dto.AvailabilityDayResponse;
import com.example.hotelback.dto.RoomAvailabilityResponse;
import com.example.hotelback.model.AvailabilityStatus;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.RoomStatus;
import com.example.hotelback.model.RoomStatusHistory;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.repository.RoomStatusHistoryRepository;
import com.example.hotelback.service.AvailabilityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final int MAX_RANGE_DAYS = 370;
    private static final List<BookingStatus> BLOCKING_BOOKING_STATUSES =
            List.of(BookingStatus.NEW, BookingStatus.CONFIRMED, BookingStatus.PAID);

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final RoomStatusHistoryRepository roomStatusHistoryRepository;

    public AvailabilityServiceImpl(RoomRepository roomRepository,
                                   BookingRepository bookingRepository,
                                   RoomStatusHistoryRepository roomStatusHistoryRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.roomStatusHistoryRepository = roomStatusHistoryRepository;
    }

    @Override
    public List<RoomAvailabilityResponse> getHotelAvailability(Long hotelId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        LocalDate queryEndExclusive = to.plusDays(1);

        List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
        List<Booking> bookings = bookingRepository.findByHotelIdOverlappingDates(
                hotelId,
                from,
                queryEndExclusive,
                BLOCKING_BOOKING_STATUSES
        );
        List<RoomStatusHistory> statusHistory = roomStatusHistoryRepository.findByHotelIdOverlappingDates(
                hotelId,
                from,
                queryEndExclusive
        );

        Map<Long, List<Booking>> bookingsByRoom = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getRoom().getId()));
        Map<Long, List<RoomStatusHistory>> statusesByRoom = statusHistory.stream()
                .collect(Collectors.groupingBy(history -> history.getRoom().getId()));

        return rooms.stream()
                .sorted(Comparator.comparing(Room::getRoomNumber, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Room::getId))
                .map(room -> buildRoomAvailability(room, from, to,
                        bookingsByRoom.getOrDefault(room.getId(), List.of()),
                        statusesByRoom.getOrDefault(room.getId(), List.of())))
                .toList();
    }

    private RoomAvailabilityResponse buildRoomAvailability(Room room,
                                                           LocalDate from,
                                                           LocalDate to,
                                                           List<Booking> bookings,
                                                           List<RoomStatusHistory> statusHistory) {
        List<AvailabilityDayResponse> days = Stream.iterate(from, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(from, to) + 1)
                .map(date -> buildDay(date, room, bookings, statusHistory))
                .toList();

        return RoomAvailabilityResponse.builder()
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .days(days)
                .build();
    }

    private AvailabilityDayResponse buildDay(LocalDate date,
                                             Room room,
                                             List<Booking> bookings,
                                             List<RoomStatusHistory> statusHistory) {
        Booking booking = bookings.stream()
                .filter(candidate -> coversBookingDay(candidate, date))
                .findFirst()
                .orElse(null);
        if (booking != null) {
            return AvailabilityDayResponse.builder()
                    .date(date)
                    .status(AvailabilityStatus.BOOKED)
                    .bookingId(booking.getId())
                    .build();
        }

        RoomStatus effectiveStatus = statusHistory.stream()
                .filter(history -> coversStatusDay(history, date))
                .max(Comparator.comparing(RoomStatusHistory::getStartDate)
                        .thenComparing(RoomStatusHistory::getId))
                .map(RoomStatusHistory::getStatus)
                .orElse(room.getStatus());

        return AvailabilityDayResponse.builder()
                .date(date)
                .status(toAvailabilityStatus(effectiveStatus))
                .build();
    }

    private boolean coversBookingDay(Booking booking, LocalDate date) {
        return !date.isBefore(booking.getCheckinDate()) && date.isBefore(booking.getCheckoutDate());
    }

    private boolean coversStatusDay(RoomStatusHistory history, LocalDate date) {
        return !date.isBefore(history.getStartDate())
                && (history.getEndDate() == null || date.isBefore(history.getEndDate()));
    }

    private AvailabilityStatus toAvailabilityStatus(RoomStatus roomStatus) {
        if (roomStatus == RoomStatus.MAINTENANCE) {
            return AvailabilityStatus.MAINTENANCE;
        }
        if (roomStatus == RoomStatus.UNAVAILABLE) {
            return AvailabilityStatus.UNAVAILABLE;
        }
        return AvailabilityStatus.AVAILABLE;
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from болон to огноо заавал байна");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to огноо from огнооноос өмнө байж болохгүй");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("Availability range 370 хоногоос их байж болохгүй");
        }
    }
}
