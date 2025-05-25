package com.example.miapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@ToString(exclude = {"appointments", "medicalRecord", "prescriptions"})
@EqualsAndHashCode(exclude = {"appointments", "medicalRecord", "prescriptions"})
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
    
    /** Patient's gender. */
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    /** Patient's blood type. */
    @Column(length = 5)
    private String bloodType;
    
    /** Patient's emergency contact name. */
    @Column(length = 100)
    private String emergencyContactName;
    
    /** Patient's emergency contact phone. */
    @Column(length = 15)
    private String emergencyContactPhone;
    
    /** Patient's insurance provider. */
    @Column(length = 100)
    private String insuranceProvider;
    
    /** Patient's insurance policy number. */
    @Column(length = 50)
    private String insurancePolicyNumber;
    
    /** User account associated with this patient. */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    /** List of appointments associated with the patient. */
    @JsonManagedReference
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    /** Medical record associated with the patient. */
    @JsonManagedReference
    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private MedicalRecord medicalRecord;
    
    /** List of prescriptions for the patient. */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> prescriptions;
    
    /**
     * Gets the full name of the patient.
     * 
     * @return The full name (first name + last name)
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Calculates the age of the patient based on the birth date.
     * 
     * @return The age in years
     */
    @Transient
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        
        Date today = new Date();
        long diffInMillies = today.getTime() - birthDate.getTime();
        return (int) (diffInMillies / (1000L * 60 * 60 * 24 * 365));
    }
    
    /**
     * Enum representing patient gender.
     */
    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }
}