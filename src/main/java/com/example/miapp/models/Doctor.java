package com.example.miapp.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Builder(toBuilder = true)
@ToString(exclude = {"appointments", "doctorSpecialties"})
@EqualsAndHashCode(exclude = {"appointments", "doctorSpecialties"})
public class Doctor {

    /** Unique identifier for the doctor. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Doctor's first name. */
    @Column(nullable = false, length = 50)
    private String firstName;

    /** Doctor's last name. */
    @Column(nullable = false, length = 50)
    private String lastName;

    /** Doctor's phone number. */
    @Column(nullable = false, length = 15)
    private String phone;

    /** Doctor's email address. */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** List of appointments assigned to the doctor. */
    @JsonManagedReference
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    /** List of specialties for the doctor. */
    @JsonManagedReference
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DoctorSpecialty> doctorSpecialties;
}
