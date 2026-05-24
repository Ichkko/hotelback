package com.example.hotelback.repository;

import com.example.hotelback.model.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, Long> {

    @Query("""
            select count(h) > 0
            from Highlight h
            join h.hotel hotel
            join hotel.owners o
            where h.id = :highlightId and o.id = :ownerId
            """)
    boolean existsByIdAndHotelOwnerId(@Param("highlightId") Long highlightId, @Param("ownerId") Long ownerId);
}
