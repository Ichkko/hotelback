package com.example.hotelback.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hotels")
public class Hotel extends BaseEntity {

    @Size(max = 150)
    @NotNull
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @Size(max = 100)
    @Column(name = "aimag", length = 100)
    private String aimag;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Lob
    @Column(name = "description")
    private String description;
}