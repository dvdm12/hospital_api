package com.example.miapp.service.prescription;

import com.example.miapp.dto.prescription.PrescriptionDto;
import com.example.miapp.entity.Prescription;
import com.example.miapp.entity.PrescriptionItem;
import com.example.miapp.entity.Prescription.PrescriptionStatus;
import com.example.miapp.mapper.PrescriptionMapper;
import com.example.miapp.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for prescription queries (Single Responsibility)
 * Implements Repository Pattern for data access abstraction
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PrescriptionQueryService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMapper prescriptionMapper;

    /**
     * Finds prescription by ID
     */
    public PrescriptionDto getPrescription(Long prescriptionId) {
        Prescription prescription = findPrescriptionById(prescriptionId);
        return prescriptionMapper.toDto(prescription);
    }

    /**
     * Finds prescription entity by ID (internal use)
     */
    public Prescription findPrescriptionById(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found with ID: " + prescriptionId));
    }

    /**
     * Finds prescriptions for a specific patient
     */
    public Page<PrescriptionDto> findPrescriptionsByPatient(Long patientId, Pageable pageable) {
        log.info("Finding prescriptions for patient: {}", patientId);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId, pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds prescriptions by a specific doctor
     */
    public Page<PrescriptionDto> findPrescriptionsByDoctor(Long doctorId, Pageable pageable) {
        log.info("Finding prescriptions by doctor: {}", doctorId);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByDoctorId(doctorId, pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds active prescriptions for a patient
     */
    public Page<PrescriptionDto> findActivePrescriptionsByPatient(Long patientId, Pageable pageable) {
        log.info("Finding active prescriptions for patient: {}", patientId);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByPatientIdAndStatus(
                patientId, PrescriptionStatus.ACTIVE, pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds prescriptions by status
     */
    public Page<PrescriptionDto> findPrescriptionsByStatus(PrescriptionStatus status, Pageable pageable) {
        log.info("Finding prescriptions by status: {}", status);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByStatus(status, pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds prescriptions issued within date range
     */
    public Page<PrescriptionDto> findPrescriptionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Finding prescriptions between {} and {}", startDate, endDate);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByIssueDateBetween(startDate, endDate, pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds prescriptions by medication name
     */
    public Page<PrescriptionDto> findPrescriptionsByMedication(String medicationName, Pageable pageable) {
        log.info("Finding prescriptions by medication: {}", medicationName);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByMedicationName(medicationName, pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds prescriptions by diagnosis
     */
    public Page<PrescriptionDto> findPrescriptionsByDiagnosis(String diagnosisPattern, Pageable pageable) {
        log.info("Finding prescriptions by diagnosis pattern: {}", diagnosisPattern);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByDiagnosisContaining(diagnosisPattern, pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds prescriptions that need renewal (expiring soon)
     */
    public Page<PrescriptionDto> findPrescriptionsNeedingRenewal(int daysAhead, Pageable pageable) {
        log.info("Finding prescriptions needing renewal within {} days", daysAhead);
        
        LocalDateTime cutoffDate = LocalDateTime.now().plusDays(daysAhead);
        Page<Prescription> prescriptions = prescriptionRepository.findPrescriptionsNeedingRenewal(cutoffDate, pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds unprinted prescriptions
     */
    public Page<PrescriptionDto> findUnprintedPrescriptions(Pageable pageable) {
        log.info("Finding unprinted prescriptions");
        
        Page<Prescription> prescriptions = prescriptionRepository.findByPrintedFalse(pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds prescriptions with refillable items
     */
    public Page<PrescriptionDto> findPrescriptionsWithRefillableItems(Pageable pageable) {
        log.info("Finding prescriptions with refillable items");
        
        Page<Prescription> prescriptions = prescriptionRepository.findPrescriptionsWithRefillableItems(pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds recent prescriptions for a patient
     */
    public List<PrescriptionDto> getRecentPrescriptionsForPatient(Long patientId, int limit) {
        log.info("Getting {} recent prescriptions for patient: {}", limit, patientId);
        
        Page<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByIssueDateDesc(
                patientId, Pageable.ofSize(limit));
        return prescriptions.map(prescriptionMapper::toDto).getContent();
    }

    /**
     * Finds patient's current medications (active prescriptions)
     */
    public List<PrescriptionDto> getCurrentMedicationsForPatient(Long patientId) {
        log.info("Getting current medications for patient: {}", patientId);
        
        Page<Prescription> activePrescriptions = prescriptionRepository.findByPatientIdAndStatus(
                patientId, PrescriptionStatus.ACTIVE, Pageable.unpaged());
        
        return activePrescriptions.map(prescriptionMapper::toDto).getContent();
    }

    /**
     * Advanced search for prescriptions
     */
    public Page<PrescriptionDto> searchPrescriptions(Long doctorId, Long patientId, PrescriptionStatus status,
                                                   LocalDateTime startDate, LocalDateTime endDate, 
                                                   String diagnosisPattern, String medicationName, 
                                                   Pageable pageable) {
        log.info("Advanced search for prescriptions with multiple criteria");
        
        Page<Prescription> prescriptions = prescriptionRepository.searchPrescriptions(
                doctorId, patientId, status, startDate, endDate, diagnosisPattern, medicationName, pageable);
        
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Gets prescription statistics by status
     */
    public List<Object[]> getPrescriptionStatusStats() {
        return prescriptionRepository.countPrescriptionsByStatus();
    }

    /**
     * Gets prescription statistics by doctor
     */
    public List<Object[]> getPrescriptionStatsByDoctor(LocalDateTime startDate, LocalDateTime endDate) {
        return prescriptionRepository.countPrescriptionsByDoctor(startDate, endDate);
    }

    /**
     * Gets most prescribed medications
     */
    public List<Object[]> getMostPrescribedMedications(int limit) {
        return prescriptionRepository.findMostPrescribedMedications(Pageable.ofSize(limit));
    }

    /**
     * Finds prescriptions by appointment
     */
    public Optional<PrescriptionDto> findPrescriptionByAppointment(Long appointmentId) {
        log.info("Finding prescription for appointment: {}", appointmentId);
        
        Optional<Prescription> prescription = prescriptionRepository.findByAppointmentId(appointmentId);
        return prescription.map(prescriptionMapper::toDto);
    }

    /**
     * Checks if patient has been prescribed specific medication
     */
    public boolean hasBeenPrescribed(Long patientId, String medicationName) {
        Page<Prescription> prescriptions = prescriptionRepository.findByPatientIdAndMedicationName(
                patientId, medicationName, Pageable.ofSize(1));
        
        return !prescriptions.isEmpty();
    }

    /**
     * Finds prescriptions with specific medication for drug interaction checking
     */
    public List<PrescriptionDto> findActivePrescriptionsWithMedication(Long patientId, String medicationName) {
        log.info("Finding active prescriptions with medication {} for patient {}", medicationName, patientId);
        
        Page<Prescription> prescriptions = prescriptionRepository.findActivePrescriptionsWithMedication(
                patientId, medicationName, Pageable.unpaged());
        
        return prescriptions.map(prescriptionMapper::toDto).getContent();
    }

    /**
     * Gets all prescriptions as DTOs
     */
    public Page<PrescriptionDto> getAllPrescriptions(Pageable pageable) {
        Page<Prescription> prescriptions = prescriptionRepository.findAll(pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    /**
     * Finds prescriptions due for refill
     */
    public List<PrescriptionDto> findPrescriptionsDueForRefill(Long patientId) {
        log.info("Finding prescriptions due for refill for patient: {}", patientId);
        
        // Find active prescriptions with refillable items that might be running low
        Page<Prescription> activePrescriptions = prescriptionRepository.findByPatientIdAndStatus(
                patientId, PrescriptionStatus.ACTIVE, Pageable.unpaged());
        
        return activePrescriptions.stream()
                .filter(prescription -> prescription.getMedicationItems().stream()
                        .anyMatch(item -> item.isRefillable() && 
                                        item.getRefillsUsed() < item.getRefillsAllowed()))
                .map(prescriptionMapper::toDto)
                .toList();
    }

    /**
     * Gets prescription items needing refill approval
     */
    public List<PrescriptionItem> findItemsNeedingRefillApproval(Long doctorId) {
        log.info("Finding prescription items needing refill approval for doctor: {}", doctorId);
        
        // This would typically involve a separate refill request entity
        // For now, we'll find items with remaining refills
        Page<Prescription> doctorPrescriptions = prescriptionRepository.findByDoctorIdAndStatus(
                doctorId, PrescriptionStatus.ACTIVE, Pageable.unpaged());
        
        return doctorPrescriptions.stream()
                .flatMap(prescription -> prescription.getMedicationItems().stream())
                .filter(item -> item.isRefillable() && 
                              item.getRefillsUsed() < item.getRefillsAllowed())
                .toList();
    }

    /**
     * Checks for potential drug interactions for patient
     */
    public boolean hasPotentialDrugInteraction(Long patientId, String newMedicationName) {
        List<PrescriptionDto> currentMedications = getCurrentMedicationsForPatient(patientId);
        
        // Simplified interaction checking - in real implementation, use drug interaction database
        return currentMedications.stream()
                .flatMap(prescription -> prescription.getMedicationItems().stream())
                .anyMatch(item -> checkBasicInteraction(item.getMedicationName(), newMedicationName));
    }

    // Private helper methods

    private boolean checkBasicInteraction(String existingMedication, String newMedication) {
        // Very simplified interaction checking
        String existing = existingMedication.toLowerCase();
        String newMed = newMedication.toLowerCase();
        
        // Blood thinner + NSAID interaction
        if ((existing.contains("warfarin") || existing.contains("heparin")) && 
            (newMed.contains("ibuprofen") || newMed.contains("aspirin"))) {
            return true;
        }
        
        // More comprehensive checking would be implemented here
        return false;
    }
}