package com.example.hotelback.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "room_details")
public class RoomDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnore
    private Room room;

    @Column(name = "category", length = 120)
    private String category;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @Column(name = "value", length = 1000)
    private String value;

    @Column(name = "display_order")
    private Integer displayOrder;
}
