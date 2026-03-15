package com.example.hotelback.repository;

import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "select b from Booking b where b.user.id = :userId", countQuery = "select count(b) from Booking b where b.user.id = :userId")
    List<Booking> findByUser_Id(@Param("userId") Long userId);

    @Query(value = "select b from Booking b where b.room.id = :roomId", countQuery = "select count(b) from Booking b where b.room.id = :roomId")
    List<Booking> findByRoom_Id(@Param("roomId") Long roomId);

    /**
     * Бронирын хугацааны давхцлыг шалгах query.
     * (existing.checkin < newCheckout) AND (existing.checkout > newCheckin)
     */
    @Query(value = """
        select b
        from Booking b
        where b.room.id = :roomId
          and b.status in :statuses
          and b.checkinDate < :newCheckout
          and b.checkoutDate > :newCheckin
        """, countQuery = """
        select count(b)
        from Booking b
        where b.room.id = :roomId
          and b.status in :statuses
          and b.checkinDate < :newCheckout
          and b.checkoutDate > :newCheckin
        """)
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("newCheckin") LocalDate newCheckin,
            @Param("newCheckout") LocalDate newCheckout,
            @Param("statuses") List<BookingStatus> statuses
    );

    /**
     * Төлбөр хийх үед booking-ийг lock-лож авах.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);
}
