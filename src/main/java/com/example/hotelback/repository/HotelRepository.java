package com.example.hotelback.repository;

import com.example.hotelback.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query(value = "select h from Hotel h where lower(h.name) like lower(concat('%', :name, '%'))", countQuery = "select count(h) from Hotel h where lower(h.name) like lower(concat('%', :name, '%'))")
    List<Hotel> findByNameContainingIgnoreCase(@Param("name") String name);
}