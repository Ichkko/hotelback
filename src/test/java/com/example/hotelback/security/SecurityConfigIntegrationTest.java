package com.example.hotelback.security;

import com.example.hotelback.controller.AuthController;
import com.example.hotelback.controller.BookingController;
import com.example.hotelback.controller.HotelController;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.service.BookingService;
import com.example.hotelback.service.HotelService;
import com.example.hotelback.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, BookingController.class, HotelController.class})
@Import({SecurityConfig.class, DtoMapper.class})
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private RoomService roomService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void authEndpointsRemainPublic() throws Exception {
        User user = new User();
        user.setId(40L);
        user.setName("Public User");
        user.setEmail("public@example.com");
        user.setRole("USER");

        when(userRepository.findByEmail("public@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(user.getId());
            return saved;
        });
        when(jwtUtil.generateToken("public@example.com", "USER")).thenReturn("public-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Public User",
                                  "email": "public@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("public-token"));
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicGetEndpointDoesNotRequireAuthentication() throws Exception {
        when(hotelService.getAllHotels()).thenReturn(List.of(new Hotel()));

        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void authenticatedUserCanAccessProtectedEndpoint() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setCheckinDate(LocalDate.now().plusDays(1));
        booking.setCheckoutDate(LocalDate.now().plusDays(2));
        booking.setStatus(BookingStatus.NEW);
        when(bookingService.getAllBookings()).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotAccessAdminOnlyEndpoint() throws Exception {
        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test Hotel",
                                  "address": "Address",
                                  "aimag": "UB",
                                  "phone": "70000000"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminOnlyEndpoint() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setId(99L);
        hotel.setName("Admin Hotel");
        hotel.setAddress("Address");
        hotel.setAimag("UB");
        hotel.setPhone("70000000");
        when(hotelService.createHotel(any(Hotel.class))).thenReturn(hotel);

        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Admin Hotel",
                                  "address": "Address",
                                  "aimag": "UB",
                                  "phone": "70000000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99L));
    }
}
