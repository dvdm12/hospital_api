package com.example.miapp.service.patient;

import com.example.miapp.dto.patient.PatientDto;
import com.example.miapp.dto.patient.PatientSearchCriteria;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.User;
import com.example.miapp.entity.Patient.Gender;
import com.example.miapp.mapper.PatientMapper;
import com.example.miapp.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientQueryServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientQueryService queryService;

    private Patient testPatient;
    private PatientDto testPatientDto;
    private final Long patientId = 1L;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup test patient
        testPatient = new Patient();
        testPatient.setId(patientId);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setGender(Gender.MALE);
        testPatient.setPhone("1234567890");
        
        // Email is probably stored in the User entity
        User user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        testPatient.setUser(user);
        
        // Set birthdate to 30 years ago
        LocalDate birthDate = LocalDate.now().minusYears(30);
        testPatient.setBirthDate(Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
        // Setup patient DTO
        testPatientDto = new PatientDto();
        testPatientDto.setId(patientId);
        testPatientDto.setFirstName("John");
        testPatientDto.setLastName("Doe");
        testPatientDto.setGender(Gender.MALE);
        testPatientDto.setPhone("1234567890");
        // Email en el DTO depende de cómo esté implementado el mapper
        
        // Setup pageable
        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Get Patient Tests")
    class GetPatientTests {

        @Test
        @DisplayName("Should return patient DTO when patient exists")
        void shouldReturnPatientDtoWhenPatientExists() {
            // Arrange
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            PatientDto result = queryService.getPatient(patientId);

            // Assert
            assertNotNull(result);
            assertEquals(patientId, result.getId());
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            verify(patientRepository).findById(patientId);
            verify(patientMapper).toDto(testPatient);
        }

        @Test
        @DisplayName("Should throw exception when patient does not exist")
        void shouldThrowExceptionWhenPatientDoesNotExist() {
            // Arrange
            when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                    () -> queryService.getPatient(patientId));

            assertTrue(exception.getMessage().contains("Patient not found"));
        }
    }

    @Nested
    @DisplayName("Find Patient By User Id Tests")
    class FindPatientByUserIdTests {

        @Test
        @DisplayName("Should return patient DTO when user ID exists")
        void shouldReturnPatientDtoWhenUserIdExists() {
            // Arrange
            Long userId = 1L;
            when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(testPatient));
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Optional<PatientDto> result = queryService.findPatientByUserId(userId);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(patientId, result.get().getId());
            verify(patientRepository).findByUserId(userId);
            verify(patientMapper).toDto(testPatient);
        }

        @Test
        @DisplayName("Should return empty optional when user ID does not exist")
        void shouldReturnEmptyOptionalWhenUserIdDoesNotExist() {
            // Arrange
            Long userId = 999L;
            when(patientRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // Act
            Optional<PatientDto> result = queryService.findPatientByUserId(userId);

            // Assert
            assertFalse(result.isPresent());
            verify(patientRepository).findByUserId(userId);
            verify(patientMapper, never()).toDto(any(Patient.class));
        }
    }

    @Nested
    @DisplayName("Find Patients By Full Name Tests")
    class FindPatientsByFullNameTests {

        @Test
        @DisplayName("Should return page of patients with matching full name")
        void shouldReturnPageOfPatientsWithMatchingFullName() {
            // Arrange
            String firstName = "John";
            String lastName = "Doe";
            List<Patient> patients = Arrays.asList(testPatient);
            Page<Patient> patientPage = new PageImpl<>(patients, pageable, patients.size());
            
            when(patientRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName, pageable))
                .thenReturn(patientPage);
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Page<PatientDto> result = queryService.findPatientsByFullName(firstName, lastName, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(firstName, result.getContent().get(0).getFirstName());
            assertEquals(lastName, result.getContent().get(0).getLastName());
            verify(patientRepository).findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName, pageable);
            verify(patientMapper).toDto(testPatient);
        }
    }

    @Nested
    @DisplayName("Search Patients By Name Tests")
    class SearchPatientsByNameTests {

        @Test
        @DisplayName("Should return page of patients when name pattern matches")
        void shouldReturnPageOfPatientsWhenNamePatternMatches() {
            // Arrange
            String namePattern = "John";
            List<Patient> patients = Arrays.asList(testPatient);
            Page<Patient> patientPage = new PageImpl<>(patients, pageable, patients.size());
            
            when(patientRepository.findByNameContaining(namePattern, pageable)).thenReturn(patientPage);
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Page<PatientDto> result = queryService.searchPatientsByName(namePattern, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(patientId, result.getContent().get(0).getId());
            assertEquals("John", result.getContent().get(0).getFirstName());
            verify(patientRepository).findByNameContaining(namePattern, pageable);
            verify(patientMapper).toDto(testPatient);
        }

        @Test
        @DisplayName("Should return empty page when no patients match name pattern")
        void shouldReturnEmptyPageWhenNoPatientsMatchNamePattern() {
            // Arrange
            String namePattern = "Nonexistent";
            Page<Patient> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            
            when(patientRepository.findByNameContaining(namePattern, pageable)).thenReturn(emptyPage);

            // Act
            Page<PatientDto> result = queryService.searchPatientsByName(namePattern, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
            verify(patientRepository).findByNameContaining(namePattern, pageable);
            verify(patientMapper, never()).toDto(any(Patient.class));
        }
    }

    @Nested
    @DisplayName("Find Patients By Gender Tests")
    class FindPatientsByGenderTests {

        @Test
        @DisplayName("Should return page of patients with specified gender")
        void shouldReturnPageOfPatientsWithSpecifiedGender() {
            // Arrange
            Gender gender = Gender.MALE;
            List<Patient> patients = Arrays.asList(testPatient);
            Page<Patient> patientPage = new PageImpl<>(patients, pageable, patients.size());
            
            when(patientRepository.findByGender(gender, pageable)).thenReturn(patientPage);
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Page<PatientDto> result = queryService.findPatientsByGender(gender, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(gender, result.getContent().get(0).getGender());
            verify(patientRepository).findByGender(gender, pageable);
            verify(patientMapper).toDto(testPatient);
        }

        @Test
        @DisplayName("Should return empty page when no patients with specified gender")
        void shouldReturnEmptyPageWhenNoPatientsWithSpecifiedGender() {
            // Arrange
            Gender gender = Gender.FEMALE;
            Page<Patient> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            
            when(patientRepository.findByGender(gender, pageable)).thenReturn(emptyPage);

            // Act
            Page<PatientDto> result = queryService.findPatientsByGender(gender, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
            verify(patientRepository).findByGender(gender, pageable);
            verify(patientMapper, never()).toDto(any(Patient.class));
        }
    }

    @Nested
    @DisplayName("Find Patients By Age Range Tests")
    class FindPatientsByAgeRangeTests {

        @Test
        @DisplayName("Should return page of patients within age range")
        void shouldReturnPageOfPatientsWithinAgeRange() {
            // Arrange
            int minAge = 25;
            int maxAge = 35;
            List<Patient> patients = Arrays.asList(testPatient);
            Page<Patient> patientPage = new PageImpl<>(patients, pageable, patients.size());
            
            // Need to capture the Date objects since we can't directly compare them
            when(patientRepository.findByAgeRange(any(Date.class), any(Date.class), eq(pageable)))
                .thenReturn(patientPage);
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Page<PatientDto> result = queryService.findPatientsByAgeRange(minAge, maxAge, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(patientRepository).findByAgeRange(any(Date.class), any(Date.class), eq(pageable));
            verify(patientMapper).toDto(testPatient);
        }
    }

    @Nested
    @DisplayName("Find Patients By Insurance Provider Tests")
    class FindPatientsByInsuranceProviderTests {

        @Test
        @DisplayName("Should return page of patients with specified insurance provider")
        void shouldReturnPageOfPatientsWithSpecifiedInsuranceProvider() {
            // Arrange
            String insuranceProvider = "Test Insurance";
            List<Patient> patients = Arrays.asList(testPatient);
            Page<Patient> patientPage = new PageImpl<>(patients, pageable, patients.size());
            
            when(patientRepository.findByInsuranceProviderContainingIgnoreCase(insuranceProvider, pageable))
                .thenReturn(patientPage);
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Page<PatientDto> result = queryService.findPatientsByInsuranceProvider(insuranceProvider, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(patientRepository).findByInsuranceProviderContainingIgnoreCase(insuranceProvider, pageable);
            verify(patientMapper).toDto(testPatient);
        }
    }

    @Nested
    @DisplayName("Search Patients Tests")
    class SearchPatientsTests {

        @Test
        @DisplayName("Should search patients with multiple criteria")
        void shouldSearchPatientsWithMultipleCriteria() {
            // Arrange
            PatientSearchCriteria criteria = new PatientSearchCriteria();
            criteria.setName("John");
            criteria.setGender(Gender.MALE);
            criteria.setMinAge(25);
            criteria.setMaxAge(35);
            criteria.setInsuranceProvider("Test");
            criteria.setCondition("Hypertension");
            
            List<Patient> patients = Arrays.asList(testPatient);
            Page<Patient> patientPage = new PageImpl<>(patients, pageable, patients.size());
            
            when(patientRepository.searchPatients(
                    criteria.getName(),
                    criteria.getGender(),
                    criteria.getMinAge(),
                    criteria.getMaxAge(),
                    criteria.getInsuranceProvider(),
                    criteria.getCondition(),
                    pageable
            )).thenReturn(patientPage);
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Page<PatientDto> result = queryService.searchPatients(criteria, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(patientRepository).searchPatients(
                    criteria.getName(),
                    criteria.getGender(),
                    criteria.getMinAge(),
                    criteria.getMaxAge(),
                    criteria.getInsuranceProvider(),
                    criteria.getCondition(),
                    pageable
            );
            verify(patientMapper).toDto(testPatient);
        }
    }

    @Nested
    @DisplayName("Get All Patients Tests")
    class GetAllPatientsTests {

        @Test
        @DisplayName("Should return page of all patients")
        void shouldReturnPageOfAllPatients() {
            // Arrange
            List<Patient> patients = Arrays.asList(testPatient);
            Page<Patient> patientPage = new PageImpl<>(patients, pageable, patients.size());
            
            when(patientRepository.findAll(pageable)).thenReturn(patientPage);
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Page<PatientDto> result = queryService.getAllPatients(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(patientRepository).findAll(pageable);
            verify(patientMapper).toDto(testPatient);
        }

        @Test
        @DisplayName("Should return empty page when no patients exist")
        void shouldReturnEmptyPageWhenNoPatientsExist() {
            // Arrange
            Page<Patient> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            
            when(patientRepository.findAll(pageable)).thenReturn(emptyPage);

            // Act
            Page<PatientDto> result = queryService.getAllPatients(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
            verify(patientRepository).findAll(pageable);
            verify(patientMapper, never()).toDto(any(Patient.class));
        }
    }

    @Nested
    @DisplayName("Find Patients Without Recent Appointments Tests")
    class FindPatientsWithoutRecentAppointmentsTests {

        @Test
        @DisplayName("Should return page of patients without recent appointments")
        void shouldReturnPageOfPatientsWithoutRecentAppointments() {
            // Arrange
            String interval = "3 MONTH";
            List<Patient> patients = Arrays.asList(testPatient);
            Page<Patient> patientPage = new PageImpl<>(patients, pageable, patients.size());
            
            when(patientRepository.findPatientsWithoutRecentAppointments(interval, pageable))
                .thenReturn(patientPage);
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Page<PatientDto> result = queryService.findPatientsWithoutRecentAppointments(interval, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(patientRepository).findPatientsWithoutRecentAppointments(interval, pageable);
            verify(patientMapper).toDto(testPatient);
        }
    }

    @Nested
    @DisplayName("Find New Patients Tests")
    class FindNewPatientsTests {

        @Test
        @DisplayName("Should return page of new patients")
        void shouldReturnPageOfNewPatients() {
            // Arrange
            String interval = "1 MONTH";
            List<Patient> patients = Arrays.asList(testPatient);
            Page<Patient> patientPage = new PageImpl<>(patients, pageable, patients.size());
            
            when(patientRepository.findNewPatients(interval, pageable))
                .thenReturn(patientPage);
            when(patientMapper.toDto(testPatient)).thenReturn(testPatientDto);

            // Act
            Page<PatientDto> result = queryService.findNewPatients(interval, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(patientRepository).findNewPatients(interval, pageable);
            verify(patientMapper).toDto(testPatient);
        }
    }

    @Nested
    @DisplayName("Get Patient Statistics Tests")
    class GetPatientStatisticsTests {

        @Test
        @DisplayName("Should return patient statistics by gender")
        void shouldReturnPatientStatisticsByGender() {
            // Arrange
            List<Object[]> stats = Arrays.asList(
                    new Object[]{Gender.MALE, 10L},
                    new Object[]{Gender.FEMALE, 15L}
            );
            
            when(patientRepository.countPatientsByGender()).thenReturn(stats);

            // Act
            List<Object[]> result = queryService.getPatientStatsByGender();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(Gender.MALE, result.get(0)[0]);
            assertEquals(10L, result.get(0)[1]);
            assertEquals(Gender.FEMALE, result.get(1)[0]);
            assertEquals(15L, result.get(1)[1]);
            verify(patientRepository).countPatientsByGender();
        }

        @Test
        @DisplayName("Should return patient statistics by age group")
        void shouldReturnPatientStatisticsByAgeGroup() {
            // Arrange
            int interval = 10;
            List<Object[]> stats = Arrays.asList(
                    new Object[]{"0-10", 5L},
                    new Object[]{"11-20", 8L},
                    new Object[]{"21-30", 12L}
            );
            
            when(patientRepository.countPatientsByAgeGroup(interval)).thenReturn(stats);

            // Act
            List<Object[]> result = queryService.getPatientStatsByAgeGroup(interval);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals("0-10", result.get(0)[0]);
            assertEquals(5L, result.get(0)[1]);
            assertEquals("11-20", result.get(1)[0]);
            assertEquals(8L, result.get(1)[1]);
            assertEquals("21-30", result.get(2)[0]);
            assertEquals(12L, result.get(2)[1]);
            verify(patientRepository).countPatientsByAgeGroup(interval);
        }
    }
}