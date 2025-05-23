package com.example.miapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
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
@EqualsAndHashCode(exclude = {"appointments", "medicalRecord", "patientRooms"})
public class Patient {

    /** Unique identifier for the patient. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Patient's first name. */
    @Column(nullable = false, length = 50)
    private String firstName;

    /** Patient's last name. */
    @Column(nullable = false, length = 50)
    private String lastName;

    /** Patient's birth date. */
    @Past(message = "Birth date must be in the past")
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    /** Patient's phone number. */
    @Column(nullable = false, length = 15)
    private String phone;

    /** Patient's address. */
    @Column(nullable = false, length = 255)
    private String address;

    /** List of appointments associated with the patient. */
    @JsonManagedReference
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    /** Medical record associated with the patient. */
    @JsonManagedReference
    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private MedicalRecord medicalRecord;

    /** List of rooms occupied by the patient. */
    @JsonManagedReference
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<PatientRoom> patientRooms;
}
