package com.example.hotelback.repository;

import com.example.hotelback.model.HotelRole;
import com.example.hotelback.model.HotelUserRole;
import com.example.hotelback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelUserRoleRepository extends JpaRepository<HotelUserRole, Long> {

    boolean existsByHotelIdAndUserIdAndRole(Long hotelId, Long userId, HotelRole role);

    boolean existsByHotelIdAndUserId(Long hotelId, Long userId);

    @Query("select hur.user from HotelUserRole hur where hur.hotel.id = :hotelId and hur.role = :role")
    List<User> findUsersByHotelIdAndRole(@Param("hotelId") Long hotelId, @Param("role") HotelRole role);

    @Query("select hur.hotel.id from HotelUserRole hur where hur.user.id = :userId")
    List<Long> findHotelIdsByUserId(@Param("userId") Long userId);

    @Query("select hur from HotelUserRole hur where hur.hotel.id = :hotelId and hur.user.id = :userId")
    List<HotelUserRole> findByHotelIdAndUserId(@Param("hotelId") Long hotelId, @Param("userId") Long userId);

    @Query("select hur from HotelUserRole hur where hur.hotel.id = :hotelId and hur.user.id = :userId and hur.role = :role")
    Optional<HotelUserRole> findByHotelIdAndUserIdAndRole(@Param("hotelId") Long hotelId,
                                                          @Param("userId") Long userId,
                                                          @Param("role") HotelRole role);

    @Modifying
    @Transactional
    @Query("delete from HotelUserRole hur where hur.hotel.id = :hotelId and hur.user.id = :userId and hur.role = :role")
    void deleteByHotelIdAndUserIdAndRole(@Param("hotelId") Long hotelId,
                                         @Param("userId") Long userId,
                                         @Param("role") HotelRole role);

    @Modifying
    @Transactional
    @Query("delete from HotelUserRole hur where hur.hotel.id = :hotelId and hur.user.id = :userId")
    void deleteByHotelIdAndUserId(@Param("hotelId") Long hotelId, @Param("userId") Long userId);

    @Query("select hur from HotelUserRole hur join fetch hur.user where hur.hotel.id = :hotelId")
    List<HotelUserRole> findAllByHotelIdWithUser(@Param("hotelId") Long hotelId);
}
