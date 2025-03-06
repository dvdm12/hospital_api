package com.example.miapp.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Represents the relation between a doctor and a specialty.
 */
@Entity
@Table(name = "doctor_specialty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DoctorSpecialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    private Date certificationDate;
    private String experienceLevel;
}
