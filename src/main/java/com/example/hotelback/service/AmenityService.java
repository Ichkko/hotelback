package com.example.hotelback.service;

import com.example.hotelback.model.Amenity;

import java.util.List;
import java.util.Optional;

public interface AmenityService {

    Amenity createAmenity(Amenity amenity);

    List<Amenity> getAllAmenities();

    Optional<Amenity> getAmenityById(Long id);

    Amenity updateAmenity(Long id, Amenity amenity);

    void deleteAmenityById(Long id);
}
