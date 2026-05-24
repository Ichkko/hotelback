package com.example.hotelback.repository;

import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query(value = "select h from Hotel h where lower(h.name) like lower(concat('%', :name, '%'))", countQuery = "select count(h) from Hotel h where lower(h.name) like lower(concat('%', :name, '%'))")
    List<Hotel> findByNameContainingIgnoreCase(@Param("name") String name);

    boolean existsByIdAndOwners_Id(Long hotelId, Long ownerId);

    boolean existsByIdAndReceptionists_Id(Long hotelId, Long receptionistId);

    @Query("select distinct h from Hotel h join h.owners o where o.id = :ownerId")
    List<Hotel> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("select distinct h from Hotel h join h.receptionists r where r.id = :receptionistId")
    List<Hotel> findByReceptionistId(@Param("receptionistId") Long receptionistId);

    @Query("select r from Hotel h join h.receptionists r where h.id = :hotelId")
    List<User> findReceptionistsByHotelId(@Param("hotelId") Long hotelId);

}
