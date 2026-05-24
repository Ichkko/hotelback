package com.example.hotelback.service.impl;

import com.example.hotelback.exception.BadRequestException;
import com.example.hotelback.exception.ErrorCode;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Hotel;
import com.example.hotelback.model.HotelPermission;
import com.example.hotelback.model.HotelRole;
import com.example.hotelback.model.HotelUserRole;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.HotelRepository;
import com.example.hotelback.repository.HotelUserRoleRepository;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.security.OwnershipAccessService;
import com.example.hotelback.service.HotelService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelUserRoleRepository hotelUserRoleRepository;
    private final UserRepository userRepository;
    private final OwnershipAccessService ownershipAccessService;

    public HotelServiceImpl(HotelRepository hotelRepository,
                            HotelUserRoleRepository hotelUserRoleRepository,
                            UserRepository userRepository,
                            OwnershipAccessService ownershipAccessService) {
        this.hotelRepository = hotelRepository;
        this.hotelUserRoleRepository = hotelUserRoleRepository;
        this.userRepository = userRepository;
        this.ownershipAccessService = ownershipAccessService;
    }

    @Override
    @Transactional
    public Hotel createHotel(Hotel hotel, Long ownerUserId, UserDetails principal) {
        Hotel saved = hotelRepository.save(hotel);
        if (ownerUserId != null) {
            if (principal != null
                    && !ownershipAccessService.isAdmin(principal)
                    && !ownerUserId.equals(ownershipAccessService.resolveCurrentUserId(principal))) {
                throw new IllegalArgumentException("Өөр хэрэглэгчид owner эрхээр буудал үүсгэх боломжгүй");
            }
            assignRole(saved, ownerUserId, HotelRole.OWNER);
        }
        return saved;
    }

    @Override
    @Transactional
    public List<Hotel> createHotels(List<Hotel> hotels, Long ownerUserId, UserDetails principal) {
        List<Hotel> saved = hotelRepository.saveAll(hotels);
        if (ownerUserId != null) {
            if (principal != null
                    && !ownershipAccessService.isAdmin(principal)
                    && !ownerUserId.equals(ownershipAccessService.resolveCurrentUserId(principal))) {
                throw new IllegalArgumentException("Өөр хэрэглэгчид owner эрхээр буудал үүсгэх боломжгүй");
            }
            saved.forEach(h -> assignRole(h, ownerUserId, HotelRole.OWNER));
        }
        return saved;
    }

    @Override
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAllWithStaff();
    }

    @Override
    public List<Hotel> getHotelsByOwnerId(UserDetails principal) {
        if (ownershipAccessService.isAdmin(principal)) {
            return hotelRepository.findAllWithStaff();
        }
        Long currentUserId = ownershipAccessService.resolveCurrentUserId(principal);
        return hotelRepository.findByUserIdAndRole(currentUserId, HotelRole.OWNER);
    }

    @Override
    public List<Hotel> getAccessibleHotelsByUserId(UserDetails principal) {
        Long currentUserId = ownershipAccessService.resolveCurrentUserId(principal);
        return hotelRepository.findByUserId(currentUserId);
    }

    @Override
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findByIdWithStaff(id);
    }

    // ── Staff listing ─────────────────────────────────────────────────────────

    @Override
    public List<HotelUserRole> getStaffByHotelId(Long hotelId, UserDetails principal) {
        if (principal != null) {
            ownershipAccessService.assertHotelPermission(
                    hotelId, principal, HotelPermission.HOTEL_VIEW,
                    "Та зөвхөн өөрийн буудлын ажилтнуудыг харах эрхтэй");
        }
        validateHotelExists(hotelId);
        return hotelUserRoleRepository.findAllByHotelIdWithUser(hotelId);
    }

    @Override
    public List<User> getReceptionistsByHotelId(Long hotelId, UserDetails principal) {
        if (principal != null) {
            ownershipAccessService.assertHotelPermission(
                    hotelId, principal, HotelPermission.HOTEL_VIEW,
                    "Та зөвхөн өөрийн буудлын staff хүрээнд хандах эрхтэй");
        }
        return hotelUserRoleRepository.findUsersByHotelIdAndRole(hotelId, HotelRole.RECEPTION);
    }

    // ── Staff management ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public Hotel assignStaff(Long hotelId, Long userId, HotelRole role, UserDetails principal) {
        if (principal != null) {
            ownershipAccessService.assertHotelPermission(
                    hotelId, principal, HotelPermission.STAFF_MANAGE,
                    "Та зөвхөн өөрийн буудлын ажилтны эрхийг удирдах боломжтой");
        }
        Hotel hotel = validateHotelExists(hotelId);
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Хэрэглэгч олдсонгүй: ID=" + userId));

        if (hotelUserRoleRepository.existsByHotelIdAndUserId(hotelId, userId)) {
            throw new BadRequestException(ErrorCode.VALIDATION_ERROR,
                    "Хэрэглэгч энэ буудалд аль хэдийн үүрэгтэй байна. Үүргийг өөрчлөхийн тулд update ашиглана уу.");
        }
        assignRole(hotel, userId, role);
        return hotelRepository.findByIdWithStaff(hotelId).orElseThrow();
    }

    @Override
    @Transactional
    public Hotel updateStaffRole(Long hotelId, Long userId, HotelRole role, UserDetails principal) {
        if (principal != null) {
            ownershipAccessService.assertHotelPermission(
                    hotelId, principal, HotelPermission.STAFF_MANAGE,
                    "Та зөвхөн өөрийн буудлын ажилтны эрхийг удирдах боломжтой");
        }
        validateHotelExists(hotelId);

        List<HotelUserRole> existing = hotelUserRoleRepository.findByHotelIdAndUserId(hotelId, userId);
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Тухайн хэрэглэгч энэ буудалд үүрэггүй байна: userId=" + userId);
        }
        existing.forEach(hur -> hur.setRole(role));
        hotelUserRoleRepository.saveAll(existing);
        return hotelRepository.findByIdWithStaff(hotelId).orElseThrow();
    }

    @Override
    @Transactional
    public Hotel removeStaff(Long hotelId, Long userId, UserDetails principal) {
        if (principal != null) {
            ownershipAccessService.assertHotelPermission(
                    hotelId, principal, HotelPermission.STAFF_MANAGE,
                    "Та зөвхөн өөрийн буудлын ажилтны эрхийг удирдах боломжтой");
        }
        validateHotelExists(hotelId);
        hotelUserRoleRepository.deleteByHotelIdAndUserId(hotelId, userId);
        return hotelRepository.findByIdWithStaff(hotelId).orElseThrow();
    }

    // ── Deprecated receptionist shortcuts ────────────────────────────────────

    @Override
    @Deprecated
    @Transactional
    public Hotel addReceptionist(Long hotelId, Long userId, UserDetails principal) {
        if (principal != null) {
            ownershipAccessService.assertHotelPermission(
                    hotelId, principal, HotelPermission.STAFF_MANAGE,
                    "Та зөвхөн өөрийн буудлын ажилтны эрхийг удирдах боломжтой");
        }
        Hotel hotel = validateHotelExists(hotelId);
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Хэрэглэгч олдсонгүй: ID=" + userId));
        if (!hotelUserRoleRepository.existsByHotelIdAndUserId(hotelId, userId)) {
            assignRole(hotel, userId, HotelRole.RECEPTION);
        }
        return hotelRepository.findByIdWithStaff(hotelId).orElseThrow();
    }

    @Override
    @Deprecated
    @Transactional
    public Hotel removeReceptionist(Long hotelId, Long userId, UserDetails principal) {
        if (principal != null) {
            ownershipAccessService.assertHotelPermission(
                    hotelId, principal, HotelPermission.STAFF_MANAGE,
                    "Та зөвхөн өөрийн буудлын ажилтны эрхийг удирдах боломжтой");
        }
        validateHotelExists(hotelId);
        hotelUserRoleRepository.deleteByHotelIdAndUserIdAndRole(hotelId, userId, HotelRole.RECEPTION);
        return hotelRepository.findByIdWithStaff(hotelId).orElseThrow();
    }

    // ── Hotel CRUD ────────────────────────────────────────────────────────────

    @Override
    public Hotel updateHotel(Hotel hotel, UserDetails principal) {
        if (principal != null) {
            ownershipAccessService.assertHotelPermission(
                    hotel.getId(), principal, HotelPermission.HOTEL_UPDATE,
                    "Та зөвхөн өөрийн буудлыг удирдах эрхтэй");
        }
        Hotel existing = hotelRepository.findByIdWithStaff(hotel.getId())
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
    public void deleteHotelById(Long id, UserDetails principal) {
        if (principal != null) {
            ownershipAccessService.assertHotelPermission(
                    id, principal, HotelPermission.HOTEL_UPDATE,
                    "Та зөвхөн өөрийн буудлыг устгах эрхтэй");
        }
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + id);
        }
        hotelRepository.deleteById(id);
    }

    @Override
    public List<Hotel> searchHotelsByName(String name) {
        return hotelRepository.findByNameContainingIgnoreCase(name);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Hotel validateHotelExists(Long hotelId) {
        return hotelRepository.findByIdWithStaff(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Зочид буудал олдсонгүй: ID=" + hotelId));
    }

    private void assignRole(Hotel hotel, Long userId, HotelRole role) {
        HotelUserRole hur = new HotelUserRole();
        hur.setHotel(hotel);
        User user = new User();
        user.setId(userId);
        hur.setUser(user);
        hur.setRole(role);
        hotelUserRoleRepository.save(hur);
    }
}
