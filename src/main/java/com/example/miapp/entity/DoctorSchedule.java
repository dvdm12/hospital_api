package com.example.miapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a doctor's working schedule.
 */
@Entity
@Table(name = "doctor_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "doctor")
@EqualsAndHashCode(exclude = "doctor")
public class DoctorSchedule {

    /** Unique identifier for the schedule. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** The doctor associated with this schedule. */
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    
    /** Day of week for this schedule. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;
    
    /** Start time of doctor's working hours. */
    @Column(nullable = false)
    private LocalTime startTime;
    
    /** End time of doctor's working hours. */
    @Column(nullable = false)
    private LocalTime endTime;
    
    /** Duration of each appointment slot in minutes. */
    @Column(nullable = false)
    private Integer slotDurationMinutes;
    
    /** Flag to indicate if this schedule is active. */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
    
    /** Location or consultation room. */
    @Column(length = 50)
    private String location;
    
    /**
     * Checks if a given time falls within this schedule.
     * 
     * @param time The time to check
     * @return true if the time is within schedule, false otherwise
     */
    public boolean isTimeWithinSchedule(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
}