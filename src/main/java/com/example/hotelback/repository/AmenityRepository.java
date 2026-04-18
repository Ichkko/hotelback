package com.example.hotelback.repository;

import com.example.hotelback.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    @Query("select a.hotel.id from Amenity a where a.id = :amenityId")
    Optional<Long> findHotelIdByAmenityId(@Param("amenityId") Long amenityId);
}
