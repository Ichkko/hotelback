package com.example.hotelback.controller;

import com.example.hotelback.model.Amenity;
import com.example.hotelback.security.OwnershipAccessService;
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
    private final OwnershipAccessService ownershipAccessService;

    public AmenityController(AmenityService amenityService,
                             OwnershipAccessService ownershipAccessService) {
        this.amenityService = amenityService;
        this.ownershipAccessService = ownershipAccessService;
    }

    @PostMapping
    public ResponseEntity<Amenity> createAmenity(@RequestBody Amenity amenity,
                                                 @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertHotelOwnerOrAdmin(amenity.getHotelId(), principal);
        return ResponseEntity.ok(amenityService.createAmenity(amenity));
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
        ownershipAccessService.assertAmenityHotelOwnerOrAdmin(id, principal);
        return ResponseEntity.ok(amenityService.updateAmenity(id, amenity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAmenity(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails principal) {
        ownershipAccessService.assertAmenityHotelOwnerOrAdmin(id, principal);
        amenityService.deleteAmenityById(id);
        return ResponseEntity.ok("Amenity with ID " + id + " deleted successfully");
    }
}
