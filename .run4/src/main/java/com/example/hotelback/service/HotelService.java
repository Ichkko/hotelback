package com.example.hotelback.service;

import com.example.hotelback.model.Hotel;

import com.example.hotelback.model.User;

import java.util.List;
import java.util.Optional;

public interface HotelService {

    Hotel createHotel(Hotel hotel);

    List<Hotel> createHotels(List<Hotel> hotels);

    List<Hotel> getAllHotels();

    List<Hotel> getHotelsByOwnerId(Long ownerId);

    List<Hotel> getHotelsByReceptionistId(Long receptionistId);

    List<Hotel> getAccessibleHotelsByUserId(Long userId);

    Optional<Hotel> getHotelById(Long id);

    List<User> getReceptionistsByHotelId(Long hotelId);

    Hotel updateHotel(Hotel hotel);

    Hotel addReceptionist(Long hotelId, Long userId);

    Hotel removeReceptionist(Long hotelId, Long userId);

    void deleteHotelById(Long id);

    List<Hotel> searchHotelsByName(String name);


}
