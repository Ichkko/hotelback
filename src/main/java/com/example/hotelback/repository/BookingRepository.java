package com.example.hotelback.repository;

import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.HotelRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "select b from Booking b where b.user.id = :userId", countQuery = "select count(b) from Booking b where b.user.id = :userId")
    List<Booking> findByUser_Id(@Param("userId") Long userId);

    @Query(value = "select b from Booking b where b.room.id = :roomId", countQuery = "select count(b) from Booking b where b.room.id = :roomId")
    List<Booking> findByRoom_Id(@Param("roomId") Long roomId);

    @Query("""
        select distinct b
        from Booking b
        join fetch b.room r
        left join fetch b.user u
        where r.hotel.id = :hotelId
        order by b.createdAt desc, b.id desc
        """)
    List<Booking> findByHotelId(@Param("hotelId") Long hotelId);

    @Query("""
        select distinct b
        from Booking b
        join fetch b.room r
        where r.hotel.id = :hotelId
          and b.status in :statuses
          and b.checkinDate < :to
          and b.checkoutDate > :from
        order by b.checkinDate, b.id
        """)
    List<Booking> findByHotelIdOverlappingDates(@Param("hotelId") Long hotelId,
                                                @Param("from") LocalDate from,
                                                @Param("to") LocalDate to,
                                                @Param("statuses") List<BookingStatus> statuses);

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    @Query("select b.user.id from Booking b where b.id = :id")
    Optional<Long> findUserIdById(@Param("id") Long id);

    @Query("select b.room.hotel.id from Booking b where b.id = :id")
    Optional<Long> findHotelIdById(@Param("id") Long id);

    @Query("""
            select count(b) > 0
            from Booking b
            join b.room r
            join r.hotel h
            join h.staffRoles hur
            where b.id = :id and hur.user.id = :userId and hur.role = com.example.hotelback.model.HotelRole.OWNER
            """)
    boolean existsByIdAndHotelOwnerId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
            select count(b) > 0
            from Booking b
            join b.room r
            join r.hotel h
            join h.staffRoles hur
            where b.id = :id and hur.user.id = :userId and hur.role = com.example.hotelback.model.HotelRole.RECEPTION
            """)
    boolean existsByIdAndHotelReceptionistId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
            select count(b) > 0
            from Booking b
            join b.room r
            join r.hotel h
            join h.staffRoles hur
            where b.id = :id and hur.user.id = :userId
            """)
    boolean existsByIdAndHotelStaffId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("select count(b) from Booking b join b.room r where r.hotel.id = :hotelId and b.status = :status")
    long countByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") BookingStatus status);

    @Query("select coalesce(sum(b.totalPrice), 0) from Booking b join b.room r where r.hotel.id = :hotelId and b.status = :status")
    BigDecimal sumTotalPriceByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") BookingStatus status);

    @Query("select coalesce(sum(b.serviceFee), 0) from Booking b join b.room r where r.hotel.id = :hotelId and b.status = :status")
    BigDecimal sumServiceFeeByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") BookingStatus status);

    @Query("""
        select count(distinct b.room.id)
        from Booking b
        join b.room r
        where r.hotel.id = :hotelId
          and b.status in :statuses
          and b.checkinDate <= :today
          and b.checkoutDate > :today
        """)
    long countOccupiedRoomsToday(@Param("hotelId") Long hotelId,
                                 @Param("statuses") List<BookingStatus> statuses,
                                 @Param("today") LocalDate today);
}
