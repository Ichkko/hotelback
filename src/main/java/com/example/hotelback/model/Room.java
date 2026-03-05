package com.example.hotelback.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

 
import java.math.BigDecimal;
import java.util.ArrayList;
 
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room extends BaseEntity {

 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "room_type", length = 100)
 
    @Column(name = "room_type")
 
    private String roomType;

    @Column(name = "price")
    private Double price;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImage> roomImages = new ArrayList<>();
}
