package com.example.hotelback.controller;

import com.example.hotelback.exception.GlobalExceptionHandler;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.HotelRole;
import com.example.hotelback.model.HotelUserRole;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.RoomImage;
import com.example.hotelback.model.RoomStatus;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.PaymentRepository;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.AvailabilityService;
import com.example.hotelback.service.HotelService;
import com.example.hotelback.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HotelRoomEndpointsTest {

    @Mock
    private HotelService hotelService;

    @Mock
    private RoomService roomService;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private OwnershipAccessService ownershipAccessService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RoomRepository roomRepository;

    private MockMvc hotelMockMvc;
    private MockMvc roomMockMvc;
    private HotelController hotelController;
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        DtoMapper dtoMapper = new DtoMapper();

        hotelController = new HotelController(
                hotelService,
                roomService,
                availabilityService,
                dtoMapper,
                ownershipAccessService,
                bookingRepository,
                paymentRepository,
                roomRepository
        );

        hotelMockMvc = MockMvcBuilders.standaloneSetup(hotelController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        roomController = new RoomController(roomService, dtoMapper);
        roomMockMvc = MockMvcBuilders.standaloneSetup(roomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void hotelRoomsEndpointReturnsEmptyArrayWhenHotelHasNoRooms() throws Exception {
        when(roomService.getRoomsByHotelId(77L)).thenReturn(List.of());

        hotelMockMvc.perform(get("/api/hotels/77/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(roomService).getRoomsByHotelId(77L);
    }

    @Test
    void hotelRoomsEndpointMatchesRoomsByHotelRouteContract() throws Exception {
        Room room = new Room();
        room.setId(11L);
        room.setRoomType("Deluxe Twin");
        room.setPrice(250000.0);
        room.setCapacity(2);
        room.setStatus(RoomStatus.AVAILABLE);
        RoomImage image = new RoomImage();
        image.setId(91L);
        image.setImageUrl("https://cdn.example.com/rooms/11/main.jpg");
        image.setDescription("Main view");
        image.setRoom(room);
        room.getRoomImages().add(image);

        UserDetails principal = new org.springframework.security.core.userdetails.User("manager@example.com", "pw", List.of());
        when(roomService.getRoomsByHotelId(15L)).thenReturn(List.of(room));
        when(roomService.getRoomsByHotelId(15L, principal)).thenReturn(List.of(room));

        String expectedJson = """
                [{
                  "id": 11,
                  "hotelId": null,
                  "roomType": "Deluxe Twin",
                  "price": 250000.0,
                  "capacity": 2,
                  "status": "AVAILABLE",
                  "images": [{
                    "id": 91,
                    "imageUrl": "https://cdn.example.com/rooms/11/main.jpg",
                    "description": "Main view"
                  }]
                }]
                """;

        hotelMockMvc.perform(get("/api/hotels/15/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        List<com.example.hotelback.dto.RoomResponse> roomRouteResponse = roomController.getRoomsByHotel(15L, principal).getBody();

        org.assertj.core.api.Assertions.assertThat(roomRouteResponse).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(roomRouteResponse.get(0).getId()).isEqualTo(11L);
        org.assertj.core.api.Assertions.assertThat(roomRouteResponse.get(0).getRoomType()).isEqualTo("Deluxe Twin");

        verify(roomService).getRoomsByHotelId(15L);
        verify(roomService).getRoomsByHotelId(15L, principal);
    }

    @Test
    void myAccessResponseIncludesMembershipAndPermissionsPerHotel() {
        User currentUser = new User();
        currentUser.setId(7L);

        Hotel firstHotel = new Hotel();
        firstHotel.setId(100L);
        firstHotel.setName("Alpha");
        HotelUserRole ownerRole = new HotelUserRole();
        ownerRole.setHotel(firstHotel);
        ownerRole.setUser(currentUser);
        ownerRole.setRole(HotelRole.OWNER);
        firstHotel.getStaffRoles().add(ownerRole);

        Hotel secondHotel = new Hotel();
        secondHotel.setId(200L);
        secondHotel.setName("Beta");
        HotelUserRole receptionRole = new HotelUserRole();
        receptionRole.setHotel(secondHotel);
        receptionRole.setUser(currentUser);
        receptionRole.setRole(HotelRole.RECEPTION);
        secondHotel.getStaffRoles().add(receptionRole);

        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User("user@example.com", "pw", List.of());

        when(ownershipAccessService.resolveCurrentUserId(principal)).thenReturn(7L);
        when(hotelService.getAccessibleHotelsByUserId(principal)).thenReturn(List.of(firstHotel, secondHotel));

        List<com.example.hotelback.dto.HotelResponse> response = hotelController.getMyAccessibleHotels(principal).getBody();

        org.assertj.core.api.Assertions.assertThat(response).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(response.get(0).getMembership().getHotelId()).isEqualTo(100L);
        org.assertj.core.api.Assertions.assertThat(response.get(0).getMembership().getHotelName()).isEqualTo("Alpha");
        org.assertj.core.api.Assertions.assertThat(response.get(0).getMembership().getRole()).isEqualTo("OWNER");
        org.assertj.core.api.Assertions.assertThat(response.get(0).getPermissions().isCanManageHotel()).isTrue();
        org.assertj.core.api.Assertions.assertThat(response.get(0).getPermissions().isCanManageStaff()).isTrue();
        org.assertj.core.api.Assertions.assertThat(response.get(0).getPermissions().isCanManagePayments()).isTrue();
        org.assertj.core.api.Assertions.assertThat(response.get(0).getPermissions().isCanViewReports()).isTrue();
        org.assertj.core.api.Assertions.assertThat(response.get(1).getMembership().getHotelId()).isEqualTo(200L);
        org.assertj.core.api.Assertions.assertThat(response.get(1).getMembership().getHotelName()).isEqualTo("Beta");
        org.assertj.core.api.Assertions.assertThat(response.get(1).getMembership().getRole()).isEqualTo("RECEPTION");
        org.assertj.core.api.Assertions.assertThat(response.get(1).getPermissions().isCanManageHotel()).isFalse();
        org.assertj.core.api.Assertions.assertThat(response.get(1).getPermissions().isCanManageRooms()).isFalse();
        org.assertj.core.api.Assertions.assertThat(response.get(1).getPermissions().isCanUpdateBookings()).isTrue();
        org.assertj.core.api.Assertions.assertThat(response.get(1).getPermissions().isCanManagePayments()).isFalse();
        org.assertj.core.api.Assertions.assertThat(response.get(1).getPermissions().isCanViewReports()).isFalse();
    }
}
