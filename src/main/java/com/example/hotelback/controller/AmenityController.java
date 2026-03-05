package com.example.hotelback.controller;

import com.example.hotelback.model.Amenity;
import com.example.hotelback.service.AmenityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
 
@RequestMapping("/api/amenities")
 
@RequestMapping("/api/amenitys")
 
public class AmenityController {

    private final AmenityService amenityService;

    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    @PostMapping
    public ResponseEntity<Amenity> createAmenity(@RequestBody Amenity amenity) {
        return ResponseEntity.ok(amenityService.createAmenity(amenity));
    }

    @GetMapping
 
    public ResponseEntity<List<Amenity>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
 
    public ResponseEntity<List<Amenity>> getAllAmenitys() {
        return ResponseEntity.ok(amenityService.getAllAmenitys());
 
    }

    @GetMapping("/{id}")
    public ResponseEntity<Amenity> getAmenityById(@PathVariable Long id) {
        return amenityService.getAmenityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Amenity> updateAmenity(@PathVariable Long id, @RequestBody Amenity amenity) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, amenity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenityById(id);
        return ResponseEntity.ok("Amenity with ID " + id + " deleted successfully");
    }
}
