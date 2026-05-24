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
            join h.staffRoles hur
            where a.id = :amenityId and hur.user.id = :userId and hur.role = com.example.hotelback.model.HotelRole.OWNER
            """)
    boolean existsByIdAndHotelOwnerId(@Param("amenityId") Long amenityId, @Param("userId") Long userId);
}
