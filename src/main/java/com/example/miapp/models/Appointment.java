package com.example.miapp.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;

import java.time.LocalDateTime;

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
@ToString
public class Appointment {

    /** Unique identifier for the appointment. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Date and time of the appointment. Must be in the present or future. */
    @FutureOrPresent(message = "Appointment date must be in the present or future")
    private LocalDateTime date;

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

    /** Status of the appointment (e.g., Scheduled, Completed, Canceled). */
    @Column(nullable = false, length = 20)
    private String status;
}
