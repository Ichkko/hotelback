package com.example.hotelback.repository;

import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Room;
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
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query(value = "select r from Room r where r.hotel.id = :hotelId", countQuery = "select count(r) from Room r where r.hotel.id = :hotelId")
    List<Room> findByHotel_Id(@Param("hotelId") Long hotelId);

    @Query("select count(r) from Room r where r.hotel.id = :hotelId")
    long countByHotelId(@Param("hotelId") Long hotelId);

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select count(r) > 0
            from Room r
            join r.hotel h
            join h.staffRoles hur
            where r.id = :roomId and hur.user.id = :userId and hur.role = com.example.hotelback.model.HotelRole.OWNER
            """)
    boolean existsByIdAndHotelOwnerId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("""
            select count(r) > 0
            from Room r
            join r.hotel h
            join h.staffRoles hur
            where r.id = :roomId and hur.user.id = :userId and hur.role = com.example.hotelback.model.HotelRole.RECEPTION
            """)
    boolean existsByIdAndHotelReceptionistId(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
