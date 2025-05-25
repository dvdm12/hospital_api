package com.example.miapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a patient's medical history.
 */
@Entity
@Table(name = "medical_record")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"patient", "entries"})
@EqualsAndHashCode(exclude = {"patient", "entries"})
public class MedicalRecord {

    /** Unique identifier for the medical record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Creation date of the medical record. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Last updated date of the medical record. */
    private LocalDateTime updatedAt;

    /** The patient associated with this medical record. */
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    /** List of medical record entries. */
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicalRecordEntry> entries = new ArrayList<>();
    
    /** Known allergies of the patient. */
    @Column(length = 500)
    private String allergies;
    
    /** Family medical history. */
    @Column(length = 1000)
    private String familyHistory;
    
    /** Chronic conditions of the patient. */
    @Column(length = 500)
    private String chronicConditions;
    
    /** Current medications being taken by the patient. */
    @Column(length = 500)
    private String currentMedications;
    
    /** Previous surgeries. */
    @Column(length = 500)
    private String surgicalHistory;
    
    /** Additional notes about the patient's medical history. */
    @Column(length = 1000)
    private String notes;
    
    /**
     * Pre-persist hook to set creation timestamp.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Pre-update hook to set update timestamp.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Adds a new entry to the medical record.
     * 
     * @param entry The entry to add
     */
    public void addEntry(MedicalRecordEntry entry) {
        entries.add(entry);
        entry.setMedicalRecord(this);
    }
    
    /**
     * Removes an entry from the medical record.
     * 
     * @param entry The entry to remove
     */
    public void removeEntry(MedicalRecordEntry entry) {
        entries.remove(entry);
        entry.setMedicalRecord(null);
    }
}