package com.example.hotelback.service.impl;


import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.HotelRole;
import com.example.hotelback.model.HotelUserRole;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.HotelRepository;
import com.example.hotelback.repository.HotelUserRoleRepository;
import com.example.hotelback.service.HotelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelUserRoleRepository hotelUserRoleRepository;

    public HotelServiceImpl(HotelRepository hotelRepository,
                            HotelUserRoleRepository hotelUserRoleRepository) {
        this.hotelRepository = hotelRepository;
        this.hotelUserRoleRepository = hotelUserRoleRepository;
    }

    @Override
    @Transactional
    public Hotel createHotel(Hotel hotel, Long ownerUserId) {
        Hotel saved = hotelRepository.save(hotel);
        if (ownerUserId != null) {
            assignOwner(saved, ownerUserId);
        }
        return saved;
    }

    @Override
    @Transactional
    public List<Hotel> createHotels(List<Hotel> hotels, Long ownerUserId) {
        List<Hotel> saved = hotelRepository.saveAll(hotels);
        if (ownerUserId != null) {
            saved.forEach(h -> assignOwner(h, ownerUserId));
        }
        return saved;
    }

    @Override
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @Override
    public List<Hotel> getHotelsByUserId(Long userId) {
        return hotelRepository.findByUserId(userId);
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

    private void assignOwner(Hotel hotel, Long userId) {
        HotelUserRole role = new HotelUserRole();
        role.setHotel(hotel);
        User user = new User();
        user.setId(userId);
        role.setUser(user);
        role.setRole(HotelRole.OWNER);
        hotelUserRoleRepository.save(role);
    }
}
