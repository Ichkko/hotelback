package com.example.hotelback.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "hotels")
public class Hotel extends BaseEntity {

    @NotBlank(message = "Зочид буудлын нэр хоосон байж болохгүй")
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

    @Column(name = "starting_price")
    private Double startingPrice;

    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;

    @ManyToMany
    @JoinTable(
            name = "hotel_owners",
            joinColumns = @jakarta.persistence.JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "user_id")
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<User> owners = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "hotel_receptionists",
            joinColumns = @jakarta.persistence.JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "user_id")
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<User> receptionists = new ArrayList<>();

    @Transient
    public Long getOwnerId() {
        return owners.isEmpty() ? null : owners.get(0).getId();
    }

    @Transient
    public List<Long> getOwnerIds() {
        return owners.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    @Transient
    public List<Long> getReceptionistIds() {
        return receptionists.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    @OneToMany(mappedBy = "hotel")
    private List<Room> rooms = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "hotel")
    private List<Amenity> amenities = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "hotel")
    private List<Highlight> highlights = new ArrayList<>();
}
