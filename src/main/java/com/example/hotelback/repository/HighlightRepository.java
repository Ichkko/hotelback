package com.example.hotelback.repository;

import com.example.hotelback.model.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, Long> {

    @Query("select h.hotel.id from Highlight h where h.id = :highlightId")
    Optional<Long> findHotelIdByHighlightId(@Param("highlightId") Long highlightId);
}
