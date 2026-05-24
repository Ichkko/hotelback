package com.example.hotelback.service;

import com.example.hotelback.model.Amenity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface AmenityService {

    Amenity createAmenity(Amenity amenity, UserDetails principal);

    List<Amenity> getAllAmenities();

    Optional<Amenity> getAmenityById(Long id);

    Amenity updateAmenity(Long id, Amenity amenity, UserDetails principal);

    void deleteAmenityById(Long id, UserDetails principal);
}
