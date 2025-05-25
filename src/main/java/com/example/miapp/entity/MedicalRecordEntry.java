package com.example.miapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an entry in a patient's medical record.
 */
@Entity
@Table(name = "medical_record_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "medicalRecord")
@EqualsAndHashCode(exclude = "medicalRecord")
public class MedicalRecordEntry {

    /** Unique identifier for the entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** The medical record this entry belongs to. */
    @ManyToOne
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;
    
    /** Date and time when the entry was created. */
    @Column(nullable = false)
    private LocalDateTime entryDate;
    
    /** Type of entry (e.g., CONSULTATION, LAB_RESULT, DIAGNOSIS, TREATMENT). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;
    
    /** Title or summary of the entry. */
    @Column(nullable = false, length = 100)
    private String title;
    
    /** Detailed content of the entry. */
    @Column(nullable = false, length = 2000)
    private String content;
    
    /** Doctor who created the entry. */
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    
    /** Related appointment, if any. */
    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
    
    /** Flag to indicate if this entry is visible to the patient. */
    @Builder.Default
    private boolean visibleToPatient = true;
    
    /** List of attachments (file paths) related to this entry. */
    @Column(length = 1000)
    private String attachments;
    
    /**
     * Enum representing types of medical record entries.
     */
    public enum EntryType {
        CONSULTATION,
        LAB_RESULT,
        IMAGING,
        DIAGNOSIS,
        TREATMENT,
        SURGERY,
        FOLLOW_UP,
        PRESCRIPTION,
        OTHER
    }
    
    /**
     * Pre-persist hook to set entry date if not already set.
     */
    @PrePersist
    public void prePersist() {
        if (this.entryDate == null) {
            this.entryDate = LocalDateTime.now();
        }
    }
}