package com.example.miapp.service.prescription;

import com.example.miapp.dto.prescription.CreatePrescriptionRequest;
import com.example.miapp.dto.prescription.CreatePrescriptionItemRequest;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Prescription;
import com.example.miapp.entity.PrescriptionItem;
import com.example.miapp.exception.PrescriptionValidationException;
import com.example.miapp.service.medicalrecord.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Service responsible for prescription validations (Single Responsibility)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionValidationService {

    private final MedicalRecordService medicalRecordService;

    // Known dangerous drug interactions (simplified - in real world, use drug interaction database)
    private static final Set<String> CONTROLLED_SUBSTANCES = Set.of(
        "morphine", "oxycodone", "fentanyl", "tramadol", "codeine", 
        "alprazolam", "lorazepam", "diazepam", "zolpidem"
    );

    private static final Set<String> BLOOD_THINNERS = Set.of(
        "warfarin", "heparin", "rivaroxaban", "apixaban", "dabigatran"
    );

    private static final Set<String> NSAIDS = Set.of(
        "ibuprofen", "aspirin", "naproxen", "diclofenac", "celecoxib"
    );

    /**
     * Validates prescription creation request
     */
    public void validatePrescriptionCreation(CreatePrescriptionRequest request, Doctor doctor, Patient patient) {
        validateBasicPrescriptionData(request);
        validateDoctorCanPrescribe(doctor);
        validatePatientEligibility(patient);
        validateMedicationItems(request.getMedicationItems());
        validateDrugInteractions(request.getMedicationItems(), patient);
        validatePatientAllergies(request.getMedicationItems(), patient);
    }

    /**
     * Validates prescription update
     */
    public void validatePrescriptionUpdate(CreatePrescriptionRequest request, Prescription existingPrescription, Doctor doctor) {
        validateBasicPrescriptionData(request);
        validateDoctorCanModifyPrescription(doctor, existingPrescription);
        validatePrescriptionCanBeModified(existingPrescription);
        validateMedicationItems(request.getMedicationItems());
        validateDrugInteractions(request.getMedicationItems(), existingPrescription.getPatient());
        validatePatientAllergies(request.getMedicationItems(), existingPrescription.getPatient());
    }

    /**
     * Validates if prescription can be canceled
     */
    public void validatePrescriptionCancellation(Prescription prescription, Doctor doctor) {
        if (prescription.getStatus() == Prescription.PrescriptionStatus.COMPLETED) {
            throw new PrescriptionValidationException("Cannot cancel a completed prescription");
        }

        validateDoctorCanModifyPrescription(doctor, prescription);

        // Check if prescription has been printed (pharmacy constraint)
        if (prescription.isPrinted()) {
            log.warn("Canceling printed prescription {} - pharmacy notification may be required", prescription.getId());
        }
    }

    /**
     * Validates refill request
     */
    public void validateRefillRequest(PrescriptionItem item, int requestedRefills) {
        if (!item.isRefillable()) {
            throw new PrescriptionValidationException("Medication is not refillable: " + item.getMedicationName());
        }

        int remainingRefills = item.getRefillsAllowed() - item.getRefillsUsed();
        if (requestedRefills > remainingRefills) {
            throw new PrescriptionValidationException(
                String.format("Requested refills (%d) exceed remaining refills (%d) for %s", 
                    requestedRefills, remainingRefills, item.getMedicationName()));
        }

        // Check if controlled substance refill restrictions apply
        if (isControlledSubstance(item.getMedicationName()) && requestedRefills > 1) {
            throw new PrescriptionValidationException(
                "Controlled substances can only be refilled one at a time: " + item.getMedicationName());
        }
    }

    /**
     * Validates prescription item creation
     */
    public void validatePrescriptionItem(CreatePrescriptionItemRequest item) {
        if (item.getMedicationName() == null || item.getMedicationName().trim().isEmpty()) {
            throw new PrescriptionValidationException("Medication name is required");
        }

        if (item.getDosage() == null || item.getDosage().trim().isEmpty()) {
            throw new PrescriptionValidationException("Dosage is required");
        }

        if (item.getFrequency() == null || item.getFrequency().trim().isEmpty()) {
            throw new PrescriptionValidationException("Frequency is required");
        }

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new PrescriptionValidationException("Quantity must be positive");
        }

        // Validate dosage format
        validateDosageFormat(item.getDosage(), item.getMedicationName());

        // Validate frequency format
        validateFrequencyFormat(item.getFrequency());

        // Validate controlled substance restrictions
        validateControlledSubstance(item);
    }

    // Private validation methods

    private void validateBasicPrescriptionData(CreatePrescriptionRequest request) {
        if (request.getDoctorId() == null) {
            throw new PrescriptionValidationException("Doctor ID is required");
        }

        if (request.getPatientId() == null) {
            throw new PrescriptionValidationException("Patient ID is required");
        }

        if (request.getDiagnosis() == null || request.getDiagnosis().trim().isEmpty()) {
            throw new PrescriptionValidationException("Diagnosis is required for prescription");
        }

        if (request.getMedicationItems() == null || request.getMedicationItems().isEmpty()) {
            throw new PrescriptionValidationException("At least one medication item is required");
        }

        // Validate diagnosis format (basic check)
        if (request.getDiagnosis().length() < 10) {
            throw new PrescriptionValidationException("Diagnosis should be more detailed (minimum 10 characters)");
        }
    }

    private void validateDoctorCanPrescribe(Doctor doctor) {
        if (doctor.getUser() == null || doctor.getUser().getStatus() != com.example.miapp.entity.User.UserStatus.ACTIVE) {
            throw new PrescriptionValidationException("Only active doctors can create prescriptions");
        }

        if (doctor.getLicenseNumber() == null || doctor.getLicenseNumber().trim().isEmpty()) {
            throw new PrescriptionValidationException("Doctor must have a valid license number to prescribe");
        }

        // Additional checks could include DEA number for controlled substances
    }

    private void validatePatientEligibility(Patient patient) {
        if (patient.getUser() == null || patient.getUser().getStatus() != com.example.miapp.entity.User.UserStatus.ACTIVE) {
            throw new PrescriptionValidationException("Cannot prescribe to inactive patients");
        }

        // Check patient age for certain medications
        int patientAge = patient.getAge();
        if (patientAge < 0) {
            throw new PrescriptionValidationException("Invalid patient age");
        }
    }

    private void validateMedicationItems(List<CreatePrescriptionItemRequest> items) {
        if (items.size() > 10) {
            throw new PrescriptionValidationException("Maximum 10 medications allowed per prescription");
        }

        for (CreatePrescriptionItemRequest item : items) {
            validatePrescriptionItem(item);
        }

        // Check for duplicate medications
        long distinctMedications = items.stream()
                .map(item -> item.getMedicationName().toLowerCase().trim())
                .distinct()
                .count();

        if (distinctMedications < items.size()) {
            throw new PrescriptionValidationException("Duplicate medications found in prescription");
        }
    }

    private void validateDrugInteractions(List<CreatePrescriptionItemRequest> items, Patient patient) {
        // Check current medications for interactions
        if (medicalRecordService.findMedicalRecordByPatientId(patient.getId()).isPresent()) {
            // This would typically integrate with a drug interaction database
            checkBasicDrugInteractions(items);
        }
    }

    private void validatePatientAllergies(List<CreatePrescriptionItemRequest> items, Patient patient) {
        for (CreatePrescriptionItemRequest item : items) {
            String medicationName = item.getMedicationName().toLowerCase();
            
            if (medicalRecordService.hasAllergy(patient.getId(), medicationName)) {
                throw new PrescriptionValidationException(
                    "Patient is allergic to " + item.getMedicationName());
            }

            // Check for drug class allergies (simplified)
            checkDrugClassAllergies(medicationName, patient);
        }
    }

    public void validateDoctorCanModifyPrescription(Doctor doctor, Prescription prescription) {
        if (!prescription.getDoctor().getId().equals(doctor.getId())) {
            // Check if doctor has admin privileges
            boolean canModify = doctor.getUser().getRoles().stream()
                .anyMatch(role -> role.getName().name().contains("ADMIN"));
            
            if (!canModify) {
                throw new PrescriptionValidationException(
                    "Only the prescribing doctor or authorized personnel can modify this prescription");
            }
        }
    }

    private void validatePrescriptionCanBeModified(Prescription prescription) {
        if (prescription.getStatus() == Prescription.PrescriptionStatus.COMPLETED) {
            throw new PrescriptionValidationException("Cannot modify completed prescription");
        }

        // Check if prescription is too old to modify
        if (prescription.getIssueDate().isBefore(LocalDateTime.now().minusDays(30))) {
            log.warn("Modifying prescription older than 30 days: {}", prescription.getId());
        }
    }

    private void validateDosageFormat(String dosage, String medicationName) {
        dosage = dosage.toLowerCase().trim();
        
        // Basic dosage format validation
        if (!dosage.matches(".*\\d+.*")) {
            throw new PrescriptionValidationException("Dosage must include numeric value: " + dosage);
        }

        // Check for common dosage units
        boolean hasValidUnit = dosage.contains("mg") || dosage.contains("ml") || 
                              dosage.contains("g") || dosage.contains("mcg") || 
                              dosage.contains("unit") || dosage.contains("tablet");
        
        if (!hasValidUnit) {
            log.warn("Dosage may be missing unit: {} for {}", dosage, medicationName);
        }
    }

    private void validateFrequencyFormat(String frequency) {
        frequency = frequency.toLowerCase().trim();
        
        // Common frequency patterns
        boolean validFrequency = frequency.contains("daily") || frequency.contains("twice") || 
                               frequency.contains("three times") || frequency.contains("four times") ||
                               frequency.contains("every") || frequency.contains("as needed") ||
                               frequency.contains("bid") || frequency.contains("tid") || 
                               frequency.contains("qid") || frequency.contains("prn");
        
        if (!validFrequency) {
            throw new PrescriptionValidationException("Invalid frequency format: " + frequency);
        }
    }

    private void validateControlledSubstance(CreatePrescriptionItemRequest item) {
        String medicationName = item.getMedicationName().toLowerCase();
        
        if (isControlledSubstance(medicationName)) {
            // Controlled substances have special restrictions
            if (item.getRefillsAllowed() != null && item.getRefillsAllowed() > 5) {
                throw new PrescriptionValidationException(
                    "Controlled substances cannot have more than 5 refills: " + item.getMedicationName());
            }

            if (item.getQuantity() > 90) {
                log.warn("Large quantity prescribed for controlled substance: {} - {} units", 
                        item.getMedicationName(), item.getQuantity());
            }
        }
    }

    private void checkBasicDrugInteractions(List<CreatePrescriptionItemRequest> items) {
        boolean hasBloodThinner = false;
        boolean hasNSAID = false;

        for (CreatePrescriptionItemRequest item : items) {
            String medicationName = item.getMedicationName().toLowerCase();
            
            if (BLOOD_THINNERS.stream().anyMatch(medicationName::contains)) {
                hasBloodThinner = true;
            }
            
            if (NSAIDS.stream().anyMatch(medicationName::contains)) {
                hasNSAID = true;
            }
        }

        if (hasBloodThinner && hasNSAID) {
            throw new PrescriptionValidationException(
                "Dangerous interaction: Blood thinners and NSAIDs should not be prescribed together");
        }
    }

    private void checkDrugClassAllergies(String medicationName, Patient patient) {
        // Simplified drug class allergy checking
        if (medicationName.contains("penicillin") || medicationName.contains("amoxicillin")) {
            if (medicalRecordService.hasAllergy(patient.getId(), "penicillin")) {
                throw new PrescriptionValidationException("Patient is allergic to penicillin class drugs");
            }
        }

        if (medicationName.contains("sulfa")) {
            if (medicalRecordService.hasAllergy(patient.getId(), "sulfa")) {
                throw new PrescriptionValidationException("Patient is allergic to sulfa drugs");
            }
        }
    }

    private boolean isControlledSubstance(String medicationName) {
        return CONTROLLED_SUBSTANCES.stream()
                .anyMatch(substance -> medicationName.toLowerCase().contains(substance));
    }
}