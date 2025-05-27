package com.example.miapp.service.patient;

import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Prescription;
import com.example.miapp.entity.User;
import com.example.miapp.exception.PatientValidationException;
import com.example.miapp.repository.PatientRepository;
import com.example.miapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientValidationServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientValidationService validationService;

    private CreatePatientRequest validRequest;
    private Patient patient;

    @BeforeEach
    void setUp() {
        // Configurar una solicitud válida para la mayoría de las pruebas
        validRequest = new CreatePatientRequest();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPhone("1234567890");
        validRequest.setUsername("johndoe");
        
        // Configurar una fecha de nacimiento válida (hace 30 años)
        LocalDate birthDate = LocalDate.now().minusYears(30);
        validRequest.setBirthDate(Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
        validRequest.setGender(Patient.Gender.MALE);
        validRequest.setBloodType("A+");
        validRequest.setInsuranceProvider("Example Insurance");
        validRequest.setInsurancePolicyNumber("POL-123456");
        validRequest.setEmergencyContactName("Jane Doe");
        validRequest.setEmergencyContactPhone("9876543210");
        
        // Configurar un paciente para pruebas
        patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        
        User user = new User();
        user.setId(1L);
        user.setUsername("johndoe");
        user.setEmail("john.doe@example.com");
        
        patient.setUser(user);
    }

    @Nested
    @DisplayName("validatePatientCreation Tests")
    class ValidatePatientCreationTests {
        
        @Test
        @DisplayName("Should pass validation with valid request")
        void shouldPassValidationWithValidRequest() {
            // Arrange
            when(patientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(patientRepository.findByPhone(anyString())).thenReturn(Optional.empty());
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validatePatientCreation(validRequest));
        }
        
        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Arrange
            when(patientRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(patient));
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientCreation(validRequest));
            
            assertTrue(exception.getMessage().contains("Email already exists"));
        }
        
        @Test
        @DisplayName("Should throw exception when phone already exists")
        void shouldThrowExceptionWhenPhoneAlreadyExists() {
            // Arrange
            when(patientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(patientRepository.findByPhone(validRequest.getPhone())).thenReturn(Optional.of(patient));
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientCreation(validRequest));
            
            assertTrue(exception.getMessage().contains("Phone number already exists"));
        }
        
        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameAlreadyExists() {
            // Arrange
            when(patientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(patientRepository.findByPhone(anyString())).thenReturn(Optional.empty());
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(true);
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientCreation(validRequest));
            
            assertTrue(exception.getMessage().contains("Username already exists"));
        }
    }
    
    @Nested
    @DisplayName("validateBirthDate Tests")
    class ValidateBirthDateTests {
        
        @Test
        @DisplayName("Should throw exception when birth date is null")
        void shouldThrowExceptionWhenBirthDateIsNull() {
            // Arrange
            validRequest.setBirthDate(null);
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientCreation(validRequest));
            
            assertEquals("Birth date is required", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when birth date is in the future")
        void shouldThrowExceptionWhenBirthDateIsInFuture() {
            // Arrange
            LocalDate futureDate = LocalDate.now().plusDays(1);
            validRequest.setBirthDate(Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientCreation(validRequest));
            
            assertEquals("Birth date cannot be in the future", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when birth date gives age over 150")
        void shouldThrowExceptionWhenAgeIsOverMaximum() {
            // Arrange
            LocalDate veryOldDate = LocalDate.now().minusYears(151);
            validRequest.setBirthDate(Date.from(veryOldDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientCreation(validRequest));
            
            assertEquals("Invalid birth date - age cannot exceed 150 years", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when birth date is today")
        void shouldThrowExceptionWhenBirthDateIsToday() {
            // Arrange
            LocalDate today = LocalDate.now();
            validRequest.setBirthDate(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientCreation(validRequest));
            
            assertEquals("Patient must be at least 1 day old for registration", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("validateInsuranceInfo Tests")
    class ValidateInsuranceInfoTests {
        
        @Test
        @DisplayName("Should pass validation with both provider and policy")
        void shouldPassValidationWithBothProviderAndPolicy() {
            // Arrange
            validRequest.setInsuranceProvider("Provider");
            validRequest.setInsurancePolicyNumber("POL-123");
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateInsuranceUpdate(
                    validRequest.getInsuranceProvider(), 
                    validRequest.getInsurancePolicyNumber()));
        }
        
        @Test
        @DisplayName("Should pass validation with no insurance info")
        void shouldPassValidationWithNoInsuranceInfo() {
            // Arrange
            validRequest.setInsuranceProvider(null);
            validRequest.setInsurancePolicyNumber(null);
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateInsuranceUpdate(
                    validRequest.getInsuranceProvider(), 
                    validRequest.getInsurancePolicyNumber()));
        }
        
        @Test
        @DisplayName("Should throw exception when provider exists but policy is missing")
        void shouldThrowExceptionWhenProviderExistsButPolicyIsMissing() {
            // Arrange
            validRequest.setInsuranceProvider("Provider");
            validRequest.setInsurancePolicyNumber(null);
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validateInsuranceUpdate(
                            validRequest.getInsuranceProvider(), 
                            validRequest.getInsurancePolicyNumber()));
            
            assertEquals("Insurance policy number is required when provider is specified", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when policy exists but provider is missing")
        void shouldThrowExceptionWhenPolicyExistsButProviderIsMissing() {
            // Arrange
            validRequest.setInsuranceProvider(null);
            validRequest.setInsurancePolicyNumber("POL-123");
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validateInsuranceUpdate(
                            validRequest.getInsuranceProvider(), 
                            validRequest.getInsurancePolicyNumber()));
            
            assertEquals("Insurance provider is required when policy number is specified", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when policy has invalid format")
        void shouldThrowExceptionWhenPolicyHasInvalidFormat() {
            // Arrange
            validRequest.setInsuranceProvider("Provider");
            validRequest.setInsurancePolicyNumber("Invalid@Policy#");
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validateInsuranceUpdate(
                            validRequest.getInsuranceProvider(), 
                            validRequest.getInsurancePolicyNumber()));
            
            assertEquals("Insurance policy number contains invalid characters", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("validateBloodType Tests")
    class ValidateBloodTypeTests {
        
        @Test
        @DisplayName("Should pass validation with valid blood type")
        void shouldPassValidationWithValidBloodType() {
            // Test all valid blood types
            String[] validBloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            
            for (String bloodType : validBloodTypes) {
                validRequest.setBloodType(bloodType);
                assertDoesNotThrow(() -> validationService.validatePatientCreation(validRequest));
            }
        }
        
        @Test
        @DisplayName("Should pass validation with null blood type")
        void shouldPassValidationWithNullBloodType() {
            // Arrange
            validRequest.setBloodType(null);
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validatePatientCreation(validRequest));
        }
        
        @Test
        @DisplayName("Should throw exception with invalid blood type")
        void shouldThrowExceptionWithInvalidBloodType() {
            // Arrange
            validRequest.setBloodType("XYZ");
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientCreation(validRequest));
            
            assertTrue(exception.getMessage().contains("Invalid blood type"));
        }
    }
    
    @Nested
    @DisplayName("validateEmergencyContact Tests")
    class ValidateEmergencyContactTests {
        
        @Test
        @DisplayName("Should pass validation with both name and phone")
        void shouldPassValidationWithBothNameAndPhone() {
            // Arrange
            validRequest.setEmergencyContactName("Contact Name");
            validRequest.setEmergencyContactPhone("1234567890");
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateEmergencyContactUpdate(
                    validRequest.getEmergencyContactName(), 
                    validRequest.getEmergencyContactPhone()));
        }
        
        @Test
        @DisplayName("Should pass validation with no emergency contact")
        void shouldPassValidationWithNoEmergencyContact() {
            // Arrange
            validRequest.setEmergencyContactName(null);
            validRequest.setEmergencyContactPhone(null);
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateEmergencyContactUpdate(
                    validRequest.getEmergencyContactName(), 
                    validRequest.getEmergencyContactPhone()));
        }
        
        @Test
        @DisplayName("Should throw exception when name exists but phone is missing")
        void shouldThrowExceptionWhenNameExistsButPhoneIsMissing() {
            // Arrange
            validRequest.setEmergencyContactName("Contact Name");
            validRequest.setEmergencyContactPhone(null);
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validateEmergencyContactUpdate(
                            validRequest.getEmergencyContactName(), 
                            validRequest.getEmergencyContactPhone()));
            
            assertEquals("Emergency contact phone is required when contact name is provided", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when phone exists but name is missing")
        void shouldThrowExceptionWhenPhoneExistsButNameIsMissing() {
            // Arrange
            validRequest.setEmergencyContactName(null);
            validRequest.setEmergencyContactPhone("1234567890");
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validateEmergencyContactUpdate(
                            validRequest.getEmergencyContactName(), 
                            validRequest.getEmergencyContactPhone()));
            
            assertEquals("Emergency contact name is required when phone is provided", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when phone has invalid format")
        void shouldThrowExceptionWhenPhoneHasInvalidFormat() {
            // Arrange
            validRequest.setEmergencyContactName("Contact Name");
            validRequest.setEmergencyContactPhone("invalid@phone");
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validateEmergencyContactUpdate(
                            validRequest.getEmergencyContactName(), 
                            validRequest.getEmergencyContactPhone()));
            
            assertEquals("Invalid emergency contact phone format", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("validatePatientDeletion Tests")
    class ValidatePatientDeletionTests {
        
        @Test
        @DisplayName("Should pass validation for patient without appointments or prescriptions")
        void shouldPassValidationForPatientWithoutAppointmentsOrPrescriptions() {
            // Arrange
            patient.setAppointments(new ArrayList<>());
            patient.setPrescriptions(new ArrayList<>());
            
            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setEntries(new ArrayList<>());
            patient.setMedicalRecord(medicalRecord);
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validatePatientDeletion(patient));
        }
        
        @Test
        @DisplayName("Should throw exception when patient has active appointments")
        void shouldThrowExceptionWhenPatientHasActiveAppointments() {
            // Arrange
            List<Appointment> appointments = new ArrayList<>();
            Appointment activeAppointment = new Appointment();
            activeAppointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
            appointments.add(activeAppointment);
            
            patient.setAppointments(appointments);
            patient.setPrescriptions(new ArrayList<>());
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientDeletion(patient));
            
            assertEquals("Cannot delete patient with active appointments", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when patient has active prescriptions")
        void shouldThrowExceptionWhenPatientHasActivePrescriptions() {
            // Arrange
            patient.setAppointments(new ArrayList<>());
            
            List<Prescription> prescriptions = new ArrayList<>();
            Prescription activePrescription = new Prescription();
            activePrescription.setStatus(Prescription.PrescriptionStatus.ACTIVE);
            prescriptions.add(activePrescription);
            
            patient.setPrescriptions(prescriptions);
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validatePatientDeletion(patient));
            
            assertEquals("Cannot delete patient with active prescriptions", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("validateUniquePhone Tests")
    class ValidateUniquePhoneTests {
        
        @Test
        @DisplayName("Should pass validation when phone is unique")
        void shouldPassValidationWhenPhoneIsUnique() {
            // Arrange
            String phone = "1234567890";
            when(patientRepository.findByPhone(phone)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateUniquePhone(phone, null));
        }
        
        @Test
        @DisplayName("Should pass validation when phone belongs to same patient")
        void shouldPassValidationWhenPhoneBelongsToSamePatient() {
            // Arrange
            String phone = "1234567890";
            Long patientId = 1L;
            
            Patient existingPatient = new Patient();
            existingPatient.setId(patientId);
            
            when(patientRepository.findByPhone(phone)).thenReturn(Optional.of(existingPatient));
            
            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateUniquePhone(phone, patientId));
        }
        
        @Test
        @DisplayName("Should throw exception when phone belongs to different patient")
        void shouldThrowExceptionWhenPhoneBelongsToDifferentPatient() {
            // Arrange
            String phone = "1234567890";
            Long patientId = 1L;
            
            Patient existingPatient = new Patient();
            existingPatient.setId(2L); // Different ID
            
            when(patientRepository.findByPhone(phone)).thenReturn(Optional.of(existingPatient));
            
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validateUniquePhone(phone, patientId));
            
            assertEquals("Phone number already exists: " + phone, exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when phone is null")
        void shouldThrowExceptionWhenPhoneIsNull() {
            // Act & Assert
            Exception exception = assertThrows(PatientValidationException.class, 
                    () -> validationService.validateUniquePhone(null, 1L));
            
            assertEquals("Phone number is required", exception.getMessage());
        }
    }
}