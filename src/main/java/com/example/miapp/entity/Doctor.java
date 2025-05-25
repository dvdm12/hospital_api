package com.example.miapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a doctor in the hospital system.
 */
@Entity
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"appointments", "doctorSpecialties", "workSchedules", "prescriptions"})
@EqualsAndHashCode(exclude = {"appointments", "doctorSpecialties", "workSchedules", "prescriptions"})
public class Doctor {

    /** Unique identifier for the doctor. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Doctor's first name. */
    @Column(nullable = false, length = 50)
    private String firstName;

    /** Doctor's last name. */
    @Column(nullable = false, length = 50)
    private String lastName;

    /** Doctor's phone number. */
    @Column(nullable = false, length = 15)
    private String phone;

    /** Doctor's email address. */
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    /** Doctor's license number. */
    @Column(nullable = false, unique = true, length = 20)
    private String licenseNumber;
    
    /** Doctor's profile picture URL. */
    private String profilePicture;
    
    /** Doctor's biography/description. */
    @Column(length = 1000)
    private String biography;
    
    /** Doctor's consultation fee. */
    private Double consultationFee;
    
    /** User account associated with this doctor. */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    /** List of appointments assigned to the doctor. */
    @JsonManagedReference
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    /** List of specialties for the doctor. */
    @JsonManagedReference
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DoctorSpecialty> doctorSpecialties;
    
    /** List of prescriptions created by the doctor. */
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> prescriptions;
    
    /** Doctor's work schedules. */
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<DoctorSchedule> workSchedules = new HashSet<>();
    
    /**
     * Gets the full name of the doctor.
     * 
     * @return The full name (first name + last name)
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Checks if the doctor is available at a specific time and day.
     * 
     * @param dayOfWeek The day to check
     * @param time The time to check
     * @return true if available, false otherwise
     */
    public boolean isAvailable(DayOfWeek dayOfWeek, LocalTime time) {
        return workSchedules.stream()
            .filter(schedule -> schedule.getDayOfWeek() == dayOfWeek)
            .anyMatch(schedule -> 
                !time.isBefore(schedule.getStartTime()) && 
                !time.isAfter(schedule.getEndTime()));
    }
}