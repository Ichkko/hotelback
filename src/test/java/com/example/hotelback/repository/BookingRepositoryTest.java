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
        "spring.jpa.properties.hibernate.jdbc.time_zone=UTC",
        "spring.flyway.enabled=false"
})
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findOverlappingBookingsReturnsOnlyMatchingStatusesAndDateRanges() {
        TestData testData = persistBaseData();

        entityManager.persist(booking(testData.user(), testData.room(), LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 12), BookingStatus.NEW));
        entityManager.persist(booking(testData.user(), testData.room(), LocalDate.of(2026, 4, 11), LocalDate.of(2026, 4, 13), BookingStatus.CANCELLED));
        entityManager.persist(booking(testData.user(), testData.room(), LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 14), BookingStatus.CONFIRMED));
        entityManager.flush();

        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                testData.room().getId(),
                LocalDate.of(2026, 4, 11),
                LocalDate.of(2026, 4, 13),
                List.of(BookingStatus.NEW, BookingStatus.CONFIRMED, BookingStatus.PAID)
        );

        assertThat(overlaps)
                .extracting(Booking::getStatus)
                .containsExactlyInAnyOrder(BookingStatus.NEW, BookingStatus.CONFIRMED);
    }

    @Test
    void findByUserIdReturnsOnlyBookingsForThatUser() {
        TestData primary = persistBaseData();
        TestData secondary = persistBaseData("other@example.com", "Other Hotel", "Suite");

        Booking targetBooking = booking(primary.user(), primary.room(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3), BookingStatus.NEW);
        Booking otherBooking = booking(secondary.user(), secondary.room(), LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 4), BookingStatus.CONFIRMED);
        entityManager.persist(targetBooking);
        entityManager.persist(otherBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findByUser_Id(primary.user().getId());

        assertThat(result)
                .extracting(Booking::getBookingNumber)
                .containsExactly(targetBooking.getBookingNumber());
    }

    @Test
    void findByIdForUpdateReturnsLockedBookingWhenPresent() {
        TestData testData = persistBaseData();
        Booking savedBooking = booking(testData.user(), testData.room(), LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 12), BookingStatus.CONFIRMED);
        entityManager.persist(savedBooking);
        entityManager.flush();
        entityManager.clear();

        Booking lockedBooking = bookingRepository.findByIdForUpdate(savedBooking.getId()).orElseThrow();

        assertThat(lockedBooking.getId()).isEqualTo(savedBooking.getId());
        assertThat(lockedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    private TestData persistBaseData() {
        return persistBaseData("repo@example.com", "Repo Hotel", "Deluxe");
    }

    private TestData persistBaseData(String email, String hotelName, String roomType) {
        User user = new User();
        user.setName("Repo User");
        user.setEmail(email);
        user.setPassword("secret");
        user.setGlobalRole(com.example.hotelback.model.GlobalRole.USER);
        entityManager.persist(user);

        Hotel hotel = new Hotel();
        hotel.setName(hotelName);
        hotel.setDescription("desc");
        hotel.setAddress("addr");
        hotel.setAimag("UB");
        hotel.setPhone("70000000");
        entityManager.persist(hotel);

        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomType(roomType);
        room.setPrice(150.0);
        room.setCapacity(2);
        entityManager.persist(room);
        entityManager.flush();

        return new TestData(user, room);
    }

    private Booking booking(User user, Room room, LocalDate checkin, LocalDate checkout, BookingStatus status) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckinDate(checkin);
        booking.setCheckoutDate(checkout);
        booking.setFirstName("Repo");
        booking.setLastName("Test");
        booking.setEmail(user.getEmail());
        booking.setPhone("99990000");
        booking.setGuestCount(1);
        booking.setBookingNumber("BK-" + status + "-" + checkin + "-" + user.getId());
        booking.setStatus(status);
        return booking;
    }

    private record TestData(User user, Room room) {
    }
}
