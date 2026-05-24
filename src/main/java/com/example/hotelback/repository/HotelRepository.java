package com.example.hotelback.repository;

import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.HotelRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // staffRoles fetch join хийж бүх буудлыг буцаана
    @Query("select distinct h from Hotel h left join fetch h.staffRoles sr left join fetch sr.user")
    List<Hotel> findAllWithStaff();

    // Нэг буудлыг staffRoles-тэй хамт авна
    @Query("select distinct h from Hotel h left join fetch h.staffRoles sr left join fetch sr.user where h.id = :id")
    Optional<Hotel> findByIdWithStaff(@Param("id") Long id);

    @Query("select distinct h from Hotel h left join fetch h.staffRoles sr left join fetch sr.user where lower(h.name) like lower(concat('%', :name, '%'))")
    List<Hotel> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("select distinct h from Hotel h join fetch h.staffRoles sr join sr.user where sr.user.id = :userId and sr.role = :role")
    List<Hotel> findByUserIdAndRole(@Param("userId") Long userId, @Param("role") HotelRole role);

    @Query("select distinct h from Hotel h join fetch h.staffRoles sr join sr.user where sr.user.id = :userId")
    List<Hotel> findByUserId(@Param("userId") Long userId);

    @Query("select count(hur) > 0 from HotelUserRole hur where hur.hotel.id = :hotelId and hur.user.id = :userId and hur.role = :role")
    boolean existsByIdAndUserRole(@Param("hotelId") Long hotelId, @Param("userId") Long userId, @Param("role") HotelRole role);

    @Query("select count(hur) > 0 from HotelUserRole hur where hur.hotel.id = :hotelId and hur.user.id = :userId")
    boolean existsByIdAndUserId(@Param("hotelId") Long hotelId, @Param("userId") Long userId);
}
