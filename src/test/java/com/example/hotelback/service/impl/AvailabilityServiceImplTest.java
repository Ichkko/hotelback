package com.example.hotelback.service.impl;

import com.example.hotelback.dto.RoomAvailabilityResponse;
import com.example.hotelback.model.AvailabilityStatus;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.RoomStatus;
import com.example.hotelback.model.RoomStatusHistory;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.repository.RoomStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomStatusHistoryRepository roomStatusHistoryRepository;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    @Test
    void getHotelAvailabilityMarksBookingsAndFutureRoomStatusPeriods() {
        Room room = room(12L, "203", RoomStatus.AVAILABLE);
        Booking booking = booking(55L, room, LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 16));
        RoomStatusHistory maintenance = statusHistory(99L, room, RoomStatus.MAINTENANCE,
                LocalDate.of(2026, 5, 16), LocalDate.of(2026, 5, 17));

        when(roomRepository.findByHotel_Id(7L)).thenReturn(List.of(room));
        when(bookingRepository.findByHotelIdOverlappingDates(
                7L,
                LocalDate.of(2026, 5, 14),
                LocalDate.of(2026, 5, 18),
                List.of(BookingStatus.NEW, BookingStatus.CONFIRMED, BookingStatus.PAID)
        )).thenReturn(List.of(booking));
        when(roomStatusHistoryRepository.findByHotelIdOverlappingDates(
                7L,
                LocalDate.of(2026, 5, 14),
                LocalDate.of(2026, 5, 18)
        )).thenReturn(List.of(maintenance));

        List<RoomAvailabilityResponse> result = availabilityService.getHotelAvailability(
                7L,
                LocalDate.of(2026, 5, 14),
                LocalDate.of(2026, 5, 17)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomId()).isEqualTo(12L);
        assertThat(result.get(0).getDays())
                .extracting(day -> day.getDate() + ":" + day.getStatus() + ":" + day.getBookingId())
                .containsExactly(
                        "2026-05-14:AVAILABLE:null",
                        "2026-05-15:BOOKED:55",
                        "2026-05-16:MAINTENANCE:null",
                        "2026-05-17:AVAILABLE:null"
                );
    }

    @Test
    void getHotelAvailabilityRejectsInvalidRange() {
        assertThatThrownBy(() -> availabilityService.getHotelAvailability(
                7L,
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("to огноо");
    }

    private Room room(Long id, String roomNumber, RoomStatus status) {
        Hotel hotel = new Hotel();
        hotel.setId(7L);

        Room room = new Room();
        room.setId(id);
        room.setHotel(hotel);
        room.setRoomNumber(roomNumber);
        room.setRoomType("Deluxe");
        room.setStatus(status);
        return room;
    }

    private Booking booking(Long id, Room room, LocalDate checkin, LocalDate checkout) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setRoom(room);
        booking.setCheckinDate(checkin);
        booking.setCheckoutDate(checkout);
        booking.setStatus(BookingStatus.CONFIRMED);
        return booking;
    }

    private RoomStatusHistory statusHistory(Long id,
                                            Room room,
                                            RoomStatus status,
                                            LocalDate startDate,
                                            LocalDate endDate) {
        RoomStatusHistory history = new RoomStatusHistory();
        history.setId(id);
        history.setRoom(room);
        history.setStatus(status);
        history.setStartDate(startDate);
        history.setEndDate(endDate);
        return history;
    }
}
