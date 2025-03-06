package com.example.miapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

/**
 * Represents the relation between a patient and a room.
 */
@Entity
@Table(name = "patient_room")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(exclude = {"patient", "room"})
public class PatientRoom {

    /** Unique identifier for the patient-room relation. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The patient assigned to the room. */
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"patientRooms"})
    private Patient patient;

    /** The room assigned to the patient. */
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({"patientRooms"})
    private Room room;

    /** Check-in date of the patient in the room. */
    @PastOrPresent(message = "Check-in date must be in the past or present")
    @Temporal(TemporalType.DATE)
    private Date checkInDate;

    /** Check-out date of the patient from the room. */
    @FutureOrPresent(message = "Check-out date must be in the present or future")
    @Temporal(TemporalType.DATE)
    private Date checkOutDate;

    /** Additional observations about the patient stay. */
    @Size(max = 255, message = "Observations must not exceed 255 characters")
    private String observations;
}
