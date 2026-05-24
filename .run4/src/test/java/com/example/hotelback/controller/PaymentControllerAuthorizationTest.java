package com.example.hotelback.controller;

import com.example.hotelback.dto.PaymentResponse;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.model.Payment;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.PaymentService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerAuthorizationTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private OwnershipAccessService ownershipAccessService;

    private PaymentController paymentController;
    private UserDetails ownerPrincipal;

    @BeforeEach
    void setUp() {
        paymentController = new PaymentController(paymentService, dtoMapper, ownershipAccessService);
        ownerPrincipal = new User("owner@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
    }

    @Test
    void getPaymentsByBookingChecksHotelStaffOrAdmin() {
        Payment payment = new Payment();
        PaymentResponse response = PaymentResponse.builder().id(41L).bookingId(9L).build();
        when(paymentService.getPaymentsByBookingId(9L)).thenReturn(List.of(payment));
        when(dtoMapper.toPaymentResponse(payment)).thenReturn(response);

        List<PaymentResponse> result = paymentController.getPaymentsByBooking(9L, ownerPrincipal).getBody();

        assertThat(result).containsExactly(response);
        verify(ownershipAccessService).assertBookingHotelStaffOrAdmin(9L, ownerPrincipal);
        verify(paymentService).getPaymentsByBookingId(9L);
    }
}
