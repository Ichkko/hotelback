package com.example.hotelback.repository;

import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.HotelRole;
import com.example.hotelback.model.HotelUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelUserRoleRepository extends JpaRepository<HotelUserRole, Long> {

    boolean existsByHotelIdAndUserIdAndRole(Long hotelId, Long userId, HotelRole role);

    @Query("select hur.hotel from HotelUserRole hur where hur.user.id = :userId")
    List<Hotel> findHotelsByUserId(@Param("userId") Long userId);
}
