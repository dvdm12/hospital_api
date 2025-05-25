package com.example.miapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a scheduled appointment between a patient and a doctor.
 */
@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"patient", "doctor", "prescriptions"})
@EqualsAndHashCode(exclude = {"patient", "doctor", "prescriptions"})
public class Appointment {

    /** Unique identifier for the appointment. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Date and time of the appointment. Must be in the present or future. */
    @FutureOrPresent(message = "Appointment date must be in the present or future")
    private LocalDateTime date;
    
    /** End time of the appointment. */
    private LocalDateTime endTime;

    /** The patient attending the appointment. */
    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /** The doctor assigned to the appointment. */
    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /** Reason for the appointment. */
    @Column(nullable = false, length = 255)
    private String reason;

    /** Status of the appointment. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;
    
    /** Notes added during or after the appointment. */
    @Column(length = 1000)
    private String notes;
    
    /** Flag to indicate if the appointment was confirmed by the patient. */
    @Builder.Default
    private boolean confirmed = false;
    
    /** Date and time when the patient confirmed the appointment. */
    private LocalDateTime confirmationDate;
    
    /** Date and time when the appointment was created. */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /** User who created the appointment. */
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    /** Date and time when the appointment was last updated. */
    private LocalDateTime updatedAt;
    
    /** User who last updated the appointment. */
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    /** Prescriptions associated with this appointment. */
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> prescriptions;
    
    /** Appointment location or room. */
    @Column(length = 50)
    private String location;
    
    /**
     * Gets the duration of the appointment in minutes.
     * 
     * @return The duration in minutes
     */
    @Transient
    public long getDurationMinutes() {
        if (endTime == null) {
            return 0;
        }
        return Duration.between(date, endTime).toMinutes();
    }
    
    /**
     * Checks if the appointment is overdue.
     * 
     * @return true if the appointment is overdue, false otherwise
     */
    @Transient
    public boolean isOverdue() {
        return status == AppointmentStatus.SCHEDULED && date.isBefore(LocalDateTime.now());
    }
    
    /**
     * Enum representing possible appointment statuses.
     */
    public enum AppointmentStatus {
        SCHEDULED,
        CONFIRMED,
        COMPLETED,
        CANCELED,
        RESCHEDULED,
        NO_SHOW
    }
    
    /**
     * Pre-persist hook to set creation timestamp.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Pre-update hook to set update timestamp.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}