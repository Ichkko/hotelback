package com.example.hotelback.repository;

import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.jdbc.time_zone=UTC"
})
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findOverlappingBookingsReturnsOnlyMatchingStatusesAndDateRanges() {
        User user = new User();
        user.setName("Repo User");
        user.setEmail("repo@example.com");
        user.setPassword("secret");
        user.setRole("USER");
        entityManager.persist(user);

        Hotel hotel = new Hotel();
        hotel.setName("Repo Hotel");
        hotel.setDescription("desc");
        hotel.setAddress("addr");
        hotel.setAimag("UB");
        hotel.setPhone("70000000");
        entityManager.persist(hotel);

        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomType("Deluxe");
        room.setPrice(150.0);
        room.setCapacity(2);
        entityManager.persist(room);

        entityManager.persist(booking(user, room, LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 12), BookingStatus.NEW));
        entityManager.persist(booking(user, room, LocalDate.of(2026, 4, 11), LocalDate.of(2026, 4, 13), BookingStatus.CANCELLED));
        entityManager.persist(booking(user, room, LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 14), BookingStatus.CONFIRMED));
        entityManager.flush();

        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                room.getId(),
                LocalDate.of(2026, 4, 11),
                LocalDate.of(2026, 4, 13),
                List.of(BookingStatus.NEW, BookingStatus.CONFIRMED, BookingStatus.PAID)
        );

        assertThat(overlaps)
                .extracting(Booking::getStatus)
                .containsExactlyInAnyOrder(BookingStatus.NEW, BookingStatus.CONFIRMED);
    }

    private Booking booking(User user, Room room, LocalDate checkin, LocalDate checkout, BookingStatus status) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckinDate(checkin);
        booking.setCheckoutDate(checkout);
        booking.setFirstName("Repo");
        booking.setLastName("Test");
        booking.setEmail("repo@example.com");
        booking.setPhone("99990000");
        booking.setGuestCount(1);
        booking.setBookingNumber("BK-" + status + "-" + checkin);
        booking.setStatus(status);
        return booking;
    }
}
