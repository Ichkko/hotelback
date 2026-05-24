package com.example.hotelback.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "global_role", nullable = false, length = 20)
    private GlobalRole globalRole = GlobalRole.USER;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Booking> bookings = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<HotelUserRole> hotelRoles = new ArrayList<>();

    /** @deprecated Use {@link #getGlobalRole()} instead. Kept for backward compatibility. */
    @Deprecated
    public String getRole() {
        return globalRole != null ? globalRole.name() : GlobalRole.USER.name();
    }

    /** @deprecated Use {@link #setGlobalRole(GlobalRole)} instead. Kept for backward compatibility. */
    @Deprecated
    public void setRole(String role) {
        if (role == null || role.isBlank()) {
            this.globalRole = GlobalRole.USER;
            return;
        }
        this.globalRole = "ADMIN".equalsIgnoreCase(role.trim()) ? GlobalRole.ADMIN : GlobalRole.USER;
    }
}
