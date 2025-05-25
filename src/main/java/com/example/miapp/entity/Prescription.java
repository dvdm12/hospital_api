package com.example.miapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a medical prescription.
 */
@Entity
@Table(name = "prescription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"doctor", "patient", "medicationItems"})
@EqualsAndHashCode(exclude = {"doctor", "patient", "medicationItems"})
public class Prescription {

    /** Unique identifier for the prescription. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Doctor who issued the prescription. */
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull
    private Doctor doctor;
    
    /** Patient for whom the prescription is issued. */
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull
    private Patient patient;
    
    /** Date and time when the prescription was issued. */
    @Column(nullable = false)
    private LocalDateTime issueDate;
    
    /** Prescription diagnosis. */
    @Column(nullable = false, length = 500)
    private String diagnosis;
    
    /** Additional notes about the prescription. */
    @Column(length = 1000)
    private String notes;
    
    /** Status of the prescription. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status;
    
    /** Appointment related to this prescription, if any. */
    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
    
    /** List of medication items in this prescription. */
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItem> medicationItems;
    
    /** Flag to indicate if this prescription has been printed. */
    @Builder.Default
    private boolean printed = false;
    
    /** Date when the prescription was last printed. */
    private LocalDateTime printDate;
    
    /**
     * Enum representing possible prescription statuses.
     */
    public enum PrescriptionStatus {
        ACTIVE,
        COMPLETED,
        CANCELED
    }
}