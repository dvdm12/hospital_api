package com.example.miapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Represents a medication item in a prescription.
 */
@Entity
@Table(name = "prescription_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "prescription")
@EqualsAndHashCode(exclude = "prescription")
public class PrescriptionItem {

    /** Unique identifier for the prescription item. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** The prescription this item belongs to. */
    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false)
    @NotNull
    private Prescription prescription;
    
    /** Name of the medication. */
    @Column(nullable = false, length = 100)
    private String medicationName;
    
    /** Dosage information. */
    @Column(nullable = false, length = 100)
    private String dosage;
    
    /** Frequency of administration. */
    @Column(nullable = false, length = 100)
    private String frequency;
    
    /** Duration of treatment. */
    @Column(length = 100)
    private String duration;
    
    /** Special instructions for taking this medication. */
    @Column(length = 500)
    private String instructions;
    
    /** Quantity prescribed. */
    @Positive
    private Integer quantity;
    
    /** Route of administration (oral, topical, etc.). */
    @Column(length = 50)
    private String route;
    
    /** Flag to indicate if this is a refillable prescription. */
    @Builder.Default
    private boolean refillable = false;
    
    /** Number of refills allowed. */
    private Integer refillsAllowed;
    
    /** Number of refills used. */
    @Builder.Default
    private Integer refillsUsed = 0;
}