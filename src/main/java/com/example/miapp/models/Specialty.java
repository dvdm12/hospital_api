package com.example.miapp.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Represents a medical specialty.
 */
@Entity
@Table(name = "specialty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "doctorSpecialties")
public class Specialty {

    /** Unique identifier for the specialty. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the specialty (e.g., Cardiology, Neurology). */
    private String name;

    /** Description of the specialty. */
    private String description;

    /** List of doctors associated with this specialty. */
    @OneToMany(mappedBy = "specialty", cascade = CascadeType.ALL)
    private List<DoctorSpecialty> doctorSpecialties;
}
