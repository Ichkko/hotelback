package com.example.hotelback.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    @JsonIgnore
    private Hotel hotel;

    @Transient
    public Long getHotelId() {
        return hotel != null ? hotel.getId() : null;
    }

    @Column(name = "room_type", length = 100)
    private String roomType;

    @Column(name = "price")
    private Double price;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "wing", length = 100)
    private String wing;

    @Column(name = "section", length = 100)
    private String section;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Transient
    private String roomDetails;

    @Convert(converter = RoomStatusConverter.class)
    @Column(name = "status")
    private RoomStatus status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImage> roomImages = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomDetail> details = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "room")
    private List<Booking> bookings = new ArrayList<>();
}
