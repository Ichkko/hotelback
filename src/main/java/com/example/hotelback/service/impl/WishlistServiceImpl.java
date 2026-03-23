package com.example.hotelback.service.impl;

import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Room;
import com.example.hotelback.model.User;
import com.example.hotelback.model.Wishlist;
import com.example.hotelback.repository.RoomRepository;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.repository.WishlistRepository;
import com.example.hotelback.service.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               UserRepository userRepository,
                               RoomRepository roomRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public Wishlist addToWishlist(Long userId, Long roomId) {
        if (wishlistRepository.existsByUser_IdAndRoom_Id(userId, roomId)) {

            throw new IllegalStateException("Тухайн өрөө wishlist-д аль хэдийн нэмэгдсэн байна");

        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Хэрэглэгч олдсонгүй: ID=" + userId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Өрөө олдсонгүй: ID=" + roomId));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setRoom(room);

        return wishlistRepository.save(wishlist);
    }

    @Override
    public List<Wishlist> getWishlistByUserId(Long userId) {
        return wishlistRepository.findByUser_Id(userId);
    }

    @Override
    public boolean isInWishlist(Long userId, Long roomId) {
        return wishlistRepository.existsByUser_IdAndRoom_Id(userId, roomId);
    }

    @Override
    @Transactional
    public void removeById(Long id) {
        if (!wishlistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Wishlist бичлэг олдсонгүй: ID=" + id);
        }
        wishlistRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void removeByUserAndRoom(Long userId, Long roomId) {
        Wishlist wishlist = wishlistRepository.findByUser_IdAndRoom_Id(userId, roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist бичлэг олдсонгүй"));
        wishlistRepository.delete(wishlist);
    }
}
