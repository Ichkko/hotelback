package com.example.hotelback.repository;

import com.example.hotelback.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    @Query("""
            select count(a) > 0
            from Amenity a
            join a.hotel h
            join h.owners o
            where a.id = :amenityId and o.id = :ownerId
            """)
    boolean existsByIdAndHotelOwnerId(@Param("amenityId") Long amenityId, @Param("ownerId") Long ownerId);
}
