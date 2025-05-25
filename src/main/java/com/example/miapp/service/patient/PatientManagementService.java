package com.example.miapp.service.patient;

import com.example.miapp.entity.Patient;
import com.example.miapp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for patient entity management operations (Single Responsibility)
 * Implements Command Pattern for operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PatientManagementService {

    private final PatientRepository patientRepository;

    /**
     * Updates patient address
     */
    public void updateAddress(Long patientId, String address) {
        log.info("Updating address for patient {}", patientId);
        
        int updatedRows = patientRepository.updateAddress(patientId, address);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update address for patient: " + patientId);
        }
        
        log.info("Successfully updated address for patient {}", patientId);
    }

    /**
     * Updates patient phone number
     */
    public void updatePhone(Long patientId, String phone) {
        log.info("Updating phone for patient {}", patientId);
        
        int updatedRows = patientRepository.updatePhone(patientId, phone);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update phone for patient: " + patientId);
        }
        
        log.info("Successfully updated phone for patient {}", patientId);
    }

    /**
     * Updates patient insurance information
     */
    public void updateInsuranceInfo(Long patientId, String insuranceProvider, String insurancePolicyNumber) {
        log.info("Updating insurance info for patient {}", patientId);
        
        int updatedRows = patientRepository.updateInsuranceInfo(patientId, insuranceProvider, insurancePolicyNumber);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update insurance info for patient: " + patientId);
        }
        
        log.info("Successfully updated insurance info for patient {}", patientId);
    }

    /**
     * Updates patient emergency contact information
     */
    public void updateEmergencyContact(Long patientId, String emergencyContactName, String emergencyContactPhone) {
        log.info("Updating emergency contact for patient {}", patientId);
        
        int updatedRows = patientRepository.updateEmergencyContact(patientId, emergencyContactName, emergencyContactPhone);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update emergency contact for patient: " + patientId);
        }
        
        log.info("Successfully updated emergency contact for patient {}", patientId);
    }

    /**
     * Updates patient profile information
     */
    public void updatePatientProfile(Long patientId, String firstName, String lastName, String phone, String address) {
        log.info("Updating profile for patient {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));
        
        boolean updated = false;
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            patient.setFirstName(firstName.trim());
            updated = true;
        }
        
        if (lastName != null && !lastName.trim().isEmpty()) {
            patient.setLastName(lastName.trim());
            updated = true;
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            patient.setPhone(phone.trim());
            updated = true;
        }
        
        if (address != null && !address.trim().isEmpty()) {
            patient.setAddress(address.trim());
            updated = true;
        }
        
        if (updated) {
            patientRepository.save(patient);
            log.info("Successfully updated profile for patient {}", patientId);
        } else {
            log.info("No updates needed for patient {}", patientId);
        }
    }

    /**
     * Updates patient medical information
     */
    public void updateMedicalInfo(Long patientId, String bloodType, String allergies, String chronicConditions) {
        log.info("Updating medical info for patient {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));
        
        boolean updated = false;
        
        if (bloodType != null) {
            patient.setBloodType(bloodType.trim().isEmpty() ? null : bloodType.trim());
            updated = true;
        }
        
        // Note: allergies and chronicConditions are in MedicalRecord, not Patient
        // This would require medical record service interaction
        
        if (updated) {
            patientRepository.save(patient);
            log.info("Successfully updated medical info for patient {}", patientId);
        }
    }

    /**
     * Activates or deactivates patient account
     */
    public void togglePatientStatus(Long patientId, boolean active) {
        log.info("Setting patient {} status to {}", patientId, active ? "active" : "inactive");
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));
        
        // Update user status
        if (patient.getUser() != null) {
            patient.getUser().setStatus(active ? 
                com.example.miapp.entity.User.UserStatus.ACTIVE : 
                com.example.miapp.entity.User.UserStatus.INACTIVE);
            
            patientRepository.save(patient);
            
            log.info("Successfully {} patient {}", 
                    active ? "activated" : "deactivated", patientId);
        } else {
            throw new RuntimeException("Patient has no associated user account: " + patientId);
        }
    }

    /**
     * Archives patient (soft delete alternative)
     */
    public void archivePatient(Long patientId, String reason) {
        log.info("Archiving patient {} with reason: {}", patientId, reason);
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));
        
        // Set user status to inactive instead of deleting
        if (patient.getUser() != null) {
            patient.getUser().setStatus(com.example.miapp.entity.User.UserStatus.INACTIVE);
            patientRepository.save(patient);
        }
        
        // In a real implementation, you might add an 'archived' flag and archive_reason
        // patient.setArchived(true);
        // patient.setArchiveReason(reason);
        // patient.setArchivedAt(LocalDateTime.now());
        
        log.info("Successfully archived patient {}", patientId);
    }

    /**
     * Deletes patient (hard delete - use with caution)
     */
    public void deletePatient(Long patientId) {
        log.info("Deleting patient {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));
        
        // Additional validations should be performed by validation service
        patientRepository.delete(patient);
        
        log.info("Successfully deleted patient {}", patientId);
    }

    /**
     * Merges duplicate patient records
     */
    public void mergePatientRecords(Patient keepPatient, Patient mergePatient, String reason) {
        log.info("Merging patient {} into patient {} with reason: {}", 
                mergePatient.getId(), keepPatient.getId(), reason);
        
        // This is a complex operation that would typically involve:
        // 1. Moving appointments from mergePatient to keepPatient
        // 2. Merging medical records
        // 3. Updating prescriptions
        // 4. Handling duplicate information conflicts
        // 5. Archiving the merged patient record
        
        // For now, we'll implement a basic version
        log.warn("Patient merge operation is complex and should be implemented carefully");
        
        // Move basic information if keepPatient is missing data
        if (keepPatient.getEmergencyContactName() == null && mergePatient.getEmergencyContactName() != null) {
            keepPatient.setEmergencyContactName(mergePatient.getEmergencyContactName());
            keepPatient.setEmergencyContactPhone(mergePatient.getEmergencyContactPhone());
        }
        
        if (keepPatient.getInsuranceProvider() == null && mergePatient.getInsuranceProvider() != null) {
            keepPatient.setInsuranceProvider(mergePatient.getInsuranceProvider());
            keepPatient.setInsurancePolicyNumber(mergePatient.getInsurancePolicyNumber());
        }
        
        if (keepPatient.getBloodType() == null && mergePatient.getBloodType() != null) {
            keepPatient.setBloodType(mergePatient.getBloodType());
        }
        
        patientRepository.save(keepPatient);
        
        // Archive the merged patient
        archivePatient(mergePatient.getId(), "Merged into patient " + keepPatient.getId() + ": " + reason);
        
        log.info("Successfully merged patient {} into patient {}", mergePatient.getId(), keepPatient.getId());
    }
}