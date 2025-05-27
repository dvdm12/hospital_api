package com.example.miapp.service.medicalrecord;

import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import com.example.miapp.entity.Patient;
import com.example.miapp.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordManagementServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private MedicalRecordManagementService managementService;

    @Captor
    private ArgumentCaptor<MedicalRecord> medicalRecordCaptor;

    private MedicalRecord testMedicalRecord;
    private Patient testPatient;
    private Doctor testDoctor;
    private MedicalRecordEntry testEntry;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        testPatient = Patient.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        testMedicalRecord = MedicalRecord.builder()
                .id(1L)
                .patient(testPatient)
                .allergies("Penicilina")
                .chronicConditions("Hipertensión")
                .currentMedications("Enalapril 10mg")
                .createdAt(LocalDateTime.now())
                .entries(new ArrayList<>())
                .build();

        testDoctor = Doctor.builder()
                .id(1L)
                .firstName("Carlos")
                .lastName("Rodríguez")
                .build();

        testEntry = MedicalRecordEntry.builder()
                .id(1L)
                .medicalRecord(testMedicalRecord)
                .doctor(testDoctor)
                .type(MedicalRecordEntry.EntryType.CONSULTATION)
                .title("Consulta inicial")
                .content("Paciente presenta dolor de cabeza")
                .entryDate(LocalDateTime.now())
                .visibleToPatient(true)
                .build();
    }

    @Test
    void updateAllergies_shouldUpdateAllergiesField_whenRepositoryUpdatesRows() {
        // Arrange
        Long recordId = 1L;
        String newAllergies = "Penicilina, Aspirina, Mariscos";
        when(medicalRecordRepository.updateAllergies(recordId, newAllergies)).thenReturn(1);

        // Act
        managementService.updateAllergies(recordId, newAllergies);

        // Assert
        verify(medicalRecordRepository, times(1)).updateAllergies(recordId, newAllergies);
    }

    @Test
    void updateAllergies_shouldThrowException_whenRepositoryUpdatesFails() {
        // Arrange
        Long recordId = 1L;
        String newAllergies = "Penicilina, Aspirina";
        when(medicalRecordRepository.updateAllergies(recordId, newAllergies)).thenReturn(0);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            managementService.updateAllergies(recordId, newAllergies);
        });
        verify(medicalRecordRepository, times(1)).updateAllergies(recordId, newAllergies);
    }

    @Test
    void updateChronicConditions_shouldUpdateChronicConditionsField_whenRepositoryUpdatesRows() {
        // Arrange
        Long recordId = 1L;
        String newConditions = "Hipertensión, Diabetes tipo 2";
        when(medicalRecordRepository.updateChronicConditions(recordId, newConditions)).thenReturn(1);

        // Act
        managementService.updateChronicConditions(recordId, newConditions);

        // Assert
        verify(medicalRecordRepository, times(1)).updateChronicConditions(recordId, newConditions);
    }

    @Test
    void updateCurrentMedications_shouldUpdateMedicationsField_whenRepositoryUpdatesRows() {
        // Arrange
        Long recordId = 1L;
        String newMedications = "Enalapril 10mg, Metformina 850mg";
        when(medicalRecordRepository.updateCurrentMedications(recordId, newMedications)).thenReturn(1);

        // Act
        managementService.updateCurrentMedications(recordId, newMedications);

        // Assert
        verify(medicalRecordRepository, times(1)).updateCurrentMedications(recordId, newMedications);
    }

    @Test
    void addEntryToMedicalRecord_shouldAddEntryToRecordAndSave() {
        // Arrange
        MedicalRecordEntry newEntry = MedicalRecordEntry.builder()
                .id(2L)
                .doctor(testDoctor)
                .type(MedicalRecordEntry.EntryType.LAB_RESULT)
                .title("Resultados de análisis de sangre")
                .content("Niveles normales")
                .entryDate(LocalDateTime.now())
                .build();

        // Act
        managementService.addEntryToMedicalRecord(testMedicalRecord, newEntry);

        // Assert
        verify(medicalRecordRepository, times(1)).save(medicalRecordCaptor.capture());
        MedicalRecord savedRecord = medicalRecordCaptor.getValue();
        
        assertNotNull(savedRecord.getEntries());
        assertTrue(savedRecord.getEntries().contains(newEntry));
        assertEquals(testMedicalRecord, newEntry.getMedicalRecord());
    }

    @Test
    void removeEntryFromMedicalRecord_shouldRemoveEntryAndSave() {
        // Arrange
        testMedicalRecord.addEntry(testEntry);

        // Act
        managementService.removeEntryFromMedicalRecord(testMedicalRecord, testEntry);

        // Assert
        verify(medicalRecordRepository, times(1)).save(medicalRecordCaptor.capture());
        MedicalRecord savedRecord = medicalRecordCaptor.getValue();
        
        assertFalse(savedRecord.getEntries().contains(testEntry));
    }

    @Test
    void updateMedicalRecordFields_shouldUpdateAllProvidedFields() {
        // Arrange
        Long recordId = 1L;
        String allergies = "Penicilina, Sulfas";
        String chronicConditions = "Hipertensión, Asma";
        String currentMedications = "Enalapril 10mg, Salbutamol";
        String surgicalHistory = "Apendicectomía 2020";
        String familyHistory = "Padre: Diabetes, Madre: Hipertensión";
        String notes = "Paciente con buena adherencia al tratamiento";

        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(testMedicalRecord));

        // Act
        managementService.updateMedicalRecordFields(recordId, allergies, chronicConditions, 
                                                  currentMedications, surgicalHistory, 
                                                  familyHistory, notes);

        // Assert
        verify(medicalRecordRepository, times(1)).save(medicalRecordCaptor.capture());
        MedicalRecord savedRecord = medicalRecordCaptor.getValue();
        
        assertEquals(allergies, savedRecord.getAllergies());
        assertEquals(chronicConditions, savedRecord.getChronicConditions());
        assertEquals(currentMedications, savedRecord.getCurrentMedications());
        assertEquals(surgicalHistory, savedRecord.getSurgicalHistory());
        assertEquals(familyHistory, savedRecord.getFamilyHistory());
        assertEquals(notes, savedRecord.getNotes());
    }

    @Test
    void updateMedicalRecordFields_shouldOnlyUpdateProvidedFields() {
        // Arrange
        Long recordId = 1L;
        String originalAllergies = "Penicilina";
        String originalChronicConditions = "Hipertensión";
        String newMedications = "Enalapril 10mg, Aspirina 100mg";
        
        testMedicalRecord.setAllergies(originalAllergies);
        testMedicalRecord.setChronicConditions(originalChronicConditions);
        
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(testMedicalRecord));

        // Act - Solo actualizamos medicamentos
        managementService.updateMedicalRecordFields(recordId, null, null, 
                                                  newMedications, null, null, null);

        // Assert
        verify(medicalRecordRepository, times(1)).save(medicalRecordCaptor.capture());
        MedicalRecord savedRecord = medicalRecordCaptor.getValue();
        
        assertEquals(originalAllergies, savedRecord.getAllergies()); // No cambió
        assertEquals(originalChronicConditions, savedRecord.getChronicConditions()); // No cambió
        assertEquals(newMedications, savedRecord.getCurrentMedications()); // Sí cambió
    }

    @Test
    void updateEntryVisibility_shouldUpdateVisibilityAndSave() {
        // Arrange
        testMedicalRecord.addEntry(testEntry);
        testEntry.setVisibleToPatient(false);

        // Act
        managementService.updateEntryVisibility(testEntry, true);

        // Assert
        verify(medicalRecordRepository, times(1)).save(medicalRecordCaptor.capture());
        assertTrue(testEntry.isVisibleToPatient());
    }

    @Test
    void mergeMedicalRecords_shouldMergeRecordsAndArchiveSecondary() {
        // Arrange
        MedicalRecord primaryRecord = MedicalRecord.builder()
                .id(1L)
                .patient(testPatient)
                .allergies("Penicilina")
                .chronicConditions("Hipertensión")
                .entries(new ArrayList<>())
                .build();

        MedicalRecord secondaryRecord = MedicalRecord.builder()
                .id(2L)
                .patient(testPatient)
                .allergies("Aspirina")
                .chronicConditions(null)
                .currentMedications("Metformina 500mg")
                .entries(new ArrayList<>())
                .build();

        MedicalRecordEntry secondaryEntry = MedicalRecordEntry.builder()
                .id(3L)
                .medicalRecord(secondaryRecord)
                .doctor(testDoctor)
                .type(MedicalRecordEntry.EntryType.DIAGNOSIS)
                .title("Diagnóstico previo")
                .content("Diagnóstico de diabetes tipo 2")
                .entryDate(LocalDateTime.now().minusMonths(2))
                .build();

        secondaryRecord.addEntry(secondaryEntry);
        
        // Para evitar problemas con archiveMedicalRecord, hacemos un mock parcial del servicio
        MedicalRecordManagementService spyService = spy(managementService);
        doNothing().when(spyService).archiveMedicalRecord(anyLong(), anyString());

        // Act
        spyService.mergeMedicalRecords(primaryRecord, secondaryRecord, "Unificación de registros");

        // Assert
        verify(medicalRecordRepository, times(1)).save(primaryRecord);
        verify(spyService, times(1)).archiveMedicalRecord(eq(2L), contains("Merged into record 1"));
        
        // Verificar que se combinaron las alergias
        assertThat(primaryRecord.getAllergies()).contains("Penicilina").contains("Aspirina");
        
        // Verificar que se mantuvo la condición crónica original
        assertEquals("Hipertensión", primaryRecord.getChronicConditions());
        
        // Verificar que se añadieron los medicamentos
        assertEquals("Metformina 500mg", primaryRecord.getCurrentMedications());
        
        // Verificar que se añadió la entrada del registro secundario
        assertTrue(primaryRecord.getEntries().contains(secondaryEntry));
        
        // Verificar que se actualizó el contenido de la entrada para indicar la fusión
        assertThat(secondaryEntry.getContent()).contains("[Merged from record 2");
        
        // Verificar que se añadió una nota sobre la fusión
        assertThat(primaryRecord.getNotes()).contains("MERGED with record 2");
    }

    @Test
    void bulkUpdateEntryVisibility_shouldUpdateAllEntriesVisibility() {
        // Arrange
        MedicalRecordEntry entry1 = MedicalRecordEntry.builder()
                .id(1L)
                .medicalRecord(testMedicalRecord)
                .visibleToPatient(true)
                .build();

        MedicalRecordEntry entry2 = MedicalRecordEntry.builder()
                .id(2L)
                .medicalRecord(testMedicalRecord)
                .visibleToPatient(true)
                .build();

        testMedicalRecord.setEntries(Arrays.asList(entry1, entry2));
        
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(testMedicalRecord));

        // Act
        managementService.bulkUpdateEntryVisibility(1L, false);

        // Assert
        verify(medicalRecordRepository, times(1)).save(medicalRecordCaptor.capture());
        MedicalRecord savedRecord = medicalRecordCaptor.getValue();
        
        for (MedicalRecordEntry entry : savedRecord.getEntries()) {
            assertFalse(entry.isVisibleToPatient());
        }
    }

    @Test
    void deleteMedicalRecord_shouldDeleteRecordFromRepository() {
        // Arrange
        Long recordId = 1L;
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(testMedicalRecord));

        // Act
        managementService.deleteMedicalRecord(recordId);

        // Assert
        verify(medicalRecordRepository, times(1)).delete(testMedicalRecord);
    }

    @Test
    void deleteMedicalRecord_shouldThrowException_whenRecordNotFound() {
        // Arrange
        Long recordId = 999L;
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            managementService.deleteMedicalRecord(recordId);
        });
    }
}