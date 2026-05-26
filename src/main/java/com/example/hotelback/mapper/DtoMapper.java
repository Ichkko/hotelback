package com.example.hotelback.mapper;

import com.example.hotelback.dto.*;
import com.example.hotelback.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
                .ownerIds(nullSafeList(hotel.getOwnerIds()))
                .managerIds(nullSafeList(hotel.getManagerIds()))
                .receptionistIds(nullSafeList(hotel.getReceptionistIds()))
                .accountantIds(nullSafeList(hotel.getAccountantIds()))
                .build();
    }

    public HotelResponse toHotelAccessResponse(Hotel hotel, Long currentUserId) {
        HotelMembershipResponse membership = buildMembership(hotel, currentUserId);
        HotelPermissionsResponse permissions = buildPermissions(hotel, currentUserId);

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
                .ownerIds(nullSafeList(hotel.getOwnerIds()))
                .managerIds(nullSafeList(hotel.getManagerIds()))
                .receptionistIds(nullSafeList(hotel.getReceptionistIds()))
                .accountantIds(nullSafeList(hotel.getAccountantIds()))
                .membership(membership)
                .permissions(permissions)
                .build();
    }

    private <T> List<T> nullSafeList(List<T> list) {
        return list != null ? list : List.of();
    }

    private HotelMembershipResponse buildMembership(Hotel hotel, Long currentUserId) {
        if (currentUserId == null) {
            return null;
        }

        HotelRole effectiveRole = hotel.getStaffRoles().stream()
                .filter(Objects::nonNull)
                .filter(hur -> hur.getUser() != null && Objects.equals(hur.getUser().getId(), currentUserId))
                .map(HotelUserRole::getRole)
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(this::rolePriority))
                .orElse(null);

        if (effectiveRole == null) {
            return null;
        }

        return HotelMembershipResponse.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .role(effectiveRole.name())
                .build();
    }

    private HotelPermissionsResponse buildPermissions(Hotel hotel, Long currentUserId) {
        if (currentUserId == null) {
            return null;
        }

        List<HotelRole> roles = hotel.getStaffRoles().stream()
                .filter(Objects::nonNull)
                .filter(hur -> hur.getUser() != null && Objects.equals(hur.getUser().getId(), currentUserId))
                .map(HotelUserRole::getRole)
                .filter(Objects::nonNull)
                .toList();

        if (roles.isEmpty()) {
            return null;
        }

        return HotelPermissionsResponse.builder()
                .canManageHotel(hasPermission(roles, HotelPermission.HOTEL_UPDATE))
                .canManageRooms(hasPermission(roles, HotelPermission.ROOM_MANAGE))
                .canViewBookings(hasPermission(roles, HotelPermission.BOOKING_VIEW))
                .canUpdateBookings(hasPermission(roles, HotelPermission.BOOKING_UPDATE))
                .canManagePayments(hasPermission(roles, HotelPermission.PAYMENT_MANAGE))
                .canManageStaff(hasPermission(roles, HotelPermission.STAFF_MANAGE))
                .canViewReports(hasPermission(roles, HotelPermission.REPORT_VIEW))
                .build();
    }

    private boolean hasPermission(List<HotelRole> roles, HotelPermission permission) {
        return roles.stream().anyMatch(role -> role.hasPermission(permission));
    }

    private int rolePriority(HotelRole role) {
        return switch (role) {
            case OWNER -> 4;
            case MANAGER -> 3;
            case ACCOUNTANT -> 2;
            case RECEPTION -> 1;
        };
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
                .images(room.getRoomImages().stream().map(this::toRoomImageResponse).toList())
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
                .qpayInvoiceId(payment.getQpayInvoiceId())
                .qpaySenderInvoiceNo(payment.getQpaySenderInvoiceNo())
                .qpayQrText(payment.getQpayQrText())
                .qpayQrImage(payment.getQpayQrImage())
                .build();
    }

    public RoomStatusHistoryResponse toRoomStatusHistoryResponse(RoomStatusHistory history) {
        return RoomStatusHistoryResponse.builder()
                .id(history.getId())
                .roomId(history.getRoom() != null ? history.getRoom().getId() : null)
                .status(history.getStatus())
                .startDate(history.getStartDate())
                .endDate(history.getEndDate())
                .note(history.getNote())
                .build();
    }

    public UserSummaryResponse toUserSummaryResponse(User user) {
        if (user == null) {
            return null;
        }
        String globalRole = user.getGlobalRole() != null ? user.getGlobalRole().name() : GlobalRole.USER.name();
        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(globalRole)
                .globalRole(globalRole)
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

    public RoomImageResponse toRoomImageResponse(RoomImage roomImage) {
        return RoomImageResponse.builder()
                .id(roomImage.getId())
                .imageUrl(roomImage.getImageUrl())
                .description(roomImage.getDescription())
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
