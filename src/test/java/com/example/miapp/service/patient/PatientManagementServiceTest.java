package com.example.miapp.service.patient;

import com.example.miapp.entity.Patient;
import com.example.miapp.entity.User;
import com.example.miapp.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientManagementServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientManagementService managementService;

    @Captor
    private ArgumentCaptor<Patient> patientCaptor;

    private Patient testPatient;
    private User testUser;
    private final Long patientId = 1L;

    @BeforeEach
    void setUp() {
        // Setup test patient
        testPatient = new Patient();
        testPatient.setId(patientId);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setPhone("1234567890");
        testPatient.setAddress("123 Main St");
        testPatient.setInsuranceProvider("Test Insurance");
        testPatient.setInsurancePolicyNumber("POL-123456");
        testPatient.setEmergencyContactName("Jane Doe");
        testPatient.setEmergencyContactPhone("9876543210");

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setStatus(User.UserStatus.ACTIVE);

        testPatient.setUser(testUser);
    }

    @Nested
    @DisplayName("Address Update Tests")
    class AddressUpdateTests {

        @Test
        @DisplayName("Should update address successfully")
        void shouldUpdateAddressSuccessfully() {
            // Arrange
            String newAddress = "456 New St";
            when(patientRepository.updateAddress(patientId, newAddress)).thenReturn(1);

            // Act
            managementService.updateAddress(patientId, newAddress);

            // Assert
            verify(patientRepository).updateAddress(patientId, newAddress);
        }

        @Test
        @DisplayName("Should throw exception when update fails")
        void shouldThrowExceptionWhenUpdateFails() {
            // Arrange
            String newAddress = "456 New St";
            when(patientRepository.updateAddress(patientId, newAddress)).thenReturn(0);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> managementService.updateAddress(patientId, newAddress));

            assertTrue(exception.getMessage().contains("Failed to update address"));
            verify(patientRepository).updateAddress(patientId, newAddress);
        }
    }

    @Nested
    @DisplayName("Phone Update Tests")
    class PhoneUpdateTests {

        @Test
        @DisplayName("Should update phone successfully")
        void shouldUpdatePhoneSuccessfully() {
            // Arrange
            String newPhone = "5555555555";
            when(patientRepository.updatePhone(patientId, newPhone)).thenReturn(1);

            // Act
            managementService.updatePhone(patientId, newPhone);

            // Assert
            verify(patientRepository).updatePhone(patientId, newPhone);
        }

        @Test
        @DisplayName("Should throw exception when update fails")
        void shouldThrowExceptionWhenUpdateFails() {
            // Arrange
            String newPhone = "5555555555";
            when(patientRepository.updatePhone(patientId, newPhone)).thenReturn(0);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> managementService.updatePhone(patientId, newPhone));

            assertTrue(exception.getMessage().contains("Failed to update phone"));
            verify(patientRepository).updatePhone(patientId, newPhone);
        }
    }

    @Nested
    @DisplayName("Insurance Info Update Tests")
    class InsuranceInfoUpdateTests {

        @Test
        @DisplayName("Should update insurance info successfully")
        void shouldUpdateInsuranceInfoSuccessfully() {
            // Arrange
            String newProvider = "New Insurance";
            String newPolicy = "POL-987654";
            when(patientRepository.updateInsuranceInfo(patientId, newProvider, newPolicy)).thenReturn(1);

            // Act
            managementService.updateInsuranceInfo(patientId, newProvider, newPolicy);

            // Assert
            verify(patientRepository).updateInsuranceInfo(patientId, newProvider, newPolicy);
        }

        @Test
        @DisplayName("Should throw exception when update fails")
        void shouldThrowExceptionWhenUpdateFails() {
            // Arrange
            String newProvider = "New Insurance";
            String newPolicy = "POL-987654";
            when(patientRepository.updateInsuranceInfo(patientId, newProvider, newPolicy)).thenReturn(0);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> managementService.updateInsuranceInfo(patientId, newProvider, newPolicy));

            assertTrue(exception.getMessage().contains("Failed to update insurance info"));
            verify(patientRepository).updateInsuranceInfo(patientId, newProvider, newPolicy);
        }
    }

    @Nested
    @DisplayName("Emergency Contact Update Tests")
    class EmergencyContactUpdateTests {

        @Test
        @DisplayName("Should update emergency contact successfully")
        void shouldUpdateEmergencyContactSuccessfully() {
            // Arrange
            String newName = "New Contact";
            String newPhone = "5555555555";
            when(patientRepository.updateEmergencyContact(patientId, newName, newPhone)).thenReturn(1);

            // Act
            managementService.updateEmergencyContact(patientId, newName, newPhone);

            // Assert
            verify(patientRepository).updateEmergencyContact(patientId, newName, newPhone);
        }

        @Test
        @DisplayName("Should throw exception when update fails")
        void shouldThrowExceptionWhenUpdateFails() {
            // Arrange
            String newName = "New Contact";
            String newPhone = "5555555555";
            when(patientRepository.updateEmergencyContact(patientId, newName, newPhone)).thenReturn(0);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> managementService.updateEmergencyContact(patientId, newName, newPhone));

            assertTrue(exception.getMessage().contains("Failed to update emergency contact"));
            verify(patientRepository).updateEmergencyContact(patientId, newName, newPhone);
        }
    }

    @Nested
    @DisplayName("Patient Profile Update Tests")
    class PatientProfileUpdateTests {

        @Test
        @DisplayName("Should update profile successfully with all fields")
        void shouldUpdateProfileSuccessfullyWithAllFields() {
            // Arrange
            String newFirstName = "Jack";
            String newLastName = "Smith";
            String newPhone = "5555555555";
            String newAddress = "789 Update St";
            
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

            // Act
            managementService.updatePatientProfile(patientId, newFirstName, newLastName, newPhone, newAddress);

            // Assert
            verify(patientRepository).save(patientCaptor.capture());
            Patient savedPatient = patientCaptor.getValue();
            
            assertEquals(newFirstName, savedPatient.getFirstName());
            assertEquals(newLastName, savedPatient.getLastName());
            assertEquals(newPhone, savedPatient.getPhone());
            assertEquals(newAddress, savedPatient.getAddress());
        }

        @Test
        @DisplayName("Should update profile successfully with some fields null")
        void shouldUpdateProfileSuccessfullyWithSomeFieldsNull() {
            // Arrange
            String newFirstName = "Jack";
            String newLastName = null;
            String newPhone = "5555555555";
            String newAddress = null;
            
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

            // Act
            managementService.updatePatientProfile(patientId, newFirstName, newLastName, newPhone, newAddress);

            // Assert
            verify(patientRepository).save(patientCaptor.capture());
            Patient savedPatient = patientCaptor.getValue();
            
            assertEquals(newFirstName, savedPatient.getFirstName());
            assertEquals("Doe", savedPatient.getLastName()); // Should not change
            assertEquals(newPhone, savedPatient.getPhone());
            assertEquals("123 Main St", savedPatient.getAddress()); // Should not change
        }
        
        @Test
        @DisplayName("Should not update profile when all fields are null or empty")
        void shouldNotUpdateProfileWhenAllFieldsAreNullOrEmpty() {
            // Arrange
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

            // Act
            managementService.updatePatientProfile(patientId, null, "", "  ", null);

            // Assert
            verify(patientRepository, never()).save(any(Patient.class));
        }
        
        @Test
        @DisplayName("Should throw exception when patient not found")
        void shouldThrowExceptionWhenPatientNotFound() {
            // Arrange
            when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> managementService.updatePatientProfile(patientId, "Jack", "Smith", "5555555555", "789 Update St"));

            assertTrue(exception.getMessage().contains("Patient not found"));
        }
    }

    @Nested
    @DisplayName("Toggle Patient Status Tests")
    class TogglePatientStatusTests {

        @Test
        @DisplayName("Should activate patient successfully")
        void shouldActivatePatientSuccessfully() {
            // Arrange
            testUser.setStatus(User.UserStatus.INACTIVE);
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

            // Act
            managementService.togglePatientStatus(patientId, true);

            // Assert
            verify(patientRepository).save(patientCaptor.capture());
            Patient savedPatient = patientCaptor.getValue();
            
            assertEquals(User.UserStatus.ACTIVE, savedPatient.getUser().getStatus());
        }

        @Test
        @DisplayName("Should deactivate patient successfully")
        void shouldDeactivatePatientSuccessfully() {
            // Arrange
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

            // Act
            managementService.togglePatientStatus(patientId, false);

            // Assert
            verify(patientRepository).save(patientCaptor.capture());
            Patient savedPatient = patientCaptor.getValue();
            
            assertEquals(User.UserStatus.INACTIVE, savedPatient.getUser().getStatus());
        }
        
        @Test
        @DisplayName("Should throw exception when patient not found")
        void shouldThrowExceptionWhenPatientNotFound() {
            // Arrange
            when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> managementService.togglePatientStatus(patientId, true));

            assertTrue(exception.getMessage().contains("Patient not found"));
        }
        
        @Test
        @DisplayName("Should throw exception when patient has no user")
        void shouldThrowExceptionWhenPatientHasNoUser() {
            // Arrange
            testPatient.setUser(null);
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> managementService.togglePatientStatus(patientId, true));

            assertTrue(exception.getMessage().contains("Patient has no associated user account"));
        }
    }

    @Nested
    @DisplayName("Archive Patient Tests")
    class ArchivePatientTests {

        @Test
        @DisplayName("Should archive patient successfully")
        void shouldArchivePatientSuccessfully() {
            // Arrange
            String reason = "Patient requested archival";
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

            // Act
            managementService.archivePatient(patientId, reason);

            // Assert
            verify(patientRepository).save(patientCaptor.capture());
            Patient savedPatient = patientCaptor.getValue();
            
            assertEquals(User.UserStatus.INACTIVE, savedPatient.getUser().getStatus());
        }
        
        @Test
        @DisplayName("Should throw exception when patient not found")
        void shouldThrowExceptionWhenPatientNotFound() {
            // Arrange
            String reason = "Patient requested archival";
            when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> managementService.archivePatient(patientId, reason));

            assertTrue(exception.getMessage().contains("Patient not found"));
        }
    }

    @Nested
    @DisplayName("Delete Patient Tests")
    class DeletePatientTests {

        @Test
        @DisplayName("Should delete patient successfully")
        void shouldDeletePatientSuccessfully() {
            // Arrange
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));

            // Act
            managementService.deletePatient(patientId);

            // Assert
            verify(patientRepository).delete(testPatient);
        }
        
        @Test
        @DisplayName("Should throw exception when patient not found")
        void shouldThrowExceptionWhenPatientNotFound() {
            // Arrange
            when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> managementService.deletePatient(patientId));

            assertTrue(exception.getMessage().contains("Patient not found"));
        }
    }

    @Nested
    @DisplayName("Merge Patient Records Tests")
    class MergePatientRecordsTests {

        private Patient sourcePatient;

        @BeforeEach
        void setUp() {
            // Setup source patient
            sourcePatient = new Patient();
            sourcePatient.setId(2L);
            sourcePatient.setFirstName("Jane");
            sourcePatient.setLastName("Smith");
            sourcePatient.setEmergencyContactName("Emergency Contact");
            sourcePatient.setEmergencyContactPhone("1112223333");
            sourcePatient.setInsuranceProvider("Another Insurance");
            sourcePatient.setInsurancePolicyNumber("POL-ABCDEF");
            sourcePatient.setBloodType("B+");
            
            User sourceUser = new User();
            sourceUser.setId(2L);
            sourceUser.setUsername("janesmith");
            sourceUser.setStatus(User.UserStatus.ACTIVE);
            
            sourcePatient.setUser(sourceUser);
        }

        @Test
        @DisplayName("Should merge patient records successfully with missing data in target")
        void shouldMergePatientRecordsSuccessfullyWithMissingDataInTarget() {
            // Arrange
            testPatient.setEmergencyContactName(null);
            testPatient.setEmergencyContactPhone(null);
            testPatient.setInsuranceProvider(null);
            testPatient.setInsurancePolicyNumber(null);
            testPatient.setBloodType(null);
            
            String reason = "Duplicate records";
            
            // Mock the findById call inside archivePatient method
            when(patientRepository.findById(sourcePatient.getId())).thenReturn(Optional.of(sourcePatient));

            // Act
            managementService.mergePatientRecords(testPatient, sourcePatient, reason);

            // Assert
            verify(patientRepository).save(testPatient);
            
            assertEquals(sourcePatient.getEmergencyContactName(), testPatient.getEmergencyContactName());
            assertEquals(sourcePatient.getEmergencyContactPhone(), testPatient.getEmergencyContactPhone());
            assertEquals(sourcePatient.getInsuranceProvider(), testPatient.getInsuranceProvider());
            assertEquals(sourcePatient.getInsurancePolicyNumber(), testPatient.getInsurancePolicyNumber());
            assertEquals(sourcePatient.getBloodType(), testPatient.getBloodType());
        }
        
        @Test
        @DisplayName("Should not override existing data in target patient")
        void shouldNotOverrideExistingDataInTargetPatient() {
            // Arrange
            String reason = "Duplicate records";
            
            // Mock the findById call inside archivePatient method
            when(patientRepository.findById(sourcePatient.getId())).thenReturn(Optional.of(sourcePatient));

            // Act
            managementService.mergePatientRecords(testPatient, sourcePatient, reason);

            // Assert
            verify(patientRepository).save(testPatient);
            
            // Verify that existing data in target patient was not overridden
            assertEquals("John", testPatient.getFirstName());
            assertEquals("Doe", testPatient.getLastName());
            assertEquals("Jane Doe", testPatient.getEmergencyContactName());
            assertEquals("9876543210", testPatient.getEmergencyContactPhone());
            assertEquals("Test Insurance", testPatient.getInsuranceProvider());
            assertEquals("POL-123456", testPatient.getInsurancePolicyNumber());
        }
        
        @Test
        @DisplayName("Should archive source patient after merging")
        void shouldArchiveSourcePatientAfterMerging() {
            // Arrange
            String reason = "Duplicate records";
            
            // Mock the findById call inside archivePatient method
            when(patientRepository.findById(sourcePatient.getId())).thenReturn(Optional.of(sourcePatient));
            
            // Act
            managementService.mergePatientRecords(testPatient, sourcePatient, reason);
            
            // Assert
            verify(patientRepository).save(sourcePatient);
            assertEquals(User.UserStatus.INACTIVE, sourcePatient.getUser().getStatus());
        }
    }
}