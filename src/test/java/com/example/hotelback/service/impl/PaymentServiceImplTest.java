package com.example.hotelback.service.impl;

import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.Payment;
import com.example.hotelback.model.PaymentStatus;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.PaymentRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Booking booking;

    @BeforeEach
    void setUp() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);

        Room room = new Room();
        room.setId(2L);
        room.setHotel(hotel);
        room.setPrice(120.0);
        room.setCapacity(2);

        User user = new User();
        user.setId(9L);
        user.setEmail("payer@example.com");

        booking = new Booking();
        booking.setId(5L);
        booking.setRoom(room);
        booking.setUser(user);
        booking.setBookingNumber("BK-1-2-2026-03-25-2026-03-27");
        booking.setCheckinDate(LocalDate.now().plusDays(5));
        booking.setCheckoutDate(LocalDate.now().plusDays(7));
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Test
    void createPaymentRejectsNonConfirmedBooking() {
        booking.setStatus(BookingStatus.NEW);
        Payment payment = validPayment(new BigDecimal("100.00"));
        when(bookingRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.createPayment(payment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CONFIRMED");
    }

    @Test
    void createPaymentRejectsAmountLessThanOrEqualZero() {
        Payment payment = validPayment(BigDecimal.ZERO);

        assertThatThrownBy(() -> paymentService.createPayment(payment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("дүн буруу");
    }

    @Test
    void createPaymentRejectsOverpayment() {
        Payment payment = validPayment(new BigDecimal("200.00"));
        Payment existingPayment = new Payment();
        existingPayment.setAmount(new BigDecimal("100.00"));
        existingPayment.setStatus(PaymentStatus.SUCCESS);

        when(bookingRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_Id(5L)).thenReturn(List.of(existingPayment));

        assertThatThrownBy(() -> paymentService.createPayment(payment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("илүү дүнгээр");
    }

    @Test
    void createPaymentMarksBookingPaidOnFullPayment() {
        Payment payment = validPayment(new BigDecimal("240.00"));
        when(bookingRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_Id(5L)).thenReturn(List.of());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment saved = paymentService.createPayment(payment);

        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(saved.getPaymentDate()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAID);
        verify(bookingRepository).save(booking);
        verify(notificationService).createNotification(9L, "Захиалга бүрэн төлөгдлөө",
                "Таны захиалга бүрэн төлөгдөж PAID төлөвт шилжлээ. Дугаар: " + booking.getBookingNumber(), "BOOKING_PAID");
    }

    @Test
    void createPaymentDoesNotMarkBookingPaidOnPartialPayment() {
        Payment payment = validPayment(new BigDecimal("120.00"));
        when(bookingRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_Id(5L)).thenReturn(List.of());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment saved = paymentService.createPayment(payment);

        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository, never()).save(any(Booking.class));
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getBooking()).isSameAs(booking);
    }

    private Payment validPayment(BigDecimal amount) {
        Payment payment = new Payment();
        Booking bookingRef = new Booking();
        bookingRef.setId(5L);
        payment.setBooking(bookingRef);
        payment.setAmount(amount);
        payment.setPaymentMethod("CARD");
        return payment;
    }
}
