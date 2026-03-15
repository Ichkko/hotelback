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
}
