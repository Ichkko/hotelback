package com.example.hotelback.service.impl;

import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.repository.RoomStatusHistoryRepository;
import com.example.hotelback.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomStatusHistoryRepository roomStatusHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Room room;
    private User user;

    @BeforeEach
    void setUp() {
        Hotel hotel = new Hotel();
        hotel.setId(99L);

        room = new Room();
        room.setId(7L);
        room.setHotel(hotel);
        room.setPrice(100.0);
        room.setCapacity(2);

        user = new User();
        user.setId(15L);
        user.setEmail("guest@example.com");
        user.setGlobalRole(com.example.hotelback.model.GlobalRole.USER);
    }

    @Test
    void createBookingRejectsOverlappingBooking() {
        Booking booking = validBooking();
        when(roomRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any(), anyList()))
                .thenReturn(List.of(new Booking()));

        assertThatThrownBy(() -> bookingService.createBooking(booking))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("аль хэдийнэ захиалгатай");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsPastCheckinDate() {
        Booking booking = validBooking();
        booking.setCheckinDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> bookingService.createBooking(booking))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Өнгөрсөн өдрөөр");
    }

    @Test
    void createBookingRejectsCheckoutBeforeOrEqualCheckin() {
        Booking booking = validBooking();
        booking.setCheckoutDate(booking.getCheckinDate());

        assertThatThrownBy(() -> bookingService.createBooking(booking))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-out нь check-in-ээс хойшхи өдөр");
    }

    @Test
    void createBookingRejectsGuestCountLessThanOne() {
        Booking booking = validBooking();
        booking.setGuestCount(0);
        when(roomRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> bookingService.createBooking(booking))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1-ээс багагүй");
    }

    @Test
    void createBookingRejectsGuestCountAboveRoomCapacity() {
        Booking booking = validBooking();
        booking.setGuestCount(3);
        when(roomRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> bookingService.createBooking(booking))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("багтаамжаас хэтэрсэн");
    }


    @Test
    void createBookingLoadsRoomPriceFromRepositoryWhenRequestOnlyContainsRoomId() {
        Booking booking = validBooking();
        Room requestRoom = new Room();
        requestRoom.setId(room.getId());
        booking.setRoom(requestRoom);

        when(roomRepository.findByIdForUpdate(room.getId())).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any(), anyList()))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking saved = bookingService.createBooking(booking);

        assertThat(saved.getRoom()).isSameAs(room);
        assertThat(saved.getRoomPrice()).isEqualByComparingTo("100.0");
    }

    @Test
    void createBookingRejectsUnknownRoomId() {
        Booking booking = validBooking();
        Room requestRoom = new Room();
        requestRoom.setId(999L);
        booking.setRoom(requestRoom);

        when(roomRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(booking))
                .isInstanceOf(com.example.hotelback.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Өрөө олдсонгүй");
    }

    @Test
    void confirmBookingTransitionsNewToConfirmed() {
        Booking booking = validBooking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.NEW);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenAnswer(invocation -> invocation.getArgument(0));

        Booking confirmed = bookingService.confirmBooking(1L);

        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(notificationService).createNotification(15L, "Захиалга баталгаажлаа",
                "Таны захиалга CONFIRMED төлөвт шилжлээ. Дугаар: null", "BOOKING_CONFIRMED");
    }

    @Test
    void cancelBookingRejectsPaidBooking() {
        Booking booking = validBooking();
        booking.setId(2L);
        booking.setStatus(BookingStatus.PAID);
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAID төлөвтэй");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingCalculatesFieldsAndDefaultsStatusToNew() {
        Booking booking = validBooking();
        when(roomRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any(), anyList()))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking saved = bookingService.createBooking(booking);

        assertThat(saved.getStatus()).isEqualTo(BookingStatus.NEW);
        assertThat(saved.getNights()).isEqualTo(2);
        assertThat(saved.getRoomPrice()).isEqualByComparingTo("100.0");
        assertThat(saved.getServiceFee()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("210.00"));
        assertThat(saved.getBookingNumber()).isEqualTo("BK-99-7-" + booking.getCheckinDate() + "-" + booking.getCheckoutDate());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.NEW);
        verify(notificationService).createNotification(15L, "Захиалга амжилттай үүслээ",
                "Таны захиалга бүртгэгдлээ. Дугаар: " + saved.getBookingNumber(), "BOOKING_CREATED");
    }

    @Test
    void createBookingLocksRoomBeforeOverlapCheck() {
        Booking booking = validBooking();
        when(roomRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any(), anyList()))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.createBooking(booking);

        verify(roomRepository, times(1)).findByIdForUpdate(7L);
    }

    private Booking validBooking() {
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setCheckinDate(LocalDate.now().plusDays(3));
        booking.setCheckoutDate(LocalDate.now().plusDays(5));
        booking.setFirstName("Test");
        booking.setLastName("User");
        booking.setEmail("guest@example.com");
        booking.setPhone("99112233");
        booking.setGuestCount(2);
        return booking;
    }
}
