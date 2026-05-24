package com.example.hotelback.mapper;

import com.example.hotelback.dto.*;
import com.example.hotelback.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class DtoMapper {

    public Booking toBooking(CreateBookingRequest request) {
        Booking booking = new Booking();
        applyBookingFields(booking, request.getCheckinDate(), request.getCheckoutDate(), request.getFirstName(),
                request.getLastName(), request.getEmail(), request.getPhone(), request.getGuestCount(),
                request.getSpecialRequests(), request.getRoomPrice(), request.getStatus());

        if (request.getUserId() != null) {
            User user = new User();
            user.setId(request.getUserId());
            booking.setUser(user);
        }

        Room room = new Room();
        room.setId(request.getRoomId());
        booking.setRoom(room);
        return booking;
    }

    public Booking toBooking(UpdateBookingRequest request) {
        Booking booking = new Booking();
        applyBookingFields(booking, request.getCheckinDate(), request.getCheckoutDate(), request.getFirstName(),
                request.getLastName(), request.getEmail(), request.getPhone(), request.getGuestCount(),
                request.getSpecialRequests(), request.getRoomPrice(), request.getStatus());
        return booking;
    }

    public Payment toPayment(CreatePaymentRequest request) {
        Payment payment = new Payment();
        Booking booking = new Booking();
        booking.setId(request.getBookingId());
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        return payment;
    }

    public Hotel toHotel(CreateHotelRequest request) {
        Hotel hotel = new Hotel();
        applyHotelFields(hotel, request.getName(), request.getAddress(), request.getAimag(), request.getPhone(),
                request.getDescription(), request.getStartingPrice(), request.getCoverImageUrl());
        hotel.setOwners(toUsers(request.getOwnerIds(), request.getOwnerId()));
        hotel.setReceptionists(toUsers(request.getReceptionistIds(), null));
        return hotel;
    }

    public Hotel toHotel(UpdateHotelRequest request) {
        Hotel hotel = new Hotel();
        applyHotelFields(hotel, request.getName(), request.getAddress(), request.getAimag(), request.getPhone(),
                request.getDescription(), request.getStartingPrice(), request.getCoverImageUrl());
        return hotel;
    }

    public Room toRoom(CreateRoomRequest request) {
        Room room = new Room();
        Hotel hotel = new Hotel();
        hotel.setId(request.getHotelId());
        room.setHotel(hotel);

        applyRoomFields(room, request.getRoomType(), request.getPrice(), request.getCapacity(), request.getStatus(),
                request.getRoomNumber(), request.getFloor(), request.getWing(), request.getSection(),
                request.getPositionX(), request.getPositionY(), request.getDetails(), request.getRoomDetails());

        return room;
    }

    public Room toRoom(UpdateRoomRequest request) {
        Room room = new Room();
        applyRoomFields(room, request.getRoomType(), request.getPrice(), request.getCapacity(), request.getStatus(),
                request.getRoomNumber(), request.getFloor(), request.getWing(), request.getSection(),
                request.getPositionX(), request.getPositionY(), request.getDetails(), request.getRoomDetails());

        return room;
    }

    public HotelResponse toHotelResponse(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .aimag(hotel.getAimag())
                .phone(hotel.getPhone())
                .description(hotel.getDescription())
                .startingPrice(hotel.getStartingPrice())
                .coverImageUrl(hotel.getCoverImageUrl())
                .ownerId(hotel.getOwnerId())
                .ownerIds(hotel.getOwnerIds())
                .receptionistIds(hotel.getReceptionistIds())
                .build();
    }

    public RoomResponse toRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .hotelId(room.getHotelId())
                .roomType(room.getRoomType())
                .price(room.getPrice())
                .capacity(room.getCapacity())
                .roomNumber(room.getRoomNumber())
                .floor(room.getFloor())
                .wing(room.getWing())
                .section(room.getSection())
                .positionX(room.getPositionX())
                .positionY(room.getPositionY())
                .status(room.getStatus())
                .details(room.getDetails().stream().map(this::toRoomDetailResponse).toList())
                .roomDetails(room.getRoomDetails())
                .build();
    }

    public BookingResponse toBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .user(toUserSummaryResponse(booking.getUser()))
                .room(booking.getRoom() != null ? toRoomResponse(booking.getRoom()) : null)
                .checkinDate(booking.getCheckinDate())
                .checkoutDate(booking.getCheckoutDate())
                .firstName(booking.getFirstName())
                .lastName(booking.getLastName())
                .email(booking.getEmail())
                .phone(booking.getPhone())
                .guestCount(booking.getGuestCount())
                .specialRequests(booking.getSpecialRequests())
                .nights(booking.getNights())
                .roomPrice(booking.getRoomPrice())
                .serviceFee(booking.getServiceFee())
                .totalPrice(booking.getTotalPrice())
                .bookingNumber(booking.getBookingNumber())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    public PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking() != null ? payment.getBooking().getId() : null)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .build();
    }

    public UserSummaryResponse toUserSummaryResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }


    public RoomDetail toRoomDetail(RoomDetailRequest request) {
        RoomDetail detail = new RoomDetail();
        detail.setCategory(request.getCategory());
        detail.setLabel(request.getLabel());
        detail.setValue(request.getValue());
        detail.setDisplayOrder(request.getDisplayOrder());
        return detail;
    }

    public RoomDetailResponse toRoomDetailResponse(RoomDetail detail) {
        return RoomDetailResponse.builder()
                .id(detail.getId())
                .category(detail.getCategory())
                .label(detail.getLabel())
                .value(detail.getValue())
                .displayOrder(detail.getDisplayOrder())
                .build();
    }

    private void applyBookingFields(Booking booking,
                                    java.time.LocalDate checkinDate,
                                    java.time.LocalDate checkoutDate,
                                    String firstName,
                                    String lastName,
                                    String email,
                                    String phone,
                                    Integer guestCount,
                                    String specialRequests,
                                    java.math.BigDecimal roomPrice,
                                    BookingStatus status) {
        booking.setCheckinDate(checkinDate);
        booking.setCheckoutDate(checkoutDate);
        booking.setFirstName(firstName);
        booking.setLastName(lastName);
        booking.setEmail(email);
        booking.setPhone(phone);
        booking.setGuestCount(guestCount);
        booking.setSpecialRequests(specialRequests);
        booking.setRoomPrice(roomPrice);
        booking.setStatus(status);
    }

    private void applyHotelFields(Hotel hotel, String name, String address, String aimag, String phone,
                                  String description, Double startingPrice, String coverImageUrl) {
        hotel.setName(name);
        hotel.setAddress(address);
        hotel.setAimag(aimag);
        hotel.setPhone(phone);
        hotel.setDescription(description);
        hotel.setStartingPrice(startingPrice);
        hotel.setCoverImageUrl(coverImageUrl);
    }

    private List<User> toUsers(List<Long> ownerIds, Long ownerId) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (ownerIds != null) {
            ids.addAll(ownerIds);
        }
        if (ownerId != null) {
            ids.add(ownerId);
        }

        List<User> owners = new ArrayList<>();
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            User owner = new User();
            owner.setId(id);
            owners.add(owner);
        }
        return owners;
    }

    private void applyRoomFields(Room room, String roomType, Double price, Integer capacity, RoomStatus status,
                                 String roomNumber, Integer floor, String wing, String section,
                                 Double positionX, Double positionY, java.util.List<RoomDetailRequest> details,
                                 String roomDetails) {
        room.setRoomType(roomType);
        room.setPrice(price);
        room.setCapacity(capacity);
        room.setRoomNumber(roomNumber);
        room.setFloor(floor);
        room.setWing(wing);
        room.setSection(section);
        room.setPositionX(positionX);
        room.setPositionY(positionY);
        room.setStatus(status);

        room.getDetails().clear();
        if (details != null) {
            int fallbackOrder = 0;
            for (RoomDetailRequest detailRequest : details) {
                RoomDetail detail = toRoomDetail(detailRequest);
                if (detail.getDisplayOrder() == null) {
                    detail.setDisplayOrder(fallbackOrder);
                }
                detail.setRoom(room);
                room.getDetails().add(detail);
                fallbackOrder++;
            }
        }

        room.setRoomDetails(roomDetails);
    }
}
