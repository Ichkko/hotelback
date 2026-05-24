package com.example.hotelback.service.impl;

import com.example.hotelback.model.Amenity;
import com.example.hotelback.repository.AmenityRepository;
import com.example.hotelback.service.AmenityService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;

    public AmenityServiceImpl(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    @Override
    public Amenity createAmenity(Amenity amenity) {
        return amenityRepository.save(amenity);
    }

    @Override
    public List<Amenity> getAllAmenities() {
        return amenityRepository.findAll();
    }

    @Override
    public Optional<Amenity> getAmenityById(Long id) {
        return amenityRepository.findById(id);
    }

    @Override
    public Amenity updateAmenity(Long id, Amenity amenity) {
        Amenity existing = amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found"));
        BeanUtils.copyProperties(amenity, existing, "id", "createdAt", "updatedAt");
        return amenityRepository.save(existing);
    }

    @Override
    public void deleteAmenityById(Long id) {
        amenityRepository.deleteById(id);
    }
}
