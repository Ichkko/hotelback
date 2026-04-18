package com.example.hotelback.repository;

import com.example.hotelback.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query(value = "select h from Hotel h where lower(h.name) like lower(concat('%', :name, '%'))", countQuery = "select count(h) from Hotel h where lower(h.name) like lower(concat('%', :name, '%'))")
    List<Hotel> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("select h.owner.id from Hotel h where h.id = :hotelId")
    Optional<Long> findOwnerIdById(@Param("hotelId") Long hotelId);
 
    @Query("select h from Hotel h where h.owner.id = :ownerId")
    List<Hotel> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("select hur.hotel from HotelUserRole hur where hur.user.id = :userId")
    List<Hotel> findByUserId(@Param("userId") Long userId);
}
