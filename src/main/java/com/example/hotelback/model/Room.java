package com.example.hotelback.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room extends BaseEntity{


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Size(max = 100)
    @Column(name = "room_type", length = 100)
    private String roomType;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "capacity")
    private Integer capacity;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

}