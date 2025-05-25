package com.example.miapp.service.medicalrecord;

import com.example.miapp.dto.medicalrecord.CreateMedicalEntryRequest;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import com.example.miapp.entity.Patient;
import com.example.miapp.exception.MedicalRecordValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service responsible for medical record validations (Single Responsibility)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordValidationService {

    /**
     * Validates medical record creation
     */
    public void validateMedicalRecordCreation(Patient patient) {
        if (patient == null) {
            throw new MedicalRecordValidationException("Patient is required for medical record creation");
        }
        
        if (patient.getMedicalRecord() != null) {
            throw new MedicalRecordValidationException("Patient already has a medical record");
        }
    }

    /**
     * Validates medical record entry creation
     */
    public void validateMedicalEntryCreation(CreateMedicalEntryRequest request, MedicalRecord medicalRecord, Doctor doctor) {
        validateEntryRequest(request);
        
        if (medicalRecord == null) {
            throw new MedicalRecordValidationException("Medical record not found");
        }
        
        if (doctor == null) {
            throw new MedicalRecordValidationException("Doctor not found");
        }
        
        validateDoctorCanCreateEntry(doctor, medicalRecord);
    }

    /**
     * Validates medical record entry update
     */
    public void validateMedicalEntryUpdate(CreateMedicalEntryRequest request, MedicalRecordEntry existingEntry, Doctor doctor) {
        validateEntryRequest(request);
        
        if (existingEntry == null) {
            throw new MedicalRecordValidationException("Medical record entry not found");
        }
        
        if (doctor == null) {
            throw new MedicalRecordValidationException("Doctor not found");
        }
        
        validateDoctorCanModifyEntry(doctor, existingEntry);
    }

    /**
     * Validates medical record field updates
     */
    public void validateMedicalRecordUpdate(String allergies, String chronicConditions, 
                                          String currentMedications, String surgicalHistory, 
                                          String familyHistory, String notes) {
        
        // Validate length constraints
        if (allergies != null && allergies.length() > 500) {
            throw new MedicalRecordValidationException("Allergies text cannot exceed 500 characters");
        }
        
        if (chronicConditions != null && chronicConditions.length() > 500) {
            throw new MedicalRecordValidationException("Chronic conditions text cannot exceed 500 characters");
        }
        
        if (currentMedications != null && currentMedications.length() > 500) {
            throw new MedicalRecordValidationException("Current medications text cannot exceed 500 characters");
        }
        
        if (surgicalHistory != null && surgicalHistory.length() > 500) {
            throw new MedicalRecordValidationException("Surgical history text cannot exceed 500 characters");
        }
        
        if (familyHistory != null && familyHistory.length() > 1000) {
            throw new MedicalRecordValidationException("Family history text cannot exceed 1000 characters");
        }
        
        if (notes != null && notes.length() > 1000) {
            throw new MedicalRecordValidationException("Notes text cannot exceed 1000 characters");
        }
        
        // Validate medical content format (basic validation)
        if (allergies != null && !allergies.trim().isEmpty()) {
            validateMedicalText(allergies, "allergies");
        }
        
        if (currentMedications != null && !currentMedications.trim().isEmpty()) {
            validateMedicationText(currentMedications);
        }
    }

    /**
     * Validates if medical record can be deleted
     */
    public void validateMedicalRecordDeletion(MedicalRecord medicalRecord) {
        if (medicalRecord == null) {
            throw new MedicalRecordValidationException("Medical record not found");
        }
        
        // Check if medical record has entries
        if (medicalRecord.getEntries() != null && !medicalRecord.getEntries().isEmpty()) {
            boolean hasImportantEntries = medicalRecord.getEntries().stream()
                .anyMatch(entry -> 
                    entry.getType() == MedicalRecordEntry.EntryType.DIAGNOSIS ||
                    entry.getType() == MedicalRecordEntry.EntryType.SURGERY ||
                    entry.getType() == MedicalRecordEntry.EntryType.LAB_RESULT);
            
            if (hasImportantEntries) {
                throw new MedicalRecordValidationException(
                    "Cannot delete medical record with important entries (diagnosis, surgery, lab results)");
            }
        }
    }

    /**
     * Validates if entry can be deleted
     */
    public void validateEntryDeletion(MedicalRecordEntry entry, Doctor requestingDoctor) {
        if (entry == null) {
            throw new MedicalRecordValidationException("Medical record entry not found");
        }
        
        validateDoctorCanModifyEntry(requestingDoctor, entry);
        
        // Check if entry is critical (e.g., diagnosis, surgery records)
        if (entry.getType() == MedicalRecordEntry.EntryType.SURGERY) {
            throw new MedicalRecordValidationException("Surgery records cannot be deleted for legal compliance");
        }
        
        // Check entry age - old entries might be protected
        if (entry.getEntryDate() != null && 
            entry.getEntryDate().isBefore(LocalDateTime.now().minusYears(1))) {
            log.warn("Attempting to delete old medical record entry: {}", entry.getId());
        }
    }

    /**
     * Validates patient access to medical record
     */
    public void validatePatientAccess(MedicalRecord medicalRecord, Long patientId) {
        if (medicalRecord == null) {
            throw new MedicalRecordValidationException("Medical record not found");
        }
        
        if (!medicalRecord.getPatient().getId().equals(patientId)) {
            throw new MedicalRecordValidationException("Access denied: Medical record belongs to different patient");
        }
    }

    // Private validation methods

    private void validateEntryRequest(CreateMedicalEntryRequest request) {
        if (request.getType() == null) {
            throw new MedicalRecordValidationException("Entry type is required");
        }
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new MedicalRecordValidationException("Entry title is required");
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new MedicalRecordValidationException("Entry content is required");
        }
        
        if (request.getDoctorId() == null) {
            throw new MedicalRecordValidationException("Doctor ID is required");
        }
        
        // Validate content based on entry type
        validateContentByType(request.getType(), request.getContent());
    }

    private void validateDoctorCanCreateEntry(Doctor doctor, MedicalRecord medicalRecord) {
        // Basic validation - doctor should be active
        if (doctor.getUser() == null || doctor.getUser().getStatus() != com.example.miapp.entity.User.UserStatus.ACTIVE) {
            throw new MedicalRecordValidationException("Only active doctors can create medical record entries");
        }
        
        // Additional business rules can be added here
        // e.g., doctor should have treated the patient, specific specialties for certain entry types, etc.
    }

    private void validateDoctorCanModifyEntry(Doctor doctor, MedicalRecordEntry entry) {
        if (doctor == null) {
            throw new MedicalRecordValidationException("Doctor is required");
        }
        
        // Business rule: Only the original doctor or authorized doctors can modify entries
        if (!entry.getDoctor().getId().equals(doctor.getId())) {
            // Check if doctor has admin privileges or is in the same department
            boolean canModify = doctor.getUser().getRoles().stream()
                .anyMatch(role -> role.getName().name().contains("ADMIN"));
            
            if (!canModify) {
                throw new MedicalRecordValidationException(
                    "Only the original doctor or authorized personnel can modify this entry");
            }
        }
        
        // Time-based restrictions
        if (entry.getEntryDate() != null && 
            entry.getEntryDate().isBefore(LocalDateTime.now().minusDays(30))) {
            log.warn("Modifying medical record entry older than 30 days: {}", entry.getId());
        }
    }

    private void validateContentByType(MedicalRecordEntry.EntryType type, String content) {
        switch (type) {
            case DIAGNOSIS:
                validateDiagnosisContent(content);
                break;
            case PRESCRIPTION:
                validatePrescriptionContent(content);
                break;
            case LAB_RESULT:
                validateLabResultContent(content);
                break;
            case SURGERY:
                validateSurgeryContent(content);
                break;
            default:
                // General content validation
                if (content.length() < 10) {
                    throw new MedicalRecordValidationException("Entry content seems too short for meaningful medical information");
                }
        }
    }

    private void validateDiagnosisContent(String content) {
        // Basic validation for diagnosis entries
        if (content.length() < 20) {
            throw new MedicalRecordValidationException("Diagnosis content should be detailed (minimum 20 characters)");
        }
        
        // Could add ICD-10 code validation here
    }

    private void validatePrescriptionContent(String content) {
        // Basic validation for prescription entries
        if (!content.toLowerCase().contains("medication") && 
            !content.toLowerCase().contains("dosage") &&
            !content.toLowerCase().contains("mg") &&
            !content.toLowerCase().contains("ml")) {
            log.warn("Prescription entry might be missing medication details");
        }
    }

    private void validateLabResultContent(String content) {
        // Basic validation for lab result entries
        if (content.length() < 15) {
            throw new MedicalRecordValidationException("Lab result content should include detailed results");
        }
    }

    private void validateSurgeryContent(String content) {
        // Surgery records require detailed information
        if (content.length() < 50) {
            throw new MedicalRecordValidationException("Surgery records must contain detailed information (minimum 50 characters)");
        }
    }

    private void validateMedicalText(String text, String field) {
        // Basic medical text validation
        if (text.contains("<script>") || text.contains("javascript:")) {
            throw new MedicalRecordValidationException("Invalid characters detected in " + field);
        }
        
        // Check for proper medical terminology format
        if (text.matches(".*[0-9]{10,}.*")) {
            log.warn("Suspicious numeric content in {}: might contain unintended data", field);
        }
    }

    private void validateMedicationText(String medications) {
        // Basic medication text validation
        if (medications.toLowerCase().contains("unknown") && medications.trim().length() < 20) {
            log.warn("Current medications marked as unknown - might need follow-up");
        }
        
        // Check for dangerous medication combinations (simplified example)
        String lowerMeds = medications.toLowerCase();
        if (lowerMeds.contains("warfarin") && lowerMeds.contains("aspirin")) {
            log.warn("Potential medication interaction detected: warfarin + aspirin");
        }
    }
}