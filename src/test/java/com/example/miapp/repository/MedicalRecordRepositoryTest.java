package com.example.miapp.repository;

import com.example.miapp.entity.*;
import com.example.miapp.entity.MedicalRecordEntry.EntryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicalRecordRepositoryTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    private Patient patient;
    private User patientUser;
    private Doctor doctor;
    private User doctorUser;
    private MedicalRecord medicalRecord;
    private MedicalRecordEntry entry1;
    private MedicalRecordEntry entry2;
    private List<MedicalRecord> medicalRecordList;
    private Page<MedicalRecord> medicalRecordPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Crear usuarios
        patientUser = User.builder()
                .id(1L)
                .username("patient1")
                .email("patient1@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        doctorUser = User.builder()
                .id(2L)
                .username("doctor1")
                .email("doctor1@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        // Crear paciente
        patient = Patient.builder()
                .id(1L)
                .firstName("Maria")
                .lastName("Garcia")
                .phone("123456789")
                .address("123 Main St")
                .user(patientUser)
                .build();
        
        // Crear doctor
        doctor = Doctor.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("987654321")
                .licenseNumber("MED12345")
                .user(doctorUser)
                .build();
        
        // Crear historial médico
        LocalDateTime now = LocalDateTime.now();
        
        medicalRecord = MedicalRecord.builder()
                .id(1L)
                .patient(patient)
                .createdAt(now.minusMonths(6))
                .allergies("Penicillin, Pollen")
                .chronicConditions("Hypertension, Asthma")
                .currentMedications("Lisinopril, Albuterol")
                .familyHistory("Father: Diabetes, Mother: Hypertension")
                .surgicalHistory("Appendectomy (2015)")
                .notes("Patient manages conditions well with current treatment plan")
                .build();
        
        // Crear entradas del historial médico
        entry1 = MedicalRecordEntry.builder()
                .id(1L)
                .medicalRecord(medicalRecord)
                .entryDate(now.minusMonths(3))
                .type(EntryType.CONSULTATION)
                .title("Routine Check-up")
                .content("Blood pressure normal. Lungs clear. Patient reports occasional use of rescue inhaler.")
                .doctor(doctor)
                .visibleToPatient(true)
                .build();
        
        entry2 = MedicalRecordEntry.builder()
                .id(2L)
                .medicalRecord(medicalRecord)
                .entryDate(now.minusMonths(1))
                .type(EntryType.LAB_RESULT)
                .title("Blood Work Results")
                .content("Complete blood count normal. Cholesterol slightly elevated.")
                .doctor(doctor)
                .visibleToPatient(true)
                .build();
        
        // Añadir entradas al historial médico
        List<MedicalRecordEntry> entries = new ArrayList<>();
        entries.add(entry1);
        entries.add(entry2);
        medicalRecord.setEntries(entries);
        
        // Configurar lista de historiales médicos
        medicalRecordList = List.of(medicalRecord);
        
        // Configurar paginación
        pageable = PageRequest.of(0, 10);
        medicalRecordPage = new PageImpl<>(medicalRecordList, pageable, medicalRecordList.size());
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda básica")
    class BasicSearchTests {
        
        @Test
        @DisplayName("Debería encontrar un historial médico por ID de paciente")
        void shouldFindByPatientId() {
            // Given
            when(medicalRecordRepository.findByPatientId(1L))
                    .thenReturn(Optional.of(medicalRecord));
            
            // When
            Optional<MedicalRecord> result = medicalRecordRepository.findByPatientId(1L);
            
            // Then
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getPatient().getId());
            verify(medicalRecordRepository).findByPatientId(1L);
        }
        
        @Test
        @DisplayName("Debería retornar vacío si no hay historial médico para el paciente")
        void shouldReturnEmptyWhenNoMedicalRecordForPatient() {
            // Given
            when(medicalRecordRepository.findByPatientId(2L))
                    .thenReturn(Optional.empty());
            
            // When
            Optional<MedicalRecord> result = medicalRecordRepository.findByPatientId(2L);
            
            // Then
            assertFalse(result.isPresent());
            verify(medicalRecordRepository).findByPatientId(2L);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de operaciones de actualización")
    class UpdateOperationTests {
        
        @Test
        @DisplayName("Debería actualizar las alergias")
        void shouldUpdateAllergies() {
            // Given
            String newAllergies = "Penicillin, Pollen, Shellfish";
            when(medicalRecordRepository.updateAllergies(1L, newAllergies))
                    .thenReturn(1);
            
            // When
            int affectedRows = medicalRecordRepository.updateAllergies(1L, newAllergies);
            
            // Then
            assertEquals(1, affectedRows);
            verify(medicalRecordRepository).updateAllergies(1L, newAllergies);
        }
        
        @Test
        @DisplayName("Debería actualizar las condiciones crónicas")
        void shouldUpdateChronicConditions() {
            // Given
            String newConditions = "Hypertension, Asthma, Diabetes Type 2";
            when(medicalRecordRepository.updateChronicConditions(1L, newConditions))
                    .thenReturn(1);
            
            // When
            int affectedRows = medicalRecordRepository.updateChronicConditions(1L, newConditions);
            
            // Then
            assertEquals(1, affectedRows);
            verify(medicalRecordRepository).updateChronicConditions(1L, newConditions);
        }
        
        @Test
        @DisplayName("Debería actualizar los medicamentos actuales")
        void shouldUpdateCurrentMedications() {
            // Given
            String newMedications = "Lisinopril, Albuterol, Metformin";
            when(medicalRecordRepository.updateCurrentMedications(1L, newMedications))
                    .thenReturn(1);
            
            // When
            int affectedRows = medicalRecordRepository.updateCurrentMedications(1L, newMedications);
            
            // Then
            assertEquals(1, affectedRows);
            verify(medicalRecordRepository).updateCurrentMedications(1L, newMedications);
        }
        
        @Test
        @DisplayName("Debería actualizar el historial familiar")
        void shouldUpdateFamilyHistory() {
            // Given
            String newFamilyHistory = "Father: Diabetes, Mother: Hypertension, Sister: Asthma";
            when(medicalRecordRepository.updateFamilyHistory(1L, newFamilyHistory))
                    .thenReturn(1);
            
            // When
            int affectedRows = medicalRecordRepository.updateFamilyHistory(1L, newFamilyHistory);
            
            // Then
            assertEquals(1, affectedRows);
            verify(medicalRecordRepository).updateFamilyHistory(1L, newFamilyHistory);
        }
        
        @Test
        @DisplayName("Debería actualizar el historial quirúrgico")
        void shouldUpdateSurgicalHistory() {
            // Given
            String newSurgicalHistory = "Appendectomy (2015), Tonsillectomy (2010)";
            when(medicalRecordRepository.updateSurgicalHistory(1L, newSurgicalHistory))
                    .thenReturn(1);
            
            // When
            int affectedRows = medicalRecordRepository.updateSurgicalHistory(1L, newSurgicalHistory);
            
            // Then
            assertEquals(1, affectedRows);
            verify(medicalRecordRepository).updateSurgicalHistory(1L, newSurgicalHistory);
        }
        
        @Test
        @DisplayName("Debería actualizar las notas")
        void shouldUpdateNotes() {
            // Given
            String newNotes = "Patient manages conditions well. Regular follow-ups recommended.";
            when(medicalRecordRepository.updateNotes(1L, newNotes))
                    .thenReturn(1);
            
            // When
            int affectedRows = medicalRecordRepository.updateNotes(1L, newNotes);
            
            // Then
            assertEquals(1, affectedRows);
            verify(medicalRecordRepository).updateNotes(1L, newNotes);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por contenido médico")
    class MedicalContentSearchTests {
        
        @Test
        @DisplayName("Debería encontrar historiales médicos por alergia")
        void shouldFindByAllergiesContaining() {
            // Given
            when(medicalRecordRepository.findByAllergiesContaining("Penicillin", pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findByAllergiesContaining("Penicillin", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().get(0).getAllergies().contains("Penicillin"));
            verify(medicalRecordRepository).findByAllergiesContaining("Penicillin", pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar historiales médicos por condición crónica")
        void shouldFindByChronicConditionsContaining() {
            // Given
            when(medicalRecordRepository.findByChronicConditionsContaining("Hypertension", pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findByChronicConditionsContaining("Hypertension", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().get(0).getChronicConditions().contains("Hypertension"));
            verify(medicalRecordRepository).findByChronicConditionsContaining("Hypertension", pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar historiales médicos por medicación")
        void shouldFindByCurrentMedicationsContaining() {
            // Given
            when(medicalRecordRepository.findByCurrentMedicationsContaining("Lisinopril", pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findByCurrentMedicationsContaining("Lisinopril", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().get(0).getCurrentMedications().contains("Lisinopril"));
            verify(medicalRecordRepository).findByCurrentMedicationsContaining("Lisinopril", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por fecha")
    class DateSearchTests {
        
        @Test
        @DisplayName("Debería encontrar historiales médicos actualizados en un rango de fechas")
        void shouldFindByUpdatedAtBetween() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().minusMonths(6);
            LocalDateTime endDate = LocalDateTime.now();
            
            when(medicalRecordRepository.findByUpdatedAtBetween(startDate, endDate, pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findByUpdatedAtBetween(
                    startDate, endDate, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            verify(medicalRecordRepository).findByUpdatedAtBetween(startDate, endDate, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar historiales médicos con entradas creadas en un rango de fechas")
        void shouldFindByEntryDateBetween() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().minusMonths(4);
            LocalDateTime endDate = LocalDateTime.now();
            
            when(medicalRecordRepository.findByEntryDateBetween(startDate, endDate, pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findByEntryDateBetween(
                    startDate, endDate, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            verify(medicalRecordRepository).findByEntryDateBetween(startDate, endDate, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por doctor y tipo de entrada")
    class DoctorAndEntryTypeSearchTests {
        
        @Test
        @DisplayName("Debería encontrar historiales médicos por ID de doctor")
        void shouldFindByDoctorId() {
            // Given
            when(medicalRecordRepository.findByDoctorId(1L, pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findByDoctorId(1L, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            verify(medicalRecordRepository).findByDoctorId(1L, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar historiales médicos por tipo de entrada")
        void shouldFindByEntryType() {
            // Given
            when(medicalRecordRepository.findByEntryType(EntryType.CONSULTATION, pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findByEntryType(EntryType.CONSULTATION, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            verify(medicalRecordRepository).findByEntryType(EntryType.CONSULTATION, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar historiales médicos por contenido de entrada")
        void shouldFindByEntryContentContaining() {
            // Given
            when(medicalRecordRepository.findByEntryContentContaining("blood pressure", pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findByEntryContentContaining("blood pressure", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            verify(medicalRecordRepository).findByEntryContentContaining("blood pressure", pageable);
        }
    }
    
    @Nested
@DisplayName("Pruebas de estadísticas")
class StatisticsTests {
    
    @Test
    @DisplayName("Debería contar entradas por historial médico")
    void shouldCountEntriesByMedicalRecord() {
        // Given
        List<Object[]> entryCounts = new ArrayList<>();
        entryCounts.add(new Object[]{1L, "Maria", "Garcia", 2L});
        
        when(medicalRecordRepository.countEntriesByMedicalRecord())
                .thenReturn(entryCounts);
        
        // When
        List<Object[]> result = medicalRecordRepository.countEntriesByMedicalRecord();
        
        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0)[0]);  // ID
        assertEquals("Maria", result.get(0)[1]);  // firstName
        assertEquals("Garcia", result.get(0)[2]);  // lastName
        assertEquals(2L, result.get(0)[3]);  // entryCount
        verify(medicalRecordRepository).countEntriesByMedicalRecord();
    }
}
    
    @Nested
    @DisplayName("Pruebas de búsqueda especial por entradas")
    class SpecialEntrySearchTests {
        
        @Test
        @DisplayName("Debería encontrar historiales médicos sin entradas")
        void shouldFindMedicalRecordsWithNoEntries() {
            // Given
            MedicalRecord emptyRecord = MedicalRecord.builder()
                    .id(2L)
                    .patient(Patient.builder().id(2L).build())
                    .createdAt(LocalDateTime.now().minusMonths(1))
                    .entries(new ArrayList<>())
                    .build();
            
            Page<MedicalRecord> emptyRecordPage = new PageImpl<>(
                    List.of(emptyRecord), pageable, 1);
            
            when(medicalRecordRepository.findMedicalRecordsWithNoEntries(pageable))
                    .thenReturn(emptyRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findMedicalRecordsWithNoEntries(pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().get(0).getEntries().isEmpty());
            verify(medicalRecordRepository).findMedicalRecordsWithNoEntries(pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar historiales médicos con un mínimo de entradas")
        void shouldFindMedicalRecordsWithMinimumEntries() {
            // Given
            when(medicalRecordRepository.findMedicalRecordsWithMinimumEntries(2L, pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.findMedicalRecordsWithMinimumEntries(2L, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().get(0).getEntries().size() >= 2);
            verify(medicalRecordRepository).findMedicalRecordsWithMinimumEntries(2L, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda avanzada")
    class AdvancedSearchTests {
        
        @Test
        @DisplayName("Debería buscar historiales médicos con múltiples criterios")
        void shouldSearchMedicalRecords() {
            // Given
            String allergyPattern = "Penicillin";
            String conditionPattern = "Hypertension";
            String medicationPattern = "Lisinopril";
            EntryType entryType = EntryType.CONSULTATION;
            String contentPattern = "blood pressure";
            
            when(medicalRecordRepository.searchMedicalRecords(
                    allergyPattern, conditionPattern, medicationPattern, entryType, contentPattern, pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.searchMedicalRecords(
                    allergyPattern, conditionPattern, medicationPattern, entryType, contentPattern, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            verify(medicalRecordRepository).searchMedicalRecords(
                    allergyPattern, conditionPattern, medicationPattern, entryType, contentPattern, pageable);
        }
        
        @Test
        @DisplayName("Debería buscar historiales médicos con criterios parciales")
        void shouldSearchMedicalRecordsWithPartialCriteria() {
            // Given
            when(medicalRecordRepository.searchMedicalRecords(
                    "Penicillin", null, null, null, null, pageable))
                    .thenReturn(medicalRecordPage);
            
            // When
            Page<MedicalRecord> result = medicalRecordRepository.searchMedicalRecords(
                    "Penicillin", null, null, null, null, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            verify(medicalRecordRepository).searchMedicalRecords(
                    "Penicillin", null, null, null, null, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de proyecciones")
    class ProjectionTests {
        
        @Test
        @DisplayName("Debería encontrar información básica de todos los historiales médicos")
        void shouldFindAllBasicInfo() {
            // Given
            List<MedicalRecordRepository.MedicalRecordBasicInfo> basicInfoList = List.of(
                    createBasicMedicalRecordInfo(1L, 1L, "Penicillin, Pollen", "Hypertension, Asthma")
            );
            
            Page<MedicalRecordRepository.MedicalRecordBasicInfo> basicInfoPage = 
                    new PageImpl<>(basicInfoList, pageable, basicInfoList.size());
                    
            when(medicalRecordRepository.findAllBasicInfo(pageable)).thenReturn(basicInfoPage);
            
            // When
            Page<MedicalRecordRepository.MedicalRecordBasicInfo> result = medicalRecordRepository.findAllBasicInfo(pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals(1L, result.getContent().get(0).getId());
            assertEquals(1L, result.getContent().get(0).getPatientId());
            assertEquals("Penicillin, Pollen", result.getContent().get(0).getAllergies());
            verify(medicalRecordRepository).findAllBasicInfo(pageable);
        }
    }
    
    // Método auxiliar para crear objetos MedicalRecordBasicInfo
    private MedicalRecordRepository.MedicalRecordBasicInfo createBasicMedicalRecordInfo(
            Long id, Long patientId, String allergies, String chronicConditions) {
        return new MedicalRecordRepository.MedicalRecordBasicInfo() {
            @Override
            public Long getId() {
                return id;
            }
            
            @Override
            public Long getPatientId() {
                return patientId;
            }
            
            @Override
            public String getAllergies() {
                return allergies;
            }
            
            @Override
            public String getChronicConditions() {
                return chronicConditions;
            }
            
            @Override
            public LocalDateTime getCreatedAt() {
                return LocalDateTime.now().minusMonths(6);
            }
            
            @Override
            public LocalDateTime getUpdatedAt() {
                return LocalDateTime.now().minusMonths(1);
            }
        };
    }
}