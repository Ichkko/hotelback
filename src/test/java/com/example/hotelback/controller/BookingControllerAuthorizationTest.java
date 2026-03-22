package com.example.hotelback.controller;

import com.example.hotelback.dto.BookingResponse;
import com.example.hotelback.dto.CreateBookingRequest;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.model.Booking;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerAuthorizationTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private OwnershipAccessService ownershipAccessService;

    private BookingController bookingController;
    private UserDetails userPrincipal;
    private UserDetails adminPrincipal;

    @BeforeEach
    void setUp() {
        bookingController = new BookingController(bookingService, dtoMapper, ownershipAccessService);
        userPrincipal = new User("user@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        adminPrincipal = new User("admin@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void getAllBookingsReturnsOnlyCurrentUserBookingsForRegularUser() {
        Booking booking = new Booking();
        BookingResponse response = BookingResponse.builder().id(1L).build();
        when(ownershipAccessService.isAdmin(userPrincipal)).thenReturn(false);
        when(ownershipAccessService.resolveCurrentUserId(userPrincipal)).thenReturn(15L);
        when(bookingService.getBookingsByUserId(15L)).thenReturn(List.of(booking));
        when(dtoMapper.toBookingResponse(booking)).thenReturn(response);

        List<BookingResponse> result = bookingController.getAllBookings(userPrincipal).getBody();

        assertThat(result).containsExactly(response);
        verify(bookingService, never()).getAllBookings();
    }

    @Test
    void getAllBookingsReturnsEverythingForAdmin() {
        Booking booking = new Booking();
        BookingResponse response = BookingResponse.builder().id(2L).build();
        when(ownershipAccessService.isAdmin(adminPrincipal)).thenReturn(true);
        when(bookingService.getAllBookings()).thenReturn(List.of(booking));
        when(dtoMapper.toBookingResponse(booking)).thenReturn(response);

        List<BookingResponse> result = bookingController.getAllBookings(adminPrincipal).getBody();

        assertThat(result).containsExactly(response);
        verify(bookingService).getAllBookings();
        verify(bookingService, never()).getBookingsByUserId(any());
    }

    @Test
    void createBookingUsesAuthenticatedUserWhenRequestUserIdMissing() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRoomId(7L);
        Booking booking = new Booking();
        BookingResponse response = BookingResponse.builder().id(3L).build();

        when(ownershipAccessService.resolveCurrentUserId(userPrincipal)).thenReturn(15L);
        when(dtoMapper.toBooking(request)).thenReturn(booking);
        when(bookingService.createBooking(booking)).thenReturn(booking);
        when(dtoMapper.toBookingResponse(booking)).thenReturn(response);

        BookingResponse result = bookingController.createBooking(request, userPrincipal).getBody();

        assertThat(request.getUserId()).isEqualTo(15L);
        assertThat(result).isEqualTo(response);
        verify(ownershipAccessService).assertCurrentUserOrAdmin(15L, userPrincipal);
    }

    @Test
    void getBookingsByUserChecksOwnershipOrAdmin() {
        when(bookingService.getBookingsByUserId(15L)).thenReturn(List.of());

        bookingController.getBookingsByUser(15L, userPrincipal);

        verify(ownershipAccessService).assertCurrentUserOrAdmin(15L, userPrincipal);
        verify(bookingService).getBookingsByUserId(15L);
    }

    @Test
    void getBookingByIdChecksBookingOwnership() {
        when(bookingService.getBookingById(20L)).thenReturn(java.util.Optional.of(new Booking()));
        when(dtoMapper.toBookingResponse(any(Booking.class))).thenReturn(BookingResponse.builder().id(20L).build());

        bookingController.getBookingById(20L, userPrincipal);

        verify(ownershipAccessService).assertBookingOwnerOrAdmin(20L, userPrincipal);
    }
}
