package com.example.hotelback.service;

import com.example.hotelback.model.Hotel;

import java.util.List;
import java.util.Optional;

public interface HotelService {

    Hotel createHotel(Hotel hotel);

    List<Hotel> createHotels(List<Hotel> hotels);

    List<Hotel> getAllHotels();

    List<Hotel> getHotelsByOwnerId(Long ownerId);

    Optional<Hotel> getHotelById(Long id);

    Hotel updateHotel(Hotel hotel);

    void deleteHotelById(Long id);

    List<Hotel> searchHotelsByName(String name);


}