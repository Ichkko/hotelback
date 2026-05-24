package com.example.hotelback.service;

import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.HotelRole;
import com.example.hotelback.model.HotelUserRole;
import com.example.hotelback.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface HotelService {

    default Hotel createHotel(Hotel hotel) {
        return createHotel(hotel, null, null);
    }

    Hotel createHotel(Hotel hotel, Long ownerUserId, UserDetails principal);

    default List<Hotel> createHotels(List<Hotel> hotels, Long ownerUserId) {
        return createHotels(hotels, ownerUserId, null);
    }

    List<Hotel> createHotels(List<Hotel> hotels, Long ownerUserId, UserDetails principal);

    List<Hotel> getAllHotels();

    List<Hotel> getHotelsByOwnerId(UserDetails principal);

    List<Hotel> getAccessibleHotelsByUserId(UserDetails principal);

    Optional<Hotel> getHotelById(Long id);

    // ── Staff listing ─────────────────────────────────────────────────────────

    List<HotelUserRole> getStaffByHotelId(Long hotelId, UserDetails principal);

    default List<User> getReceptionistsByHotelId(Long hotelId) {
        return getReceptionistsByHotelId(hotelId, null);
    }

    List<User> getReceptionistsByHotelId(Long hotelId, UserDetails principal);

    // ── Staff management ──────────────────────────────────────────────────────

    /**
     * Assign any hotel role to a user. Throws if the user already has a role at this hotel.
     */
    Hotel assignStaff(Long hotelId, Long userId, HotelRole role, UserDetails principal);

    /**
     * Update an existing staff member's role. Throws if the user has no role at this hotel.
     */
    Hotel updateStaffRole(Long hotelId, Long userId, HotelRole role, UserDetails principal);

    /**
     * Remove a staff member from the hotel entirely.
     */
    Hotel removeStaff(Long hotelId, Long userId, UserDetails principal);

    /** @deprecated Use {@link #assignStaff} with {@link HotelRole#RECEPTION} instead. */
    @Deprecated
    default Hotel addReceptionist(Long hotelId, Long userId) {
        return addReceptionist(hotelId, userId, null);
    }

    /** @deprecated Use {@link #assignStaff} with {@link HotelRole#RECEPTION} instead. */
    @Deprecated
    Hotel addReceptionist(Long hotelId, Long userId, UserDetails principal);

    /** @deprecated Use {@link #removeStaff} instead. */
    @Deprecated
    default Hotel removeReceptionist(Long hotelId, Long userId) {
        return removeReceptionist(hotelId, userId, null);
    }

    /** @deprecated Use {@link #removeStaff} instead. */
    @Deprecated
    Hotel removeReceptionist(Long hotelId, Long userId, UserDetails principal);

    // ── Hotel CRUD ────────────────────────────────────────────────────────────

    default Hotel updateHotel(Hotel hotel) {
        return updateHotel(hotel, null);
    }

    Hotel updateHotel(Hotel hotel, UserDetails principal);

    default void deleteHotelById(Long id) {
        deleteHotelById(id, null);
    }

    void deleteHotelById(Long id, UserDetails principal);

    List<Hotel> searchHotelsByName(String name);
}
