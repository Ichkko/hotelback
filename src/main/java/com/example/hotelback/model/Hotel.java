package com.example.hotelback.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "hotels")
public class Hotel extends BaseEntity {


    @Column(name = "name", nullable = false, length = 150)
    private String name;


    @Column(name = "address")
    private String address;


    @Column(name = "aimag", length = 100)
    private String aimag;


    @Column(name = "phone", length = 20)
    private String phone;

    @Lob
    @Column(name = "description")
    private String description;


}