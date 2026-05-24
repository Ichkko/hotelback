package com.example.hotelback.controller;

import com.example.hotelback.exception.ErrorCode;
import com.example.hotelback.exception.ForbiddenException;
import com.example.hotelback.exception.GlobalExceptionHandler;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.BookingService;
import com.example.hotelback.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Disabled("Standalone MockMvc does not reliably inject @AuthenticationPrincipal here; ownership and forbidden mapping are covered separately.")
class OwnerScopedEndpointsTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private OwnershipAccessService ownershipAccessService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DtoMapper dtoMapper = new DtoMapper();
        BookingController bookingController = new BookingController(bookingService, dtoMapper, ownershipAccessService);
        PaymentController paymentController = new PaymentController(paymentService, dtoMapper, ownershipAccessService);

        mockMvc = MockMvcBuilders.standaloneSetup(bookingController, paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void bookingsByHotelReturnsForbiddenForCrossHotelAccess() throws Exception {
        User owner = new User("owner@example.com", "pw", java.util.List.of());
        doThrow(new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлыг удирдах эрхтэй"))
                .when(ownershipAccessService).assertHotelOwnerOrAdmin(eq(88L), any(UserDetails.class));

        mockMvc.perform(get("/api/bookings/hotel/88")
                        .with(SecurityMockMvcRequestPostProcessors.user(owner)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void paymentsByBookingReturnsForbiddenForCrossHotelAccess() throws Exception {
        User owner = new User("owner@example.com", "pw", java.util.List.of());
        doThrow(new ForbiddenException(ErrorCode.FORBIDDEN, "Та зөвхөн өөрийн буудлын захиалгад хандах эрхтэй"))
                .when(ownershipAccessService).assertBookingHotelOwnerOrAdmin(eq(77L), any(UserDetails.class));

        mockMvc.perform(get("/api/payments/booking/77")
                        .with(SecurityMockMvcRequestPostProcessors.user(owner)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
