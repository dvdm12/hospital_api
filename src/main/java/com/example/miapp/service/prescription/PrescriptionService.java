package com.example.miapp.service.prescription;

import com.example.miapp.dto.prescription.CreatePrescriptionRequest;
import com.example.miapp.dto.prescription.PrescriptionDto;
import com.example.miapp.entity.Prescription.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface Segregation Principle - Clean contract for prescription operations
 * Dependency Inversion Principle - High-level modules depend on abstractions
 */
public interface PrescriptionService {

    // CRUD Operations
    PrescriptionDto createPrescription(CreatePrescriptionRequest request);
    PrescriptionDto updatePrescription(Long prescriptionId, CreatePrescriptionRequest request);
    void cancelPrescription(Long prescriptionId, Long doctorId, String reason);
    void deletePrescription(Long prescriptionId, Long doctorId);

    // Prescription Management
    void processRefill(Long prescriptionId, Long itemId, int refillCount, Long requestingDoctorId);
    PrescriptionDto renewPrescription(Long originalPrescriptionId, Long doctorId);
    void markPrescriptionAsPrinted(Long prescriptionId);
    void completePrescription(Long prescriptionId);
    void updatePrescriptionNotes(Long prescriptionId, String notes);

    // Query Operations
    PrescriptionDto getPrescription(Long prescriptionId);
    Page<PrescriptionDto> findPrescriptionsByPatient(Long patientId, Pageable pageable);
    Page<PrescriptionDto> findPrescriptionsByDoctor(Long doctorId, Pageable pageable);
    Page<PrescriptionDto> findActivePrescriptionsByPatient(Long patientId, Pageable pageable);
    List<PrescriptionDto> getCurrentMedicationsForPatient(Long patientId);
    Page<PrescriptionDto> findPrescriptionsNeedingRenewal(int daysAhead, Pageable pageable);

    // Advanced Search Operations
    Page<PrescriptionDto> searchPrescriptions(Long doctorId, Long patientId, PrescriptionStatus status,
                                            LocalDateTime startDate, LocalDateTime endDate, 
                                            String diagnosisPattern, String medicationName, 
                                            Pageable pageable);

    // Medical Safety Operations
    boolean hasBeenPrescribed(Long patientId, String medicationName);
    boolean hasPotentialDrugInteraction(Long patientId, String newMedicationName);

    // Statistics and Reporting
    List<Object[]> getPrescriptionStatusStats();
    List<Object[]> getPrescriptionStatsByDoctor(LocalDateTime startDate, LocalDateTime endDate);
    List<Object[]> getMostPrescribedMedications(int limit);
}