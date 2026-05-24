package com.example.hotelback.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @NotBlank(message = "Нэр хоосон байж болохгүй")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Email(message = "Email буруу форматтай")
    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "role", length = 20)
    private String role;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Booking> bookings = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "owners")
    private List<Hotel> hotels = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "receptionists")
    private List<Hotel> receptionHotels = new ArrayList<>();
}
