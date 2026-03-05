package com.example.hotelback.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "hotels")
public class Hotel extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "aimag", length = 100)
    private String aimag;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "description", columnDefinition = "TINYTEXT")
    private String description;
 
}
 


}
 