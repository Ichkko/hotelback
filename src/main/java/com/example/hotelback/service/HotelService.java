package com.example.hotelback.service;

import com.example.hotelback.model.Hotel;

import java.util.List;
import java.util.Optional;

public interface HotelService {

    Hotel createHotel(Hotel hotel, Long ownerUserId);

    List<Hotel> createHotels(List<Hotel> hotels, Long ownerUserId);

    List<Hotel> getAllHotels();

    List<Hotel> getHotelsByUserId(Long userId);

    Optional<Hotel> getHotelById(Long id);

    Hotel updateHotel(Hotel hotel);

    void deleteHotelById(Long id);

    List<Hotel> searchHotelsByName(String name);


}