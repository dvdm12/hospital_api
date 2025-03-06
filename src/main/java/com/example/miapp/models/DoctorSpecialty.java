package com.example.miapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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
@Builder(toBuilder=true)
@ToString
@EqualsAndHashCode(exclude = {"doctor", "specialty"})
public class DoctorSpecialty {

    /** Unique identifier for the doctor-specialty relation. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The doctor assigned to the specialty. */
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnoreProperties({"doctorSpecialties"})
    private Doctor doctor;

    /** The specialty assigned to the doctor. */
    @ManyToOne
    @JoinColumn(name = "specialty_id", nullable = false)
    @JsonIgnoreProperties({"doctorSpecialties"})
    private Specialty specialty;

    /** Certification date of the doctor in the specialty. */
    @PastOrPresent(message = "Certification date must be in the past or present")
    @Temporal(TemporalType.DATE)
    private Date certificationDate;

    /** Experience level of the doctor in the specialty. */
    @Size(min = 3, max = 50, message = "Experience level must be between 3 and 50 characters")
    private String experienceLevel;
}
