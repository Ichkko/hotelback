package com.example.hotelback.repository;

import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query(value = "select r from Room r where r.hotel.id = :hotelId", countQuery = "select count(r) from Room r where r.hotel.id = :hotelId")
    List<Room> findByHotel_Id(@Param("hotelId") Long hotelId);

    /**
     * Тухайн зочид буудлын захиалгад ороогүй (available) өрөөнүүдийг буцаана.
     */
    @Query(value = """
        select r
        from Room r
        where r.hotel.id = :hotelId
          and r.id not in (
            select b.room.id
            from Booking b
            where b.status in :statuses
              and b.checkinDate < :newCheckout
              and b.checkoutDate > :newCheckin
          )
        """, countQuery = """
        select count(r)
        from Room r
        where r.hotel.id = :hotelId
          and r.id not in (
            select b.room.id
            from Booking b
            where b.status in :statuses
              and b.checkinDate < :newCheckout
              and b.checkoutDate > :newCheckin
          )
        """)
    List<Room> findAvailableRoomsByHotelAndDates(
            @Param("hotelId") Long hotelId,
            @Param("newCheckin") LocalDate newCheckin,
            @Param("newCheckout") LocalDate newCheckout,
            @Param("statuses") List<BookingStatus> statuses
    );
}
