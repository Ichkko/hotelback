package com.example.hotelback.service.impl;


import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.repository.HotelRepository;
import com.example.hotelback.service.HotelService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    public HotelServiceImpl(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Override
    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @Override
    public List<Hotel> createHotels(List<Hotel> hotels) {
        return hotelRepository.saveAll(hotels);
    }

    @Override
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @Override
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    @Override
    public Hotel updateHotel(Hotel hotel) {
        Hotel existing = hotelRepository.findById(hotel.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + hotel.getId()));
        existing.setName(hotel.getName());
        existing.setAddress(hotel.getAddress());
        existing.setAimag(hotel.getAimag());
        existing.setPhone(hotel.getPhone());
        existing.setDescription(hotel.getDescription());
        existing.setStartingPrice(hotel.getStartingPrice());
        existing.setCoverImageUrl(hotel.getCoverImageUrl());
        return hotelRepository.save(existing);
    }

    @Override
    public void deleteHotelById(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + id);
        }
        hotelRepository.deleteById(id);
    }

    @Override
    public List<Hotel> searchHotelsByName(String name) {
        return hotelRepository.findByNameContainingIgnoreCase(name);
    }
}
