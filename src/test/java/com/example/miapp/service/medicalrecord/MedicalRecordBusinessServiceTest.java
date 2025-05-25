package com.example.miapp.service.medicalrecord;

import com.example.miapp.dto.medicalrecord.CreateMedicalEntryRequest;
import com.example.miapp.dto.medicalrecord.MedicalRecordDto;
import com.example.miapp.dto.medicalrecord.MedicalRecordEntryDto;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.User;
import com.example.miapp.mapper.MedicalRecordMapper;
import com.example.miapp.repository.MedicalRecordRepository;
import com.example.miapp.service.doctor.DoctorService;
import com.example.miapp.service.patient.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordBusinessServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    
    @Mock
    private MedicalRecordMapper medicalRecordMapper;
    
    @Mock
    private DoctorService doctorService;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private MedicalRecordValidationService validationService;
    
    @Mock
    private MedicalRecordQueryService queryService;
    
    @Mock
    private MedicalRecordManagementService managementService;
    
    @InjectMocks
    private MedicalRecordBusinessService businessService;
    
    // Datos de prueba comunes
    private Patient testPatient;
    private Doctor testDoctor;
    private MedicalRecord testMedicalRecord;
    private MedicalRecordEntry testEntry;
    private MedicalRecordDto testMedicalRecordDto;
    private MedicalRecordEntryDto testEntryDto;
    private CreateMedicalEntryRequest testEntryRequest;
    private List<MedicalRecordEntry> entries;
    
    @BeforeEach
    void setUp() {
        // Inicializar datos de prueba
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        
        User doctorUser = new User();
        doctorUser.setId(1L);
        doctorUser.setUsername("drsmith");
        doctorUser.setRoles(new HashSet<>());
        
        testDoctor = new Doctor();
        testDoctor.setId(1L);
        testDoctor.setFirstName("Dr.");
        testDoctor.setLastName("Smith");
        testDoctor.setUser(doctorUser);
        
        testMedicalRecord = new MedicalRecord();
        testMedicalRecord.setId(1L);
        testMedicalRecord.setPatient(testPatient);
        testMedicalRecord.setCreatedAt(LocalDateTime.now());
        
        testEntry = new MedicalRecordEntry();
        testEntry.setId(1L);
        testEntry.setType(MedicalRecordEntry.EntryType.CONSULTATION);
        testEntry.setTitle("Initial Consultation");
        testEntry.setContent("Patient came in with...");
        testEntry.setDoctor(testDoctor);
        testEntry.setMedicalRecord(testMedicalRecord);
        testEntry.setEntryDate(LocalDateTime.now());
        
        entries = new ArrayList<>();
        entries.add(testEntry);
        testMedicalRecord.setEntries(entries);
        
        testMedicalRecordDto = new MedicalRecordDto();
        testMedicalRecordDto.setId(1L);
        testMedicalRecordDto.setPatientId(1L);
        testMedicalRecordDto.setPatientName("John Doe");
        
        testEntryDto = new MedicalRecordEntryDto();
        testEntryDto.setId(1L);
        testEntryDto.setType(MedicalRecordEntry.EntryType.CONSULTATION);
        testEntryDto.setTitle("Initial Consultation");
        testEntryDto.setContent("Patient came in with...");
        testEntryDto.setDoctorName("Dr. Smith");
        
        testEntryRequest = new CreateMedicalEntryRequest();
        testEntryRequest.setType(MedicalRecordEntry.EntryType.CONSULTATION);
        testEntryRequest.setTitle("Initial Consultation");
        testEntryRequest.setContent("Patient came in with...");
        testEntryRequest.setDoctorId(1L);
    }
    
    @Nested
    class CreateMedicalRecordTests {
        
        @Test
        void shouldCreateMedicalRecordSuccessfully() {
            // Arrange
            when(patientService.findPatientById(1L)).thenReturn(testPatient);
            doNothing().when(validationService).validateMedicalRecordCreation(testPatient);
            when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(testMedicalRecord);
            when(medicalRecordMapper.toDto(testMedicalRecord)).thenReturn(testMedicalRecordDto);
            
            // Act
            MedicalRecordDto result = businessService.createMedicalRecord(1L);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(1L, result.getPatientId());
            
            // Verify
            verify(patientService).findPatientById(1L);
            verify(validationService).validateMedicalRecordCreation(testPatient);
            verify(medicalRecordRepository).save(any(MedicalRecord.class));
            verify(medicalRecordMapper).toDto(testMedicalRecord);
        }
        
        @Test
        void shouldThrowExceptionWhenPatientNotFound() {
            // Arrange
            when(patientService.findPatientById(999L)).thenThrow(new RuntimeException("Patient not found"));
            
            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                businessService.createMedicalRecord(999L);
            });
            
            assertEquals("Patient not found", exception.getMessage());
            
            // Verify
            verify(patientService).findPatientById(999L);
            verifyNoInteractions(medicalRecordRepository);
            verifyNoInteractions(medicalRecordMapper);
        }
    }
    
    @Nested
    class AddMedicalEntryTests {
        
        @Test
        void shouldAddMedicalEntrySuccessfully() {
            // Arrange
            when(medicalRecordRepository.findByPatientId(1L)).thenReturn(Optional.of(testMedicalRecord));
            when(doctorService.findDoctorById(1L)).thenReturn(testDoctor);
            doNothing().when(validationService).validateMedicalEntryCreation(testEntryRequest, testMedicalRecord, testDoctor);
            when(medicalRecordMapper.toEntryEntity(testEntryRequest)).thenReturn(testEntry);
            doNothing().when(managementService).addEntryToMedicalRecord(testMedicalRecord, testEntry);
            when(medicalRecordMapper.toEntryDto(testEntry)).thenReturn(testEntryDto);
            
            // Act
            MedicalRecordEntryDto result = businessService.addMedicalEntry(1L, testEntryRequest);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(MedicalRecordEntry.EntryType.CONSULTATION, result.getType());
            assertEquals("Initial Consultation", result.getTitle());
            
            // Verify
            verify(medicalRecordRepository).findByPatientId(1L);
            verify(doctorService).findDoctorById(1L);
            verify(validationService).validateMedicalEntryCreation(testEntryRequest, testMedicalRecord, testDoctor);
            verify(managementService).addEntryToMedicalRecord(testMedicalRecord, testEntry);
            verify(medicalRecordMapper).toEntryDto(testEntry);
        }
        
        @Test
        void shouldThrowExceptionWhenMedicalRecordNotFound() {
            // Arrange
            when(medicalRecordRepository.findByPatientId(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                businessService.addMedicalEntry(999L, testEntryRequest);
            });
            
            assertTrue(exception.getMessage().contains("Medical record not found"));
            
            // Verify
            verify(medicalRecordRepository).findByPatientId(999L);
            verifyNoInteractions(managementService);
            verifyNoInteractions(medicalRecordMapper);
        }
    }
    
    @Nested
    class UpdateMedicalEntryTests {
        
        @Test
        void shouldUpdateMedicalEntrySuccessfully() {
            // Arrange
            // Configuramos los mocks para simular el comportamiento de findMedicalEntryById
            when(medicalRecordRepository.findAll()).thenReturn(List.of(testMedicalRecord));
            
            when(doctorService.findDoctorById(1L)).thenReturn(testDoctor);
            doNothing().when(validationService).validateMedicalEntryUpdate(eq(testEntryRequest), any(MedicalRecordEntry.class), eq(testDoctor));
            when(medicalRecordMapper.toEntryDto(any(MedicalRecordEntry.class))).thenReturn(testEntryDto);
            
            // Act
            MedicalRecordEntryDto result = businessService.updateMedicalEntry(1L, testEntryRequest);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            
            // Verify
            verify(doctorService).findDoctorById(1L);
            verify(validationService).validateMedicalEntryUpdate(eq(testEntryRequest), any(MedicalRecordEntry.class), eq(testDoctor));
            verify(medicalRecordRepository).save(any(MedicalRecord.class));
            verify(medicalRecordMapper).toEntryDto(any(MedicalRecordEntry.class));
        }
        
        @Test
        void shouldThrowExceptionWhenEntryNotFound() {
            // Arrange
            // Configuramos el mock para simular que no se encuentra la entrada
            when(medicalRecordRepository.findAll()).thenReturn(new ArrayList<>());
            
            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                businessService.updateMedicalEntry(999L, testEntryRequest);
            });
            
            assertTrue(exception.getMessage().contains("Medical record entry not found"));
        }
    }
    
    @Nested
    class DeleteMedicalEntryTests {
        
        @Test
        void shouldDeleteMedicalEntrySuccessfully() {
            // Arrange
            // Configuramos los mocks para simular el comportamiento de findMedicalEntryById
            when(medicalRecordRepository.findAll()).thenReturn(List.of(testMedicalRecord));
            
            when(doctorService.findDoctorById(1L)).thenReturn(testDoctor);
            doNothing().when(validationService).validateEntryDeletion(any(MedicalRecordEntry.class), eq(testDoctor));
            doNothing().when(managementService).removeEntryFromMedicalRecord(eq(testMedicalRecord), any(MedicalRecordEntry.class));
            
            // Act
            businessService.deleteMedicalEntry(1L, 1L);
            
            // Verify
            verify(doctorService).findDoctorById(1L);
            verify(validationService).validateEntryDeletion(any(MedicalRecordEntry.class), eq(testDoctor));
            verify(managementService).removeEntryFromMedicalRecord(eq(testMedicalRecord), any(MedicalRecordEntry.class));
        }
        
        @Test
        void shouldThrowExceptionWhenEntryNotFound() {
            // Arrange
            // Configuramos el mock para simular que no se encuentra la entrada
            when(medicalRecordRepository.findAll()).thenReturn(new ArrayList<>());
            
            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                businessService.deleteMedicalEntry(999L, 1L);
            });
            
            assertTrue(exception.getMessage().contains("Medical record entry not found"));
        }
    }
    
    @Nested
    class QueryOperationTests {
        
        @Test
        void shouldGetMedicalRecord() {
            // Arrange
            when(queryService.getMedicalRecord(1L)).thenReturn(testMedicalRecordDto);
            
            // Act
            MedicalRecordDto result = businessService.getMedicalRecord(1L);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            
            // Verify
            verify(queryService).getMedicalRecord(1L);
        }
        
        @Test
        void shouldFindMedicalRecordByPatientId() {
            // Arrange
            when(queryService.findMedicalRecordByPatientId(1L)).thenReturn(Optional.of(testMedicalRecordDto));
            
            // Act
            Optional<MedicalRecordDto> result = businessService.findMedicalRecordByPatientId(1L);
            
            // Assert
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            
            // Verify
            verify(queryService).findMedicalRecordByPatientId(1L);
        }
        
        @Test
        void shouldGetPatientMedicalHistory() {
            // Arrange
            when(queryService.getPatientMedicalHistory(1L)).thenReturn(testMedicalRecordDto);
            
            // Act
            MedicalRecordDto result = businessService.getPatientMedicalHistory(1L);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            
            // Verify
            verify(queryService).getPatientMedicalHistory(1L);
        }
        
        @Test
        void shouldGetPatientVisibleMedicalHistory() {
            // Arrange
            when(queryService.findMedicalRecordByPatientId(1L)).thenReturn(Optional.of(testMedicalRecordDto));
            when(queryService.findMedicalRecordById(anyLong())).thenReturn(testMedicalRecord);
            doNothing().when(validationService).validatePatientAccess(any(MedicalRecord.class), eq(1L));
            when(queryService.getPatientVisibleMedicalHistory(1L)).thenReturn(testMedicalRecordDto);
            
            // Act
            MedicalRecordDto result = businessService.getPatientVisibleMedicalHistory(1L);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            
            // Verify
            verify(queryService).findMedicalRecordByPatientId(1L);
            verify(validationService).validatePatientAccess(any(MedicalRecord.class), eq(1L));
            verify(queryService).getPatientVisibleMedicalHistory(1L);
        }
        
        @Test
        void shouldThrowExceptionWhenMedicalRecordNotFoundForPatient() {
            // Arrange
            when(queryService.findMedicalRecordByPatientId(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                businessService.getPatientVisibleMedicalHistory(999L);
            });
            
            assertTrue(exception.getMessage().contains("Medical record not found for patient"));
        }
        
        @Test
        void shouldGetRecentEntriesForPatient() {
            // Arrange
            List<MedicalRecordEntryDto> entries = List.of(testEntryDto);
            when(queryService.getRecentEntriesForPatient(1L, 5)).thenReturn(entries);
            
            // Act
            List<MedicalRecordEntryDto> result = businessService.getRecentEntriesForPatient(1L, 5);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testEntryDto, result.get(0));
            
            // Verify
            verify(queryService).getRecentEntriesForPatient(1L, 5);
        }
        
        @Test
        void shouldGetEntriesByTypeForPatient() {
            // Arrange
            List<MedicalRecordEntryDto> entries = List.of(testEntryDto);
            when(queryService.getEntriesByTypeForPatient(1L, MedicalRecordEntry.EntryType.CONSULTATION)).thenReturn(entries);
            
            // Act
            List<MedicalRecordEntryDto> result = businessService.getEntriesByTypeForPatient(1L, MedicalRecordEntry.EntryType.CONSULTATION);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testEntryDto, result.get(0));
            
            // Verify
            verify(queryService).getEntriesByTypeForPatient(1L, MedicalRecordEntry.EntryType.CONSULTATION);
        }
    }
    
    @Nested
    class ManagementOperationTests {
        
        @Test
        void shouldUpdateAllergies() {
            // Arrange
            String allergies = "Penicillin, Peanuts";
            doNothing().when(validationService).validateMedicalRecordUpdate(eq(allergies), isNull(), isNull(), isNull(), isNull(), isNull());
            doNothing().when(managementService).updateAllergies(1L, allergies);
            
            // Act
            businessService.updateAllergies(1L, allergies);
            
            // Verify
            verify(validationService).validateMedicalRecordUpdate(eq(allergies), isNull(), isNull(), isNull(), isNull(), isNull());
            verify(managementService).updateAllergies(1L, allergies);
        }
        
        @Test
        void shouldUpdateChronicConditions() {
            // Arrange
            String conditions = "Hypertension, Diabetes";
            doNothing().when(validationService).validateMedicalRecordUpdate(isNull(), eq(conditions), isNull(), isNull(), isNull(), isNull());
            doNothing().when(managementService).updateChronicConditions(1L, conditions);
            
            // Act
            businessService.updateChronicConditions(1L, conditions);
            
            // Verify
            verify(validationService).validateMedicalRecordUpdate(isNull(), eq(conditions), isNull(), isNull(), isNull(), isNull());
            verify(managementService).updateChronicConditions(1L, conditions);
        }
        
        @Test
        void shouldUpdateCurrentMedications() {
            // Arrange
            String medications = "Aspirin, Insulin";
            doNothing().when(validationService).validateMedicalRecordUpdate(isNull(), isNull(), eq(medications), isNull(), isNull(), isNull());
            doNothing().when(managementService).updateCurrentMedications(1L, medications);
            
            // Act
            businessService.updateCurrentMedications(1L, medications);
            
            // Verify
            verify(validationService).validateMedicalRecordUpdate(isNull(), isNull(), eq(medications), isNull(), isNull(), isNull());
            verify(managementService).updateCurrentMedications(1L, medications);
        }
        
        @Test
        void shouldUpdateMedicalRecordFields() {
            // Arrange
            String allergies = "Penicillin";
            String chronicConditions = "Hypertension";
            String currentMedications = "Aspirin";
            String surgicalHistory = "Appendectomy 2020";
            String familyHistory = "Father: Diabetes";
            String notes = "Regular checkups";
            
            doNothing().when(validationService).validateMedicalRecordUpdate(allergies, chronicConditions, currentMedications, surgicalHistory, familyHistory, notes);
            doNothing().when(managementService).updateMedicalRecordFields(1L, allergies, chronicConditions, currentMedications, surgicalHistory, familyHistory, notes);
            
            // Act
            businessService.updateMedicalRecordFields(1L, allergies, chronicConditions, currentMedications, surgicalHistory, familyHistory, notes);
            
            // Verify
            verify(validationService).validateMedicalRecordUpdate(allergies, chronicConditions, currentMedications, surgicalHistory, familyHistory, notes);
            verify(managementService).updateMedicalRecordFields(1L, allergies, chronicConditions, currentMedications, surgicalHistory, familyHistory, notes);
        }
        
        @Test
void shouldUpdateEntryVisibility() {
    // Arrange
    // Configuramos los mocks para simular findMedicalEntryById
    when(medicalRecordRepository.findAll()).thenReturn(List.of(testMedicalRecord));
    
    // Act
    businessService.updateEntryVisibility(1L, true);
    
    // Verify
    // Capturamos el argumento para verificar que es la entrada esperada
    ArgumentCaptor<MedicalRecordEntry> entryCaptor = ArgumentCaptor.forClass(MedicalRecordEntry.class);
    verify(managementService).updateEntryVisibility(entryCaptor.capture(), eq(true));
    
    // Verificamos que la entrada capturada tiene el ID esperado
    assertEquals(1L, entryCaptor.getValue().getId());
}
        
        @Test
        void shouldMergeMedicalRecords() {
            // Arrange
            MedicalRecord primaryRecord = new MedicalRecord();
            primaryRecord.setId(1L);
            
            MedicalRecord secondaryRecord = new MedicalRecord();
            secondaryRecord.setId(2L);
            
            String reason = "Duplicate records for same patient";
            
            when(queryService.findMedicalRecordById(1L)).thenReturn(primaryRecord);
            when(queryService.findMedicalRecordById(2L)).thenReturn(secondaryRecord);
            
            // Act
            businessService.mergeMedicalRecords(1L, 2L, reason);
            
            // Verify
            verify(queryService).findMedicalRecordById(1L);
            verify(queryService).findMedicalRecordById(2L);
            verify(managementService).mergeMedicalRecords(primaryRecord, secondaryRecord, reason);
        }
        
        @Test
        void shouldNotMergeSameMedicalRecord() {
            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                businessService.mergeMedicalRecords(1L, 1L, "Invalid merge");
            });
            
            assertEquals("Cannot merge medical record with itself", exception.getMessage());
            
            // Verify
            verifyNoInteractions(managementService);
        }
        
        @Test
        void shouldDeleteMedicalRecord() {
            // Arrange
            when(queryService.findMedicalRecordById(1L)).thenReturn(testMedicalRecord);
            doNothing().when(validationService).validateMedicalRecordDeletion(testMedicalRecord);
            doNothing().when(managementService).deleteMedicalRecord(1L);
            
            // Act
            businessService.deleteMedicalRecord(1L);
            
            // Verify
            verify(queryService).findMedicalRecordById(1L);
            verify(validationService).validateMedicalRecordDeletion(testMedicalRecord);
            verify(managementService).deleteMedicalRecord(1L);
        }
    }
    
    @Nested
    class MedicalInformationQueryTests {
        
        @Test
        void shouldCheckForAllergy() {
            // Arrange
            when(queryService.hasAllergy(1L, "Penicillin")).thenReturn(true);
            when(queryService.hasAllergy(1L, "Sulfa")).thenReturn(false);
            
            // Act & Assert
            assertTrue(businessService.hasAllergy(1L, "Penicillin"));
            assertFalse(businessService.hasAllergy(1L, "Sulfa"));
            
            // Verify
            verify(queryService).hasAllergy(1L, "Penicillin");
            verify(queryService).hasAllergy(1L, "Sulfa");
        }
        
        @Test
        void shouldCheckForChronicCondition() {
            // Arrange
            when(queryService.hasChronicCondition(1L, "Diabetes")).thenReturn(true);
            when(queryService.hasChronicCondition(1L, "Asthma")).thenReturn(false);
            
            // Act & Assert
            assertTrue(businessService.hasChronicCondition(1L, "Diabetes"));
            assertFalse(businessService.hasChronicCondition(1L, "Asthma"));
            
            // Verify
            verify(queryService).hasChronicCondition(1L, "Diabetes");
            verify(queryService).hasChronicCondition(1L, "Asthma");
        }
    }
    
    @Nested
    class SearchOperationTests {
        
        @Test
        void shouldSearchMedicalRecords() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<MedicalRecordDto> records = List.of(testMedicalRecordDto);
            Page<MedicalRecordDto> page = new PageImpl<>(records, pageable, records.size());
            
            when(queryService.searchMedicalRecords("Penicillin", "Diabetes", "Aspirin", MedicalRecordEntry.EntryType.CONSULTATION, "fever", pageable))
                .thenReturn(page);
            
            // Act
            Page<MedicalRecordDto> result = businessService.searchMedicalRecords(
                "Penicillin", "Diabetes", "Aspirin", MedicalRecordEntry.EntryType.CONSULTATION, "fever", pageable);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(testMedicalRecordDto, result.getContent().get(0));
            
            // Verify
            verify(queryService).searchMedicalRecords("Penicillin", "Diabetes", "Aspirin", MedicalRecordEntry.EntryType.CONSULTATION, "fever", pageable);
        }
        
        @Test
        void shouldFindByAllergiesContaining() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<MedicalRecordDto> records = List.of(testMedicalRecordDto);
            Page<MedicalRecordDto> page = new PageImpl<>(records, pageable, records.size());
            
            when(queryService.findByAllergiesContaining("Penicillin", pageable)).thenReturn(page);
            
            // Act
            Page<MedicalRecordDto> result = businessService.findByAllergiesContaining("Penicillin", pageable);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            
            // Verify
            verify(queryService).findByAllergiesContaining("Penicillin", pageable);
        }
        
        @Test
        void shouldFindByChronicConditionsContaining() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<MedicalRecordDto> records = List.of(testMedicalRecordDto);
            Page<MedicalRecordDto> page = new PageImpl<>(records, pageable, records.size());
            
            when(queryService.findByChronicConditionsContaining("Diabetes", pageable)).thenReturn(page);
            
            // Act
            Page<MedicalRecordDto> result = businessService.findByChronicConditionsContaining("Diabetes", pageable);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            
            // Verify
            verify(queryService).findByChronicConditionsContaining("Diabetes", pageable);
        }
        
        @Test
        void shouldFindByCurrentMedicationsContaining() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<MedicalRecordDto> records = List.of(testMedicalRecordDto);
            Page<MedicalRecordDto> page = new PageImpl<>(records, pageable, records.size());
            
            when(queryService.findByCurrentMedicationsContaining("Aspirin", pageable)).thenReturn(page);
            
            // Act
            Page<MedicalRecordDto> result = businessService.findByCurrentMedicationsContaining("Aspirin", pageable);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            
            // Verify
            verify(queryService).findByCurrentMedicationsContaining("Aspirin", pageable);
        }
    }
    
    @Test
void shouldGetMedicalRecordEntryStats() {
    // Arrange
    // Creamos una lista de cuatro conjuntos de estadísticas
    List<Object[]> stats = new ArrayList<>();
    stats.add(new Object[]{1L, "John", "Doe", 5L});
    stats.add(new Object[]{2L, "Jane", "Smith", 3L});
    stats.add(new Object[]{3L, "Bob", "Johnson", 7L});
    stats.add(new Object[]{4L, "Alice", "Williams", 2L});
    
    when(queryService.getMedicalRecordEntryStats()).thenReturn(stats);
    
    // Act
    List<Object[]> result = businessService.getMedicalRecordEntryStats();
    
    // Assert
    assertNotNull(result);
    // Ajustamos la expectativa a 4 elementos
    assertEquals(4, result.size());
    // Verificamos el contenido del primer elemento
    assertEquals(1L, result.get(0)[0]);
    assertEquals("John", result.get(0)[1]);
    assertEquals("Doe", result.get(0)[2]);
    assertEquals(5L, result.get(0)[3]);
    
    // Verify
    verify(queryService).getMedicalRecordEntryStats();
}
}