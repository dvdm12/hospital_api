package com.example.miapp.dto.prescription;

import lombok.Data;

/**
 * DTO for Prescription Item
 */
@Data
public class PrescriptionItemDto {
    private Long id;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
    private Integer quantity;
    private String route;
    private boolean refillable;
    private Integer refillsAllowed;
    private Integer refillsUsed;
}