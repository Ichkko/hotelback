package com.example.hotelback.controller;

import com.example.hotelback.exception.GlobalExceptionHandler;
import com.example.hotelback.mapper.DtoMapper;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.RoomStatus;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.HotelService;
import com.example.hotelback.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private OwnershipAccessService ownershipAccessService;

    private MockMvc hotelMockMvc;
    private MockMvc roomMockMvc;

    @BeforeEach
    void setUp() {
        DtoMapper dtoMapper = new DtoMapper();

        hotelMockMvc = MockMvcBuilders.standaloneSetup(new HotelController(hotelService, roomService, dtoMapper, ownershipAccessService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        roomMockMvc = MockMvcBuilders.standaloneSetup(new RoomController(roomService, dtoMapper, ownershipAccessService))
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

        when(roomService.getRoomsByHotelId(15L)).thenReturn(List.of(room));

        String expectedJson = """
                [{
                  "id": 11,
                  "hotelId": null,
                  "roomType": "Deluxe Twin",
                  "price": 250000.0,
                  "capacity": 2,
                  "status": "AVAILABLE"
                }]
                """;

        hotelMockMvc.perform(get("/api/hotels/15/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        roomMockMvc.perform(get("/api/rooms/hotel/15"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andExpect(jsonPath("$[0].id").value(11L))
                .andExpect(jsonPath("$[0].roomType").value("Deluxe Twin"));

        verify(roomService, times(2)).getRoomsByHotelId(15L);
    }
}
