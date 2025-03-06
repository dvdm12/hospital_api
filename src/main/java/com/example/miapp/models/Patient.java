package com.example.miapp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * Represents a patient in the hospital system.
 */
@Entity
@Table(name = "patient")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"appointments", "medicalRecord", "patientRooms"})
public class Patient {

    /** Unique identifier for the patient. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Patient's first name. */
    private String firstName;

    /** Patient's last name. */
    private String lastName;

    /** Patient's birth date. */
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    /** Patient's phone number. */
    private String phone;

    /** Patient's address. */
    private String address;

    /** List of appointments associated with the patient. */
    @JsonIgnore
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    /** Medical record associated with the patient. */
    @JsonIgnore
    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private MedicalRecord medicalRecord;

    /** List of rooms occupied by the patient. */
    @JsonIgnore
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<PatientRoom> patientRooms;
}
