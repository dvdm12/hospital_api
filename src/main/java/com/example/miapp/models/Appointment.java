package com.example.miapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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
@ToString
public class Appointment {

    /** Unique identifier for the appointment. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Date and time of the appointment. */
    private LocalDateTime date;

    /** The patient attending the appointment. */
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties("appointments")
    private Patient patient;

    /** The doctor assigned to the appointment. */
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnoreProperties("appointments")
    private Doctor doctor;

    /** Reason for the appointment. */
    private String reason;

    /** Status of the appointment (e.g., Scheduled, Completed, Canceled). */
    private String status;
}
