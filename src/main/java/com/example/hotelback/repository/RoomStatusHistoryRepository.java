package com.example.hotelback.repository;

import com.example.hotelback.model.RoomStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomStatusHistoryRepository extends JpaRepository<RoomStatusHistory, Long> {

    @Query("""
            select h
            from RoomStatusHistory h
            join fetch h.room r
            where r.hotel.id = :hotelId
              and h.startDate < :to
              and (h.endDate is null or h.endDate > :from)
            order by r.id, h.startDate
            """)
    List<RoomStatusHistory> findByHotelIdOverlappingDates(@Param("hotelId") Long hotelId,
                                                          @Param("from") LocalDate from,
                                                          @Param("to") LocalDate to);

    @Query("""
            select h
            from RoomStatusHistory h
            join fetch h.room r
            where r.id = :roomId
            order by h.startDate desc, h.id desc
            """)
    List<RoomStatusHistory> findByRoomId(@Param("roomId") Long roomId);
}
