package com.example.miapp.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Represents a patient's medical history.
 */
@Entity
@Table(name = "medical_record")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "patient")
@EqualsAndHashCode(exclude = "patient")
public class MedicalRecord {

    /** Unique identifier for the medical record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Diagnosis information. */
    private String diagnosis;

    /** Treatment details. */
    private String treatment;

    /** Record entry date. */
    @Temporal(TemporalType.DATE)
    private Date entryDate;

    /** The patient associated with this medical record. */
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /** The doctor responsible for this medical record. */
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor responsibleDoctor;
}
