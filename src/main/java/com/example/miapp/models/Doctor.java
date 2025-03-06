package com.example.miapp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Represents a doctor in the hospital system.
 */
@Entity
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"appointments", "doctorSpecialties"})
public class Doctor {

    /** Unique identifier for the doctor. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Doctor's first name. */
    private String firstName;

    /** Doctor's last name. */
    private String lastName;

    /** Doctor's phone number. */
    private String phone;

    /** Doctor's email address. */
    private String email;

    /** List of appointments assigned to the doctor. */
    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    /** List of specialties for the doctor. */
    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DoctorSpecialty> doctorSpecialties;
}
