package com.example.hotelback.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "highlights")
public class Highlight extends BaseEntity{


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "highlight_name", length = 100)
    private String highlightName;

}