package com.example.hotelback.repository;

import com.example.hotelback.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query(value = "select p from Payment p where p.booking.id = :bookingId", countQuery = "select count(p) from Payment p where p.booking.id = :bookingId")
    List<Payment> findByBooking_Id(@Param("bookingId") Long bookingId);

    @Query("""
        select p
        from Payment p
        join fetch p.booking b
        join fetch b.room r
        left join fetch b.user u
        where b.id = :bookingId
        order by p.paymentDate desc, p.id desc
        """)
    List<Payment> findDetailedByBookingId(@Param("bookingId") Long bookingId);

    @Query("""
        select p
        from Payment p
        join fetch p.booking b
        join fetch b.room r
        left join fetch b.user u
        where r.hotel.id = :hotelId
        order by p.paymentDate desc, p.id desc
        """)
    List<Payment> findByHotelId(@Param("hotelId") Long hotelId);

    @Query("""
        select coalesce(sum(p.amount), 0)
        from Payment p
        join p.booking b
        join b.room r
        where r.hotel.id = :hotelId and p.status = com.example.hotelback.model.PaymentStatus.SUCCESS
        """)
    java.math.BigDecimal sumSuccessfulPaymentsByHotelId(@Param("hotelId") Long hotelId);
}
