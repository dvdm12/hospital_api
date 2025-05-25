package com.example.miapp.service.patient;

import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.entity.Patient;
import com.example.miapp.exception.PatientValidationException;
import com.example.miapp.repository.PatientRepository;
import com.example.miapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/**
 * Service responsible for patient validations (Single Responsibility)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientValidationService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    /**
     * Validates patient creation request
     */
    public void validatePatientCreation(CreatePatientRequest request) {
        validateUniqueEmail(request.getEmail(), null);
        validateUniquePhone(request.getPhone(), null);
        validateUniqueUsername(request.getUsername(), null);
        validateBirthDate(request.getBirthDate());
        validateInsuranceInfo(request.getInsuranceProvider(), request.getInsurancePolicyNumber());
        validateEmergencyContact(request.getEmergencyContactName(), request.getEmergencyContactPhone());
        validateBloodType(request.getBloodType());
    }

    /**
     * Validates patient update
     */
    public void validatePatientUpdate(Long patientId, CreatePatientRequest request) {
        validateUniqueEmail(request.getEmail(), patientId);
        validateUniquePhone(request.getPhone(), patientId);
        validateBirthDate(request.getBirthDate());
        validateInsuranceInfo(request.getInsuranceProvider(), request.getInsurancePolicyNumber());
        validateEmergencyContact(request.getEmergencyContactName(), request.getEmergencyContactPhone());
        validateBloodType(request.getBloodType());
    }

    /**
     * Validates if patient can be deleted
     */
    public void validatePatientDeletion(Patient patient) {
        // Check if patient has active appointments
        boolean hasActiveAppointments = patient.getAppointments() != null && 
            patient.getAppointments().stream()
                .anyMatch(appointment -> 
                    appointment.getStatus().name().equals("SCHEDULED") || 
                    appointment.getStatus().name().equals("CONFIRMED"));

        if (hasActiveAppointments) {
            throw new PatientValidationException("Cannot delete patient with active appointments");
        }

        // Check if patient has active prescriptions
        boolean hasActivePrescriptions = patient.getPrescriptions() != null &&
            patient.getPrescriptions().stream()
                .anyMatch(prescription -> 
                    prescription.getStatus().name().equals("ACTIVE"));

        if (hasActivePrescriptions) {
            throw new PatientValidationException("Cannot delete patient with active prescriptions");
        }

        // Check if patient has medical records with sensitive information
        if (patient.getMedicalRecord() != null && 
            patient.getMedicalRecord().getEntries() != null &&
            !patient.getMedicalRecord().getEntries().isEmpty()) {
            log.warn("Deleting patient {} with existing medical records", patient.getId());
        }
    }

    /**
     * Validates emergency contact update
     */
    public void validateEmergencyContactUpdate(String contactName, String contactPhone) {
        validateEmergencyContact(contactName, contactPhone);
    }

    /**
     * Validates insurance information update
     */
    public void validateInsuranceUpdate(String provider, String policyNumber) {
        validateInsuranceInfo(provider, policyNumber);
    }

    // Private validation methods

    private void validateUniqueEmail(String email, Long excludePatientId) {
        if (email == null || email.trim().isEmpty()) {
            return; // Email validation handled by @Email annotation
        }
        
        // Check in patient repository (email is not unique constraint in Patient entity but good practice)
        patientRepository.findByEmail(email)
            .filter(patient -> excludePatientId == null || !patient.getId().equals(excludePatientId))
            .ifPresent(patient -> {
                throw new PatientValidationException("Email already exists for another patient: " + email);
            });
    }

    public void validateUniquePhone(String phone, Long excludePatientId) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new PatientValidationException("Phone number is required");
        }
        
        patientRepository.findByPhone(phone)
            .filter(patient -> excludePatientId == null || !patient.getId().equals(excludePatientId))
            .ifPresent(patient -> {
                throw new PatientValidationException("Phone number already exists: " + phone);
            });
    }

    private void validateUniqueUsername(String username, Long excludeUserId) {
        if (username == null || username.trim().isEmpty()) {
            throw new PatientValidationException("Username is required");
        }
        
        if (userRepository.existsByUsername(username)) {
            throw new PatientValidationException("Username already exists: " + username);
        }
    }

    private void validateBirthDate(Date birthDate) {
        if (birthDate == null) {
            throw new PatientValidationException("Birth date is required");
        }

        LocalDate birth = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();

        if (birth.isAfter(now)) {
            throw new PatientValidationException("Birth date cannot be in the future");
        }

        // Calculate age
        int age = Period.between(birth, now).getYears();

        if (age > 150) {
            throw new PatientValidationException("Invalid birth date - age cannot exceed 150 years");
        }

        // Business rule: newborns should be at least 1 day old for registration
        if (birth.isEqual(now)) {
            throw new PatientValidationException("Patient must be at least 1 day old for registration");
        }
    }

    private void validateInsuranceInfo(String provider, String policyNumber) {
        // If provider is specified, policy number should also be specified
        if (provider != null && !provider.trim().isEmpty()) {
            if (policyNumber == null || policyNumber.trim().isEmpty()) {
                throw new PatientValidationException("Insurance policy number is required when provider is specified");
            }
            
            // Validate policy number format (example: basic alphanumeric check)
            if (!policyNumber.matches("^[A-Za-z0-9\\-]+$")) {
                throw new PatientValidationException("Insurance policy number contains invalid characters");
            }
        }
        
        // If policy number is specified, provider should also be specified
        if (policyNumber != null && !policyNumber.trim().isEmpty()) {
            if (provider == null || provider.trim().isEmpty()) {
                throw new PatientValidationException("Insurance provider is required when policy number is specified");
            }
        }
    }

    private void validateEmergencyContact(String contactName, String contactPhone) {
        // Emergency contact is recommended but not mandatory
        if (contactName != null && !contactName.trim().isEmpty()) {
            if (contactPhone == null || contactPhone.trim().isEmpty()) {
                throw new PatientValidationException("Emergency contact phone is required when contact name is provided");
            }
            
            // Validate phone format (basic validation)
            if (!isValidPhoneFormat(contactPhone)) {
                throw new PatientValidationException("Invalid emergency contact phone format");
            }
        }
        
        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            if (contactName == null || contactName.trim().isEmpty()) {
                throw new PatientValidationException("Emergency contact name is required when phone is provided");
            }
        }
    }

    private void validateBloodType(String bloodType) {
        if (bloodType != null && !bloodType.trim().isEmpty()) {
            // Validate blood type format
            String[] validBloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            boolean isValid = false;
            
            for (String validType : validBloodTypes) {
                if (validType.equalsIgnoreCase(bloodType.trim())) {
                    isValid = true;
                    break;
                }
            }
            
            if (!isValid) {
                throw new PatientValidationException("Invalid blood type: " + bloodType + 
                    ". Valid types are: A+, A-, B+, B-, AB+, AB-, O+, O-");
            }
        }
    }

    private boolean isValidPhoneFormat(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Basic phone validation: digits, spaces, dashes, parentheses, plus sign
        return phone.matches("^[\\+]?[\\d\\s\\-\\(\\)]+$") && phone.replaceAll("[^\\d]", "").length() >= 7;
    }
}