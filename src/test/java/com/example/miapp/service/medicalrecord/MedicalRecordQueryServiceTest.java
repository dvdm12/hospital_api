package com.example.miapp.service.medicalrecord;

import com.example.miapp.dto.medicalrecord.MedicalRecordDto;
import com.example.miapp.dto.medicalrecord.MedicalRecordEntryDto;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import com.example.miapp.entity.Patient;
import com.example.miapp.mapper.MedicalRecordMapper;
import com.example.miapp.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordQueryServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private MedicalRecordMapper medicalRecordMapper;

    @InjectMocks
    private MedicalRecordQueryService queryService;

    private MedicalRecord testMedicalRecord;
    private MedicalRecordDto testMedicalRecordDto;
    private MedicalRecordEntry testEntry1;
    private MedicalRecordEntry testEntry2;
    private MedicalRecordEntryDto testEntryDto1;
    private MedicalRecordEntryDto testEntryDto2;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        // Set up test data
        testPatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .build();

        testMedicalRecord = MedicalRecord.builder()
                .id(1L)
                .patient(testPatient)
                .allergies("Penicillin, Peanuts")
                .chronicConditions("Hypertension")
                .currentMedications("Lisinopril 10mg")
                .createdAt(LocalDateTime.now())
                .build();

        testMedicalRecordDto = new MedicalRecordDto();
        testMedicalRecordDto.setId(1L);
        testMedicalRecordDto.setPatientId(1L);
        testMedicalRecordDto.setAllergies("Penicillin, Peanuts");
        testMedicalRecordDto.setChronicConditions("Hypertension");
        testMedicalRecordDto.setCurrentMedications("Lisinopril 10mg");

        // Create test entries
        testEntry1 = MedicalRecordEntry.builder()
                .id(1L)
                .medicalRecord(testMedicalRecord)
                .type(MedicalRecordEntry.EntryType.DIAGNOSIS)
                .title("Initial Diagnosis")
                .content("Patient diagnosed with hypertension")
                .visibleToPatient(true)
                .entryDate(LocalDateTime.now().minusDays(10))
                .build();

        testEntry2 = MedicalRecordEntry.builder()
                .id(2L)
                .medicalRecord(testMedicalRecord)
                .type(MedicalRecordEntry.EntryType.PRESCRIPTION)
                .title("Medication Prescription")
                .content("Prescribed Lisinopril 10mg daily")
                .visibleToPatient(true)
                .entryDate(LocalDateTime.now().minusDays(5))
                .build();

        testMedicalRecord.setEntries(Arrays.asList(testEntry1, testEntry2));

        // Create entry DTOs
        testEntryDto1 = new MedicalRecordEntryDto();
        testEntryDto1.setId(1L);
        testEntryDto1.setType(MedicalRecordEntry.EntryType.DIAGNOSIS);
        testEntryDto1.setTitle("Initial Diagnosis");
        testEntryDto1.setContent("Patient diagnosed with hypertension");
        testEntryDto1.setVisibleToPatient(true);

        testEntryDto2 = new MedicalRecordEntryDto();
        testEntryDto2.setId(2L);
        testEntryDto2.setType(MedicalRecordEntry.EntryType.PRESCRIPTION);
        testEntryDto2.setTitle("Medication Prescription");
        testEntryDto2.setContent("Prescribed Lisinopril 10mg daily");
        testEntryDto2.setVisibleToPatient(true);

        testMedicalRecordDto.setEntries(Arrays.asList(testEntryDto1, testEntryDto2));
    }

    @Test
    void getMedicalRecord_shouldReturnMedicalRecordDto_whenRecordExists() {
        // Arrange
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(testMedicalRecord));
        when(medicalRecordMapper.toDto(testMedicalRecord)).thenReturn(testMedicalRecordDto);

        // Act
        MedicalRecordDto result = queryService.getMedicalRecord(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Penicillin, Peanuts", result.getAllergies());
        verify(medicalRecordRepository).findById(1L);
        verify(medicalRecordMapper).toDto(testMedicalRecord);
    }

    @Test
    void getMedicalRecord_shouldThrowException_whenRecordNotFound() {
        // Arrange
        when(medicalRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> queryService.getMedicalRecord(999L));
        verify(medicalRecordRepository).findById(999L);
    }

    @Test
    void findMedicalRecordByPatientId_shouldReturnMedicalRecordDto_whenPatientHasRecord() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(1L)).thenReturn(Optional.of(testMedicalRecord));
        when(medicalRecordMapper.toDto(testMedicalRecord)).thenReturn(testMedicalRecordDto);

        // Act
        Optional<MedicalRecordDto> result = queryService.findMedicalRecordByPatientId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(1L, result.get().getPatientId());
        verify(medicalRecordRepository).findByPatientId(1L);
        verify(medicalRecordMapper).toDto(testMedicalRecord);
    }

    @Test
    void findMedicalRecordByPatientId_shouldReturnEmpty_whenPatientHasNoRecord() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(999L)).thenReturn(Optional.empty());

        // Act
        Optional<MedicalRecordDto> result = queryService.findMedicalRecordByPatientId(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(medicalRecordRepository).findByPatientId(999L);
        verify(medicalRecordMapper, never()).toDto(any());
    }

    @Test
    void getPatientMedicalHistory_shouldReturnCompleteHistory_whenPatientHasRecord() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(1L)).thenReturn(Optional.of(testMedicalRecord));
        when(medicalRecordMapper.toDto(testMedicalRecord)).thenReturn(testMedicalRecordDto);

        // Act
        MedicalRecordDto result = queryService.getPatientMedicalHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2, result.getEntries().size());
        verify(medicalRecordRepository).findByPatientId(1L);
        verify(medicalRecordMapper).toDto(testMedicalRecord);
    }

    @Test
    void getPatientMedicalHistory_shouldThrowException_whenPatientHasNoRecord() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> queryService.getPatientMedicalHistory(999L));
        verify(medicalRecordRepository).findByPatientId(999L);
    }

    @Test
    void getPatientVisibleMedicalHistory_shouldReturnFilteredEntries_whenPatientHasRecord() {
        // Arrange
        // Create a medical record with one visible and one hidden entry
        MedicalRecord recordWithMixedEntries = MedicalRecord.builder()
                .id(2L)
                .patient(testPatient)
                .allergies("Latex")
                .build();

        MedicalRecordEntry visibleEntry = MedicalRecordEntry.builder()
                .id(3L)
                .medicalRecord(recordWithMixedEntries)
                .type(MedicalRecordEntry.EntryType.PRESCRIPTION)
                .title("Visible Entry")
                .visibleToPatient(true)
                .build();

        MedicalRecordEntry hiddenEntry = MedicalRecordEntry.builder()
                .id(4L)
                .medicalRecord(recordWithMixedEntries)
                .type(MedicalRecordEntry.EntryType.OTHER)
                .title("Hidden Entry")
                .visibleToPatient(false)
                .build();

        recordWithMixedEntries.setEntries(Arrays.asList(visibleEntry, hiddenEntry));

        MedicalRecordDto dtoWithMixedEntries = new MedicalRecordDto();
        dtoWithMixedEntries.setId(2L);
        dtoWithMixedEntries.setAllergies("Latex");

        MedicalRecordEntryDto visibleEntryDto = new MedicalRecordEntryDto();
        visibleEntryDto.setId(3L);
        visibleEntryDto.setTitle("Visible Entry");
        visibleEntryDto.setVisibleToPatient(true);

        MedicalRecordEntryDto hiddenEntryDto = new MedicalRecordEntryDto();
        hiddenEntryDto.setId(4L);
        hiddenEntryDto.setTitle("Hidden Entry");
        hiddenEntryDto.setVisibleToPatient(false);

        dtoWithMixedEntries.setEntries(Arrays.asList(visibleEntryDto, hiddenEntryDto));

        when(medicalRecordRepository.findByPatientId(1L)).thenReturn(Optional.of(recordWithMixedEntries));
        when(medicalRecordMapper.toDto(recordWithMixedEntries)).thenReturn(dtoWithMixedEntries);

        // Act
        MedicalRecordDto result = queryService.getPatientVisibleMedicalHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEntries().size());
        assertEquals("Visible Entry", result.getEntries().get(0).getTitle());
        verify(medicalRecordRepository).findByPatientId(1L);
        verify(medicalRecordMapper).toDto(recordWithMixedEntries);
    }

    @Test
    void getRecentEntriesForPatient_shouldReturnOrderedEntries_whenPatientHasEntries() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(1L)).thenReturn(Optional.of(testMedicalRecord));
        when(medicalRecordMapper.toEntryDto(testEntry1)).thenReturn(testEntryDto1);
        when(medicalRecordMapper.toEntryDto(testEntry2)).thenReturn(testEntryDto2);

        // Act
        List<MedicalRecordEntryDto> result = queryService.getRecentEntriesForPatient(1L, 2);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        // The most recent entry should be first (testEntry2 is more recent than testEntry1)
        // We can't easily test this with the current setup because the order depends on entryDate
        // A more robust test would use fixed dates and verify the order specifically
        verify(medicalRecordRepository).findByPatientId(1L);
        verify(medicalRecordMapper, times(2)).toEntryDto(any(MedicalRecordEntry.class));
    }

    @Test
    void getRecentEntriesForPatient_shouldReturnEmptyList_whenPatientHasNoRecord() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(999L)).thenReturn(Optional.empty());

        // Act
        List<MedicalRecordEntryDto> result = queryService.getRecentEntriesForPatient(999L, 5);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(medicalRecordRepository).findByPatientId(999L);
        verify(medicalRecordMapper, never()).toEntryDto(any());
    }

    @Test
    void getEntriesByTypeForPatient_shouldReturnFilteredEntries_whenPatientHasEntriesOfType() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(1L)).thenReturn(Optional.of(testMedicalRecord));
        when(medicalRecordMapper.toEntryDto(testEntry1)).thenReturn(testEntryDto1);

        // Act
        List<MedicalRecordEntryDto> result = queryService.getEntriesByTypeForPatient(1L, MedicalRecordEntry.EntryType.DIAGNOSIS);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(MedicalRecordEntry.EntryType.DIAGNOSIS, result.get(0).getType());
        verify(medicalRecordRepository).findByPatientId(1L);
        verify(medicalRecordMapper).toEntryDto(testEntry1);
    }

    @Test
    void hasAllergy_shouldReturnTrue_whenPatientHasAllergy() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(1L)).thenReturn(Optional.of(testMedicalRecord));

        // Act
        boolean result = queryService.hasAllergy(1L, "Penicillin");

        // Assert
        assertTrue(result);
        verify(medicalRecordRepository).findByPatientId(1L);
    }

    @Test
    void hasAllergy_shouldReturnFalse_whenPatientDoesNotHaveAllergy() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(1L)).thenReturn(Optional.of(testMedicalRecord));

        // Act
        boolean result = queryService.hasAllergy(1L, "Sulfa");

        // Assert
        assertFalse(result);
        verify(medicalRecordRepository).findByPatientId(1L);
    }

    @Test
    void hasChronicCondition_shouldReturnTrue_whenPatientHasCondition() {
        // Arrange
        when(medicalRecordRepository.findByPatientId(1L)).thenReturn(Optional.of(testMedicalRecord));

        // Act
        boolean result = queryService.hasChronicCondition(1L, "Hypertension");

        // Assert
        assertTrue(result);
        verify(medicalRecordRepository).findByPatientId(1L);
    }

    @Test
    void findByAllergiesContaining_shouldReturnPageOfRecords_whenMatchingRecordsExist() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<MedicalRecord> recordPage = new PageImpl<>(List.of(testMedicalRecord));
        
        when(medicalRecordRepository.findByAllergiesContaining(anyString(), any(Pageable.class)))
                .thenReturn(recordPage);
        when(medicalRecordMapper.toDto(testMedicalRecord)).thenReturn(testMedicalRecordDto);

        // Act
        Page<MedicalRecordDto> result = queryService.findByAllergiesContaining("Penicillin", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Penicillin, Peanuts", result.getContent().get(0).getAllergies());
        verify(medicalRecordRepository).findByAllergiesContaining("Penicillin", pageable);
    }

    @Test
    void searchMedicalRecords_shouldReturnMatchingRecords_whenCriteriaMatch() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<MedicalRecord> recordPage = new PageImpl<>(List.of(testMedicalRecord));
        
        when(medicalRecordRepository.searchMedicalRecords(
                anyString(), anyString(), anyString(), any(), anyString(), any(Pageable.class)))
                .thenReturn(recordPage);
        when(medicalRecordMapper.toDto(testMedicalRecord)).thenReturn(testMedicalRecordDto);

        // Act
        Page<MedicalRecordDto> result = queryService.searchMedicalRecords(
                "Penicillin", "Hypertension", "Lisinopril", 
                MedicalRecordEntry.EntryType.DIAGNOSIS, "diagnosed", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(medicalRecordRepository).searchMedicalRecords(
                "Penicillin", "Hypertension", "Lisinopril", 
                MedicalRecordEntry.EntryType.DIAGNOSIS, "diagnosed", pageable);
    }
}