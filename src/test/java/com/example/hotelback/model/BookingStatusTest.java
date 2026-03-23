package com.example.hotelback.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingStatusTest {

    @Test
    void fromValueMapsPendingToNew() {
        assertThat(BookingStatus.fromValue("PENDING")).isEqualTo(BookingStatus.NEW);
    }

    @Test
    void fromValueIsCaseInsensitiveForSupportedValues() {
        assertThat(BookingStatus.fromValue("confirmed")).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void fromValueRejectsUnknownStatus() {
        assertThatThrownBy(() -> BookingStatus.fromValue("DONE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid booking status: DONE");
    }
}
