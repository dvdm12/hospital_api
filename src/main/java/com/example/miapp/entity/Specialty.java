package com.example.miapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "doctorSpecialties")
@EqualsAndHashCode(exclude = "doctorSpecialties")
public class Specialty {

    /** Unique identifier for the specialty. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the specialty (e.g., Cardiology, Neurology). Must be unique. */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** Description of the specialty. */
    @Column(nullable = false, length = 255)
    private String description;

    /** List of doctors associated with this specialty. */
    @JsonManagedReference
    @OneToMany(mappedBy = "specialty", cascade = CascadeType.ALL)
    private List<DoctorSpecialty> doctorSpecialties;
}
