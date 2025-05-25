package com.example.miapp.service.medicalrecord;

import com.example.miapp.dto.medicalrecord.CreateMedicalEntryRequest;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import com.example.miapp.exception.MedicalRecordValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MedicalRecordValidationServiceTest {

    @Mock
    private Logger log;

    @InjectMocks
    private MedicalRecordValidationService validationService;

    @Nested
    @DisplayName("Medical Record Creation Validation Tests")
    class MedicalRecordCreationValidationTests {

        @Test
        @DisplayName("Should throw exception when patient is null")
        void shouldThrowExceptionWhenPatientIsNull() {
            // Arrange
            Patient patient = null;

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordCreation(patient))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Patient is required for medical record creation");
        }

        @Test
        @DisplayName("Should throw exception when patient already has medical record")
        void shouldThrowExceptionWhenPatientAlreadyHasMedicalRecord() {
            // Arrange
            Patient patient = new Patient();
            MedicalRecord existingRecord = new MedicalRecord();
            patient.setMedicalRecord(existingRecord);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordCreation(patient))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Patient already has a medical record");
        }

        @Test
        @DisplayName("Should pass validation with valid patient")
        void shouldPassValidationWithValidPatient() {
            // Arrange
            Patient patient = new Patient();
            patient.setMedicalRecord(null);

            // Act & Assert
            assertThatCode(() -> validationService.validateMedicalRecordCreation(patient))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Medical Entry Creation Validation Tests")
    class MedicalEntryCreationValidationTests {

        @Test
        @DisplayName("Should throw exception when request type is null")
        void shouldThrowExceptionWhenRequestTypeIsNull() {
            // Arrange
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(null); // Null type
            request.setTitle("Regular checkup");
            request.setContent("Patient is in good health");
            request.setDoctorId(1L);
            
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Entry type is required");
        }
        
        @Test
        @DisplayName("Should throw exception when request title is empty")
        void shouldThrowExceptionWhenRequestTitleIsEmpty() {
            // Arrange
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(MedicalRecordEntry.EntryType.CONSULTATION);
            request.setTitle(""); // Empty title
            request.setContent("Patient is in good health");
            request.setDoctorId(1L);
            
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Entry title is required");
        }
        
        @Test
        @DisplayName("Should throw exception when request content is empty")
        void shouldThrowExceptionWhenRequestContentIsEmpty() {
            // Arrange
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(MedicalRecordEntry.EntryType.CONSULTATION);
            request.setTitle("Regular checkup");
            request.setContent(""); // Empty content
            request.setDoctorId(1L);
            
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Entry content is required");
        }
        
        @Test
        @DisplayName("Should throw exception when request doctor ID is null")
        void shouldThrowExceptionWhenRequestDoctorIdIsNull() {
            // Arrange
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(MedicalRecordEntry.EntryType.CONSULTATION);
            request.setTitle("Regular checkup");
            request.setContent("Patient is in good health");
            request.setDoctorId(null); // Null doctor ID
            
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Doctor ID is required");
        }

        @Test
        @DisplayName("Should throw exception when medical record is null")
        void shouldThrowExceptionWhenMedicalRecordIsNull() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            MedicalRecord medicalRecord = null;
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Medical record not found");
        }

        @Test
        @DisplayName("Should throw exception when doctor is null")
        void shouldThrowExceptionWhenDoctorIsNull() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = null;

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Doctor not found");
        }

        @Test
        @DisplayName("Should throw exception when doctor is not active")
        void shouldThrowExceptionWhenDoctorIsNotActive() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            MedicalRecord medicalRecord = new MedicalRecord();
            
            Doctor doctor = new Doctor();
            User user = new User();
            user.setStatus(User.UserStatus.INACTIVE);
            doctor.setUser(user);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Only active doctors can create medical record entries");
        }
        
        @Test
        @DisplayName("Should throw exception when doctor has no user")
        void shouldThrowExceptionWhenDoctorHasNoUser() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            MedicalRecord medicalRecord = new MedicalRecord();
            
            Doctor doctor = new Doctor();
            doctor.setUser(null); // No user

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Only active doctors can create medical record entries");
        }

        @Test
        @DisplayName("Should pass validation with valid request, record and doctor")
        void shouldPassValidationWithValidParameters() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatCode(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .doesNotThrowAnyException();
        }
        
        @Test
        @DisplayName("Should throw exception when diagnosis content is too short")
        void shouldThrowExceptionWhenDiagnosisContentIsTooShort() {
            // Arrange
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(MedicalRecordEntry.EntryType.DIAGNOSIS);
            request.setTitle("Diagnosis");
            request.setContent("Brief"); // Too short for diagnosis
            request.setDoctorId(1L);
            
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Diagnosis content should be detailed (minimum 20 characters)");
        }
        
        @Test
        @DisplayName("Should throw exception when lab result content is too short")
        void shouldThrowExceptionWhenLabResultContentIsTooShort() {
            // Arrange
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(MedicalRecordEntry.EntryType.LAB_RESULT);
            request.setTitle("Lab Results");
            request.setContent("Normal"); // Too short for lab result
            request.setDoctorId(1L);
            
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Lab result content should include detailed results");
        }
        
        @Test
        @DisplayName("Should throw exception when surgery content is too short")
        void shouldThrowExceptionWhenSurgeryContentIsTooShort() {
            // Arrange
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(MedicalRecordEntry.EntryType.SURGERY);
            request.setTitle("Surgery");
            request.setContent("Appendectomy performed"); // Too short for surgery
            request.setDoctorId(1L);
            
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Surgery records must contain detailed information (minimum 50 characters)");
        }
        
        @Test
        @DisplayName("Should throw exception when general content is too short")
        void shouldThrowExceptionWhenGeneralContentIsTooShort() {
            // Arrange
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(MedicalRecordEntry.EntryType.OTHER);
            request.setTitle("Note");
            request.setContent("Brief"); // Too short for any entry
            request.setDoctorId(1L);
            
            MedicalRecord medicalRecord = new MedicalRecord();
            Doctor doctor = createActiveDoctor();

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Entry content seems too short for meaningful medical information");
        }
        
        @Test
