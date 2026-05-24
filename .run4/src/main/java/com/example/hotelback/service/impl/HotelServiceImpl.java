package com.example.hotelback.service.impl;


import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.HotelRepository;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.service.HotelService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    public HotelServiceImpl(HotelRepository hotelRepository, UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
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
    public List<Hotel> getHotelsByOwnerId(Long ownerId) {
        return hotelRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Hotel> getHotelsByReceptionistId(Long receptionistId) {
        return hotelRepository.findByReceptionistId(receptionistId);
    }

    @Override
    public List<Hotel> getAccessibleHotelsByUserId(Long userId) {
        LinkedHashMap<Long, Hotel> hotelsById = new LinkedHashMap<>();
        for (Hotel hotel : hotelRepository.findByOwnerId(userId)) {
            hotelsById.put(hotel.getId(), hotel);
        }
        for (Hotel hotel : hotelRepository.findByReceptionistId(userId)) {
            hotelsById.putIfAbsent(hotel.getId(), hotel);
        }
        return new ArrayList<>(hotelsById.values());
    }

    @Override
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    @Override
    public List<User> getReceptionistsByHotelId(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + hotelId);
        }
        return hotelRepository.findReceptionistsByHotelId(hotelId);
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
    public Hotel addReceptionist(Long hotelId, Long userId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + hotelId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Хэрэглэгч олдсонгүй: ID=" + userId));

        boolean alreadyAssigned = hotel.getReceptionists().stream()
                .anyMatch(receptionist -> receptionist.getId().equals(userId));
        if (!alreadyAssigned) {
            hotel.getReceptionists().add(user);
        }
        return hotelRepository.save(hotel);
    }

    @Override
    public Hotel removeReceptionist(Long hotelId, Long userId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + hotelId));

        hotel.getReceptionists().removeIf(receptionist -> receptionist.getId().equals(userId));
        return hotelRepository.save(hotel);
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
