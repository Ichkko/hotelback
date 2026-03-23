package com.example.hotelback.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoomStatusConverterTest {

    private final RoomStatusConverter converter = new RoomStatusConverter();

    @Test
    void convertToEntityAttributeMapsLegacyBookedStatusToUnavailable() {
        assertEquals(RoomStatus.UNAVAILABLE, converter.convertToEntityAttribute("BOOKED"));
    }

    @Test
    void convertToEntityAttributeReturnsNullForBlankDatabaseValue() {
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    void convertToEntityAttributeRejectsUnknownStatus() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute("SOLD_OUT")
        );

        assertEquals("Invalid room status: SOLD_OUT", exception.getMessage());
    }

    @Test
    void apiFacingParserStillRejectsBookedStatus() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RoomStatus.fromValue("BOOKED")
        );

        assertEquals("Invalid room status: BOOKED", exception.getMessage());
    }
}
