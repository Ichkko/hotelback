package com.example.hotelback.controller;

import com.example.hotelback.model.Amenity;
import com.example.hotelback.service.AmenityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    private final AmenityService amenityService;

    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    @PostMapping
    public ResponseEntity<Amenity> createAmenity(@RequestBody Amenity amenity,
                                                 @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(amenityService.createAmenity(amenity, principal));
    }

    @GetMapping
    public ResponseEntity<List<Amenity>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Amenity> getAmenityById(@PathVariable Long id) {
        return amenityService.getAmenityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Amenity> updateAmenity(@PathVariable Long id,
                                                  @RequestBody Amenity amenity,
                                                  @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, amenity, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAmenity(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails principal) {
        amenityService.deleteAmenityById(id, principal);
        return ResponseEntity.ok("Amenity with ID " + id + " deleted successfully");
    }
}
