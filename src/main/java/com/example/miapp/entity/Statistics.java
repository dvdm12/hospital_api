package com.example.miapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents statistics data for the dashboard.
 */
@Entity
@Table(name = "statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Statistics {

    /** Unique identifier for the statistics entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Date of the statistics entry. */
    @Column(nullable = false)
    private LocalDate date;
    
    /** Type of statistics (daily, weekly, monthly). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatisticsType type;
    
    /** Number of appointments scheduled. */
    private Integer appointmentsScheduled;
    
    /** Number of appointments completed. */
    private Integer appointmentsCompleted;
    
    /** Number of appointments canceled. */
    private Integer appointmentsCanceled;
    
    /** Number of no-shows. */
    private Integer appointmentsNoShow;
    
    /** Number of new patients registered. */
    private Integer newPatients;
    
    /** Number of prescriptions issued. */
    private Integer prescriptionsIssued;
    
    /** Average appointment duration in minutes. */
    private Double avgAppointmentDuration;
    
    /** Most common specialty requested. */
    @ManyToOne
    @JoinColumn(name = "top_specialty_id")
    private Specialty topSpecialty;
    
    /** Doctor with most appointments. */
    @ManyToOne
    @JoinColumn(name = "top_doctor_id")
    private Doctor topDoctor;
    
    /** Total revenue generated. */
    private Double totalRevenue;
    
    /** Date and time when these statistics were generated. */
    @Column(nullable = false)
    private LocalDateTime generatedAt;
    
    /** Date and time when these statistics were last updated. */
    private LocalDateTime updatedAt;
    
    /**
     * Enum representing types of statistics periods.
     */
    public enum StatisticsType {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
    
    /**
     * Pre-persist hook to set generation timestamp.
     */
    @PrePersist
    public void prePersist() {
        this.generatedAt = LocalDateTime.now();
    }
    
    /**
     * Pre-update hook to set update timestamp.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}