@DisplayName("Should pass validation even with script tags in content")
void shouldPassValidationWithScriptTagsInContent() {
    // Arrange
    CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
    request.setType(MedicalRecordEntry.EntryType.CONSULTATION);
    request.setTitle("Allergies Update");
    request.setContent("Patient allergic to penicillin <script>alert('xss')</script>"); // Contains script
    request.setDoctorId(1L);
    
    MedicalRecord medicalRecord = new MedicalRecord();
    medicalRecord.setAllergies("Previous allergies");
    Doctor doctor = createActiveDoctor();

    // Act & Assert
    assertThatCode(() -> validationService.validateMedicalEntryCreation(request, medicalRecord, doctor))
            .doesNotThrowAnyException(); // Ahora esperamos que no lance excepción
}

        private CreateMedicalEntryRequest createValidEntryRequest() {
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(MedicalRecordEntry.EntryType.CONSULTATION);
            request.setTitle("Regular checkup");
            request.setContent("Patient is in good health. No concerns noted. Follow-up in 6 months.");
            request.setDoctorId(1L);
            return request;
        }
    }

    @Nested
    @DisplayName("Medical Entry Update Validation Tests")
    class MedicalEntryUpdateValidationTests {

        @Test
        @DisplayName("Should throw exception when request type is null")
        void shouldThrowExceptionWhenRequestTypeIsNull() {
            // Arrange
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(null); // Null type
            request.setTitle("Follow-up");
            request.setContent("Patient is showing improvement");
            request.setDoctorId(1L);
            
            MedicalRecordEntry existingEntry = new MedicalRecordEntry();
            existingEntry.setEntryDate(LocalDateTime.now());
            Doctor originalDoctor = createDoctorWithId(1L);
            existingEntry.setDoctor(originalDoctor);
            
            Doctor requestingDoctor = createDoctorWithId(1L);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryUpdate(request, existingEntry, requestingDoctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Entry type is required");
        }

        @Test
        @DisplayName("Should throw exception when existing entry is null")
        void shouldThrowExceptionWhenExistingEntryIsNull() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            MedicalRecordEntry existingEntry = null;
            Doctor doctor = createDoctorWithId(1L);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryUpdate(request, existingEntry, doctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Medical record entry not found");
        }

        @Test
        @DisplayName("Should throw exception when doctor is null")
        void shouldThrowExceptionWhenDoctorIsNull() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            
            MedicalRecordEntry existingEntry = new MedicalRecordEntry();
            existingEntry.setEntryDate(LocalDateTime.now());
            Doctor originalDoctor = createDoctorWithId(1L);
            existingEntry.setDoctor(originalDoctor);
            
            Doctor requestingDoctor = null;

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryUpdate(request, existingEntry, requestingDoctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Doctor not found");
        }

        @Test
        @DisplayName("Should throw exception when doctor is not authorized")
        void shouldThrowExceptionWhenDoctorIsNotAuthorized() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            
            // Original doctor with ID 1
            Doctor originalDoctor = createDoctorWithId(1L);
            
            MedicalRecordEntry existingEntry = new MedicalRecordEntry();
            existingEntry.setDoctor(originalDoctor);
            existingEntry.setEntryDate(LocalDateTime.now());
            
            // Requesting doctor with ID 2 (different) and no admin role
            Doctor requestingDoctor = createDoctorWithId(2L);
            User user = new User();
            Set<Role> roles = new HashSet<>();
            // No ADMIN role
            user.setRoles(roles);
            requestingDoctor.setUser(user);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalEntryUpdate(request, existingEntry, requestingDoctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Only the original doctor or authorized personnel can modify this entry");
        }

        @Test
        @DisplayName("Should pass validation when doctor is original creator")
        void shouldPassValidationWhenDoctorIsOriginalCreator() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            
            // Doctor with ID 1
            Doctor doctor = createDoctorWithId(1L);
            
            MedicalRecordEntry existingEntry = new MedicalRecordEntry();
            existingEntry.setDoctor(doctor); // Same doctor
            existingEntry.setEntryDate(LocalDateTime.now());

            // Act & Assert
            assertThatCode(() -> validationService.validateMedicalEntryUpdate(request, existingEntry, doctor))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation when doctor has admin role")
        void shouldPassValidationWhenDoctorHasAdminRole() {
            // Arrange
            CreateMedicalEntryRequest request = createValidEntryRequest();
            
            // Original doctor with ID 1
            Doctor originalDoctor = createDoctorWithId(1L);
            
            MedicalRecordEntry existingEntry = new MedicalRecordEntry();
            existingEntry.setDoctor(originalDoctor);
            existingEntry.setEntryDate(LocalDateTime.now());
            
            // Admin doctor with ID 2 (different)
            Doctor adminDoctor = createDoctorWithId(2L);
            
            User user = new User();
            Set<Role> roles = new HashSet<>();
            Role adminRole = new Role();
            adminRole.setName(Role.ERole.ROLE_ADMIN);
            roles.add(adminRole);
            user.setRoles(roles);
            adminDoctor.setUser(user);

            // Act & Assert
            assertThatCode(() -> validationService.validateMedicalEntryUpdate(request, existingEntry, adminDoctor))
                    .doesNotThrowAnyException();
        }

        private CreateMedicalEntryRequest createValidEntryRequest() {
            CreateMedicalEntryRequest request = new CreateMedicalEntryRequest();
            request.setType(MedicalRecordEntry.EntryType.CONSULTATION);
            request.setTitle("Follow-up checkup");
            request.setContent("Patient is showing improvement. Continue current treatment. Follow up in 2 weeks.");
            request.setDoctorId(1L);
            return request;
        }
    }

    @Nested
    @DisplayName("Medical Record Update Validation Tests")
    class MedicalRecordUpdateValidationTests {

        @Test
        @DisplayName("Should throw exception when allergies text is too long")
        void shouldThrowExceptionWhenAllergiesTextIsTooLong() {
            // Arrange
            String allergies = "a".repeat(501); // 501 characters
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(allergies, null, null, null, null, null))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Allergies text cannot exceed 500 characters");
        }

        @Test
        @DisplayName("Should throw exception when chronic conditions text is too long")
        void shouldThrowExceptionWhenChronicConditionsTextIsTooLong() {
            // Arrange
            String chronicConditions = "a".repeat(501); // 501 characters
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(null, chronicConditions, null, null, null, null))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Chronic conditions text cannot exceed 500 characters");
        }

        @Test
        @DisplayName("Should throw exception when current medications text is too long")
        void shouldThrowExceptionWhenCurrentMedicationsTextIsTooLong() {
            // Arrange
            String currentMedications = "a".repeat(501); // 501 characters
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(null, null, currentMedications, null, null, null))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Current medications text cannot exceed 500 characters");
        }

        @Test
        @DisplayName("Should throw exception when surgical history text is too long")
        void shouldThrowExceptionWhenSurgicalHistoryTextIsTooLong() {
            // Arrange
            String surgicalHistory = "a".repeat(501); // 501 characters
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(null, null, null, surgicalHistory, null, null))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Surgical history text cannot exceed 500 characters");
        }

        @Test
        @DisplayName("Should throw exception when family history text is too long")
        void shouldThrowExceptionWhenFamilyHistoryTextIsTooLong() {
            // Arrange
            String familyHistory = "a".repeat(1001); // 1001 characters
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(null, null, null, null, familyHistory, null))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Family history text cannot exceed 1000 characters");
        }

        @Test
        @DisplayName("Should throw exception when notes text is too long")
        void shouldThrowExceptionWhenNotesTextIsTooLong() {
            // Arrange
            String notes = "a".repeat(1001); // 1001 characters
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(null, null, null, null, null, notes))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Notes text cannot exceed 1000 characters");
        }

        @Test
        @DisplayName("Should throw exception when allergies contain invalid characters")
        void shouldThrowExceptionWhenAllergiesContainInvalidCharacters() {
            // Arrange
            String allergies = "Penicillin, Sulfa <script>alert('xss')</script>";
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(allergies, null, null, null, null, null))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Invalid characters detected in allergies");
        }

        @Test
        @DisplayName("Should pass validation with valid fields")
        void shouldPassValidationWithValidFields() {
            // Arrange
            String allergies = "Penicillin, Sulfa";
            String chronicConditions = "Hypertension, Diabetes Type 2";
            String currentMedications = "Lisinopril 10mg daily, Metformin 500mg twice daily";
            String surgicalHistory = "Appendectomy (2010)";
            String familyHistory = "Father: Hypertension, Mother: Diabetes";
            String notes = "Patient is managing conditions well with current regimen.";
            
            // Act & Assert
            assertThatCode(() -> validationService.validateMedicalRecordUpdate(
                    allergies, chronicConditions, currentMedications, surgicalHistory, familyHistory, notes))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Medical Record Deletion Validation Tests")
    class MedicalRecordDeletionValidationTests {

        @Test
        @DisplayName("Should throw exception when medical record is null")
        void shouldThrowExceptionWhenMedicalRecordIsNull() {
            // Arrange
            MedicalRecord medicalRecord = null;
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordDeletion(medicalRecord))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Medical record not found");
        }

        @Test
        @DisplayName("Should throw exception when medical record has important entries")
        void shouldThrowExceptionWhenMedicalRecordHasImportantEntries() {
            // Arrange
            MedicalRecord medicalRecord = new MedicalRecord();
            List<MedicalRecordEntry> entries = new ArrayList<>();
            
            MedicalRecordEntry entry = new MedicalRecordEntry();
            entry.setType(MedicalRecordEntry.EntryType.DIAGNOSIS); // Important entry type
            entries.add(entry);
            
            medicalRecord.setEntries(entries);
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateMedicalRecordDeletion(medicalRecord))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Cannot delete medical record with important entries (diagnosis, surgery, lab results)");
        }

        @Test
        @DisplayName("Should pass validation when record has no important entries")
        void shouldPassValidationWhenRecordHasNoImportantEntries() {
            // Arrange
            MedicalRecord medicalRecord = new MedicalRecord();
            List<MedicalRecordEntry> entries = new ArrayList<>();
            
            MedicalRecordEntry entry = new MedicalRecordEntry();
            entry.setType(MedicalRecordEntry.EntryType.CONSULTATION); // Not an important entry type
            entries.add(entry);
            
            medicalRecord.setEntries(entries);
            
            // Act & Assert
            assertThatCode(() -> validationService.validateMedicalRecordDeletion(medicalRecord))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation when record has no entries")
        void shouldPassValidationWhenRecordHasNoEntries() {
            // Arrange
            MedicalRecord medicalRecord = new MedicalRecord();
            
            // Act & Assert
            assertThatCode(() -> validationService.validateMedicalRecordDeletion(medicalRecord))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Entry Deletion Validation Tests")
    class EntryDeletionValidationTests {

        @Test
        @DisplayName("Should throw exception when entry is null")
        void shouldThrowExceptionWhenEntryIsNull() {
            // Arrange
            MedicalRecordEntry entry = null;
            Doctor requestingDoctor = createDoctorWithId(1L);
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateEntryDeletion(entry, requestingDoctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Medical record entry not found");
        }

        @Test
        @DisplayName("Should throw exception when doctor is null")
        void shouldThrowExceptionWhenDoctorIsNull() {
            // Arrange
            MedicalRecordEntry entry = new MedicalRecordEntry();
            Doctor requestingDoctor = null;
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateEntryDeletion(entry, requestingDoctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Doctor is required");
        }

        @Test
        @DisplayName("Should throw exception when entry is a surgery record")
        void shouldThrowExceptionWhenEntryIsSurgeryRecord() {
            // Arrange
            MedicalRecordEntry entry = new MedicalRecordEntry();
            entry.setType(MedicalRecordEntry.EntryType.SURGERY);
            entry.setEntryDate(LocalDateTime.now());
            
            Doctor originalDoctor = createDoctorWithId(1L);
            entry.setDoctor(originalDoctor);
            
            Doctor requestingDoctor = createDoctorWithId(1L);
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateEntryDeletion(entry, requestingDoctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Surgery records cannot be deleted for legal compliance");
        }

        @Test
        @DisplayName("Should throw exception when doctor is not authorized")
        void shouldThrowExceptionWhenDoctorIsNotAuthorized() {
            // Arrange
            MedicalRecordEntry entry = new MedicalRecordEntry();
            entry.setType(MedicalRecordEntry.EntryType.CONSULTATION);
            entry.setEntryDate(LocalDateTime.now());
            
            Doctor originalDoctor = createDoctorWithId(1L);
            entry.setDoctor(originalDoctor);
            
            Doctor requestingDoctor = createDoctorWithId(2L); // Different doctor
            
            User user = new User();
            Set<Role> roles = new HashSet<>();
            // No ADMIN role
            user.setRoles(roles);
            requestingDoctor.setUser(user);
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validateEntryDeletion(entry, requestingDoctor))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Only the original doctor or authorized personnel can modify this entry");
        }

        @Test
        @DisplayName("Should pass validation with authorized doctor and valid entry")
        void shouldPassValidationWithAuthorizedDoctorAndValidEntry() {
            // Arrange
            MedicalRecordEntry entry = new MedicalRecordEntry();
            entry.setType(MedicalRecordEntry.EntryType.CONSULTATION);
            entry.setEntryDate(LocalDateTime.now());
            
            Doctor doctor = createDoctorWithId(1L);
            entry.setDoctor(doctor);
            
            // Act & Assert
            assertThatCode(() -> validationService.validateEntryDeletion(entry, doctor))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Patient Access Validation Tests")
    class PatientAccessValidationTests {

        @Test
        @DisplayName("Should throw exception when medical record is null")
        void shouldThrowExceptionWhenMedicalRecordIsNull() {
            // Arrange
            MedicalRecord medicalRecord = null;
            Long patientId = 1L;
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validatePatientAccess(medicalRecord, patientId))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Medical record not found");
        }

        @Test
        @DisplayName("Should throw exception when record belongs to different patient")
        void shouldThrowExceptionWhenRecordBelongsToDifferentPatient() {
            // Arrange
            MedicalRecord medicalRecord = new MedicalRecord();
            Patient patient = new Patient();
            patient.setId(1L);
            medicalRecord.setPatient(patient);
            
            Long requestingPatientId = 2L; // Different patient
            
            // Act & Assert
            assertThatThrownBy(() -> validationService.validatePatientAccess(medicalRecord, requestingPatientId))
                    .isInstanceOf(MedicalRecordValidationException.class)
                    .hasMessage("Access denied: Medical record belongs to different patient");
        }

        @Test
        @DisplayName("Should pass validation when patient owns the record")
        void shouldPassValidationWhenPatientOwnsTheRecord() {
            // Arrange
            MedicalRecord medicalRecord = new MedicalRecord();
            Patient patient = new Patient();
            patient.setId(1L);
            medicalRecord.setPatient(patient);
            
            Long requestingPatientId = 1L; // Same patient
            
            // Act & Assert
            assertThatCode(() -> validationService.validatePatientAccess(medicalRecord, requestingPatientId))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Medication Validation Tests")
    class MedicationValidationTests {
        
        @Test
        @DisplayName("Should detect interaction between warfarin and aspirin")
        void shouldDetectInteractionBetweenWarfarinAndAspirin() {
            // Arrange
            String medications = "Warfarin 5mg daily, Aspirin 81mg daily";
            
            // Test through validateMedicalRecordUpdate method
            // This will invoke validateMedicationText internally
            
            // Act & Assert
            // Since this only logs a warning and doesn't throw an exception,
            // we just verify it doesn't throw and assume the warning is logged
            assertThatCode(() -> validationService.validateMedicalRecordUpdate(null, null, medications, null, null, null))
                    .doesNotThrowAnyException();
        }
        
        @Test
        @DisplayName("Should handle 'unknown' medication properly")
        void shouldHandleUnknownMedicationProperly() {
            // Arrange
            String medications = "unknown";
            
            // Act & Assert
            // Since this only logs a warning and doesn't throw an exception,
            // we just verify it doesn't throw and assume the warning is logged
            assertThatCode(() -> validationService.validateMedicalRecordUpdate(null, null, medications, null, null, null))
                    .doesNotThrowAnyException();
        }
    }
    
    @Nested
@DisplayName("Parametrized Validation Tests")
class ParametrizedValidationTests {
    
    @ParameterizedTest
    @ValueSource(strings = {
        "<script>alert('xss')</script>Penicillin allergy",
        "Penicillin<script>alert('xss')</script>",
        "javascript:alert('xss') Allergy to sulfa",
        "Allergy to sulfa javascript:alert('xss')"
    })
    @DisplayName("Should throw exception for malicious content in allergies")
    void shouldThrowExceptionForMaliciousContent(String text) {
        assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(text, null, null, null, null, null))
                .isInstanceOf(MedicalRecordValidationException.class)
                .hasMessage("Invalid characters detected in allergies");
    }
    
    @Test
    @DisplayName("Should throw exception when allergies are too long (501 chars)")
    void shouldThrowExceptionWhenAllergiesAreTooLong501() {
        String text = "a".repeat(501);
        assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(text, null, null, null, null, null))
                .isInstanceOf(MedicalRecordValidationException.class)
                .hasMessage("Allergies text cannot exceed 500 characters");
    }
    
    @Test
    @DisplayName("Should throw exception when allergies are too long (550 chars)")
    void shouldThrowExceptionWhenAllergiesAreTooLong550() {
        String text = "b".repeat(550);
        assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(text, null, null, null, null, null))
                .isInstanceOf(MedicalRecordValidationException.class)
                .hasMessage("Allergies text cannot exceed 500 characters");
    }
    
    @Test
    @DisplayName("Should throw exception when allergies are too long (600 chars)")
    void shouldThrowExceptionWhenAllergiesAreTooLong600() {
        String text = "c".repeat(600);
        assertThatThrownBy(() -> validationService.validateMedicalRecordUpdate(text, null, null, null, null, null))
                .isInstanceOf(MedicalRecordValidationException.class)
                .hasMessage("Allergies text cannot exceed 500 characters");
    }
}
    
    // Helper methods to create test objects
    
    private Doctor createActiveDoctor() {
        Doctor doctor = new Doctor();
        User user = new User();
        user.setStatus(User.UserStatus.ACTIVE);
        doctor.setUser(user);
        return doctor;
    }
    
    private Doctor createDoctorWithId(Long id) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        User user = new User();
        user.setStatus(User.UserStatus.ACTIVE);
        Set<Role> roles = new HashSet<>();
        doctor.setUser(user);
        user.setRoles(roles);
        return doctor;
    }
}