package com.example.miapp.service.doctor;

import com.example.miapp.dto.doctor.DoctorDto;
import com.example.miapp.dto.doctor.DoctorSearchCriteria;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSpecialty;
import com.example.miapp.entity.Specialty;
import com.example.miapp.entity.User;
import com.example.miapp.mapper.DoctorMapper;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.DoctorRepository.DoctorBasicInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // Añadir esta línea para permitir stubbings no utilizados
class DoctorQueryServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorQueryService doctorQueryService;

    private Doctor doctor;
    private DoctorDto doctorDto;
    private Pageable pageable;
    private Page<Doctor> doctorPage;
    private Specialty specialty;
    private DoctorSpecialty doctorSpecialty;
    private DoctorSearchCriteria searchCriteria;
    private User user;
    private List<Long> specialtyIds;
    private DoctorBasicInfo doctorBasicInfo;

    @BeforeEach
    void setUp() {
        // Initialize common test objects
        user = new User();
        user.setId(1L);
        user.setUsername("jsmith");
        
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFirstName("John");
        doctor.setLastName("Smith");
        doctor.setEmail("john.smith@example.com");
        doctor.setLicenseNumber("MED12345");
        doctor.setConsultationFee(100.0);
        doctor.setUser(user);
        
        doctorDto = new DoctorDto();
        doctorDto.setId(1L);
        doctorDto.setFirstName("John");
        doctorDto.setLastName("Smith");
        doctorDto.setFullName("John Smith");
        doctorDto.setEmail("john.smith@example.com");
        doctorDto.setLicenseNumber("MED12345");
        
        // Create specialty and doctor specialty for tests that need them
        specialty = new Specialty();
        specialty.setId(1L);
        specialty.setName("Cardiology");
        
        doctorSpecialty = new DoctorSpecialty();
        doctorSpecialty.setId(1L);
        doctorSpecialty.setDoctor(doctor);
        doctorSpecialty.setSpecialty(specialty);
        doctorSpecialty.setExperienceLevel("EXPERT");
        
        // Add specialty to doctor
        doctor.setDoctorSpecialties(new ArrayList<>(Collections.singletonList(doctorSpecialty)));
        
        // Create search criteria
        searchCriteria = new DoctorSearchCriteria();
        searchCriteria.setName("Smith");
        searchCriteria.setSpecialtyId(1L);
        searchCriteria.setDayOfWeek(DayOfWeek.MONDAY);
        searchCriteria.setMinFee(50.0);
        searchCriteria.setMaxFee(200.0);
        
        // Specialty IDs for multi-specialty tests
        specialtyIds = Arrays.asList(1L, 2L);
        
        pageable = PageRequest.of(0, 10);
        doctorPage = new PageImpl<>(Collections.singletonList(doctor), pageable, 1);
        
        // Mock basic info for repository projection tests
        doctorBasicInfo = mock(DoctorBasicInfo.class);
        when(doctorBasicInfo.getId()).thenReturn(1L);
        when(doctorBasicInfo.getFirstName()).thenReturn("John");
        when(doctorBasicInfo.getLastName()).thenReturn("Smith");
        when(doctorBasicInfo.getEmail()).thenReturn("john.smith@example.com");
        when(doctorBasicInfo.getPhone()).thenReturn("123-456-7890");
        when(doctorBasicInfo.getConsultationFee()).thenReturn(100.0);
        
        // Configure mapper to return doctorDto for all tests
        when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
    }

    @Nested
    @DisplayName("Basic Entity Retrieval Tests")
    class BasicEntityRetrievalTests {
        
        @Test
        @DisplayName("getDoctor should return DTO when doctor exists")
        void getDoctor_WhenDoctorExists_ReturnsDto() {
            // Arrange
            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            DoctorDto result = doctorQueryService.getDoctor(1L);
            
            // Assert
            assertEquals(doctorDto, result, "Should return the expected DTO");
            verify(doctorRepository).findById(1L);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorById should return entity when doctor exists")
        void findDoctorById_WhenDoctorExists_ReturnsEntity() {
            // Arrange
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            
            // Act
            Doctor result = doctorQueryService.findDoctorById(1L);
            
            // Assert
            assertSame(doctor, result, "Should return the same doctor entity");
            verify(doctorRepository).findById(1L);
        }
        
        @Test
        @DisplayName("findDoctorById should throw exception when doctor doesn't exist")
        void findDoctorById_WhenDoctorDoesNotExist_ThrowsException() {
            // Arrange
            when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                    () -> doctorQueryService.findDoctorById(1L));
            assertEquals("Doctor not found with ID: 1", exception.getMessage());
            verify(doctorRepository).findById(1L);
        }
    }
    
    @Nested
    @DisplayName("Optional Entity Retrieval Tests")
    class OptionalEntityRetrievalTests {
        
        @Test
        @DisplayName("findDoctorByEmail should return Optional DTO when doctor exists")
        void findDoctorByEmail_WhenDoctorExists_ReturnsOptionalDto() {
            // Arrange
            String email = "john.smith@example.com";
            when(doctorRepository.findByEmail(email)).thenReturn(Optional.of(doctor));
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Optional<DoctorDto> result = doctorQueryService.findDoctorByEmail(email);
            
            // Assert
            assertTrue(result.isPresent(), "Result should be present");
            assertEquals(doctorDto, result.get(), "Should contain the expected DTO");
            verify(doctorRepository).findByEmail(email);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorByEmail should return empty Optional when doctor doesn't exist")
        void findDoctorByEmail_WhenDoctorDoesNotExist_ReturnsEmptyOptional() {
            // Arrange
            String email = "nonexistent@example.com";
            when(doctorRepository.findByEmail(email)).thenReturn(Optional.empty());
            
            // Act
            Optional<DoctorDto> result = doctorQueryService.findDoctorByEmail(email);
            
            // Assert
            assertTrue(result.isEmpty(), "Result should be empty");
            verify(doctorRepository).findByEmail(email);
            verify(doctorMapper, never()).toDto(any(Doctor.class));
        }
        
        @Test
        @DisplayName("findDoctorByLicenseNumber should return Optional DTO when doctor exists")
        void findDoctorByLicenseNumber_WhenDoctorExists_ReturnsOptionalDto() {
            // Arrange
            String licenseNumber = "MED12345";
            when(doctorRepository.findByLicenseNumber(licenseNumber)).thenReturn(Optional.of(doctor));
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Optional<DoctorDto> result = doctorQueryService.findDoctorByLicenseNumber(licenseNumber);
            
            // Assert
            assertTrue(result.isPresent(), "Result should be present");
            assertEquals(doctorDto, result.get(), "Should contain the expected DTO");
            verify(doctorRepository).findByLicenseNumber(licenseNumber);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorByLicenseNumber should return empty Optional when doctor doesn't exist")
        void findDoctorByLicenseNumber_WhenDoctorDoesNotExist_ReturnsEmptyOptional() {
            // Arrange
            String licenseNumber = "INVALID123";
            when(doctorRepository.findByLicenseNumber(licenseNumber)).thenReturn(Optional.empty());
            
            // Act
            Optional<DoctorDto> result = doctorQueryService.findDoctorByLicenseNumber(licenseNumber);
            
            // Assert
            assertTrue(result.isEmpty(), "Result should be empty");
            verify(doctorRepository).findByLicenseNumber(licenseNumber);
            verify(doctorMapper, never()).toDto(any(Doctor.class));
        }
        
        @Test
        @DisplayName("findDoctorByUserId should return Optional DTO when doctor exists")
        void findDoctorByUserId_WhenDoctorExists_ReturnsOptionalDto() {
            // Arrange
            Long userId = 1L;
            when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Optional<DoctorDto> result = doctorQueryService.findDoctorByUserId(userId);
            
            // Assert
            assertTrue(result.isPresent(), "Result should be present");
            assertEquals(doctorDto, result.get(), "Should contain the expected DTO");
            verify(doctorRepository).findByUserId(userId);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorByUserId should return empty Optional when doctor doesn't exist")
        void findDoctorByUserId_WhenDoctorDoesNotExist_ReturnsEmptyOptional() {
            // Arrange
            Long userId = 999L;
            when(doctorRepository.findByUserId(userId)).thenReturn(Optional.empty());
            
            // Act
            Optional<DoctorDto> result = doctorQueryService.findDoctorByUserId(userId);
            
            // Assert
            assertTrue(result.isEmpty(), "Result should be empty");
            verify(doctorRepository).findByUserId(userId);
            verify(doctorMapper, never()).toDto(any(Doctor.class));
        }
    }
    
    @Nested
    @DisplayName("Search Operations Tests")
    class SearchOperationsTests {
        
        @Test
        @DisplayName("searchDoctorsByName should return page of DTOs")
        void searchDoctorsByName_ShouldReturnPageOfDtos() {
            // Arrange
            String namePattern = "Smith";
            when(doctorRepository.findByNameContaining(namePattern, pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.searchDoctorsByName(namePattern, pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findByNameContaining(namePattern, pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("searchDoctorsByName should return empty page when no doctors match")
        void searchDoctorsByName_WhenNoMatches_ReturnsEmptyPage() {
            // Arrange
            String namePattern = "NonexistentName";
            when(doctorRepository.findByNameContaining(namePattern, pageable))
                .thenReturn(Page.empty(pageable));
            
            // Act
            Page<DoctorDto> result = doctorQueryService.searchDoctorsByName(namePattern, pageable);
            
            // Assert
            assertTrue(result.isEmpty(), "Result should be empty");
            assertEquals(0, result.getTotalElements(), "Should have zero total elements");
            verify(doctorRepository).findByNameContaining(namePattern, pageable);
            verify(doctorMapper, never()).toDto(any(Doctor.class));
        }
        
        @Test
        @DisplayName("findDoctorsBySpecialty should return page of DTOs")
        void findDoctorsBySpecialty_ShouldReturnPageOfDtos() {
            // Arrange
            Long specialtyId = 1L;
            when(doctorRepository.findBySpecialtyId(specialtyId, pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findDoctorsBySpecialty(specialtyId, pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findBySpecialtyId(specialtyId, pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorsBySpecialties should return page of DTOs")
        void findDoctorsBySpecialties_ShouldReturnPageOfDtos() {
            // Arrange
            when(doctorRepository.findBySpecialtyIdIn(specialtyIds, pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findDoctorsBySpecialties(specialtyIds, pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findBySpecialtyIdIn(specialtyIds, pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findAvailableDoctors should return page of DTOs")
        void findAvailableDoctors_ShouldReturnPageOfDtos() {
            // Arrange
            DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(17, 0);
            
            when(doctorRepository.findAvailableDoctors(dayOfWeek, startTime, endTime, pageable))
                    .thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findAvailableDoctors(dayOfWeek, startTime, endTime, pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findAvailableDoctors(dayOfWeek, startTime, endTime, pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorsWithAppointmentsInRange should return page of DTOs")
        void findDoctorsWithAppointmentsInRange_ShouldReturnPageOfDtos() {
            // Arrange
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(7);
            
            when(doctorRepository.findWithAppointmentsInRange(startDate, endDate, pageable))
                    .thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findDoctorsWithAppointmentsInRange(startDate, endDate, pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findWithAppointmentsInRange(startDate, endDate, pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("searchDoctors should return page of DTOs using criteria object")
        void searchDoctors_ShouldReturnPageOfDtos() {
            // Arrange - Using the searchCriteria object created in setUp
            when(doctorRepository.searchDoctors(
                    searchCriteria.getName(),
                    searchCriteria.getSpecialtyId(),
                    searchCriteria.getDayOfWeek(),
                    searchCriteria.getMinFee(),
                    searchCriteria.getMaxFee(),
                    pageable))
                    .thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.searchDoctors(searchCriteria, pageable);
            
            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(doctorDto);
            verify(doctorRepository).searchDoctors(
                    searchCriteria.getName(),
                    searchCriteria.getSpecialtyId(),
                    searchCriteria.getDayOfWeek(),
                    searchCriteria.getMinFee(),
                    searchCriteria.getMaxFee(),
                    pageable);
            verify(doctorMapper).toDto(doctor);
        }
    }
    
    @Nested
    @DisplayName("Fee-Based Search Tests")
    class FeeBasedSearchTests {
        
        @Test
        @DisplayName("findDoctorsByConsultationFeeRange should return page of DTOs")
        void findDoctorsByConsultationFeeRange_ShouldReturnPageOfDtos() {
            // Arrange
            Double minFee = 50.0;
            Double maxFee = 150.0;
            when(doctorRepository.findByConsultationFeeBetween(minFee, maxFee, pageable))
                    .thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findDoctorsByConsultationFeeRange(minFee, maxFee, pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findByConsultationFeeBetween(minFee, maxFee, pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorsByHighestFees should return page of DTOs")
        void findDoctorsByHighestFees_ShouldReturnPageOfDtos() {
            // Arrange
            when(doctorRepository.findByHighestConsultationFee(pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findDoctorsByHighestFees(pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findByHighestConsultationFee(pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorsByLowestFees should return page of DTOs")
        void findDoctorsByLowestFees_ShouldReturnPageOfDtos() {
            // Arrange
            when(doctorRepository.findByLowestConsultationFee(pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findDoctorsByLowestFees(pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findByLowestConsultationFee(pageable);
            verify(doctorMapper).toDto(doctor);
        }
    }
    
    @Nested
    @DisplayName("Experience and Specialty Tests")
    class ExperienceAndSpecialtyTests {
        
        @Test
        @DisplayName("findDoctorsByExperienceLevel should return page of DTOs")
        void findDoctorsByExperienceLevel_ShouldReturnPageOfDtos() {
            // Arrange
            String experienceLevel = "EXPERT";
            when(doctorRepository.findByExperienceLevel(experienceLevel, pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findDoctorsByExperienceLevel(experienceLevel, pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findByExperienceLevel(experienceLevel, pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorsWithMinimumSpecialties should return page of DTOs")
        void findDoctorsWithMinimumSpecialties_ShouldReturnPageOfDtos() {
            // Arrange
            long specialtyCount = 2;
            when(doctorRepository.findDoctorsWithMinimumSpecialties(specialtyCount, pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findDoctorsWithMinimumSpecialties(specialtyCount, pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findDoctorsWithMinimumSpecialties(specialtyCount, pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("findDoctorsWithoutSpecialty should return page of DTOs")
        void findDoctorsWithoutSpecialty_ShouldReturnPageOfDtos() {
            // Arrange
            Long specialtyId = 2L;
            when(doctorRepository.findDoctorsWithoutSpecialty(specialtyId, pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.findDoctorsWithoutSpecialty(specialtyId, pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertEquals(doctorDto, result.getContent().get(0), "Should contain the expected DTO");
            verify(doctorRepository).findDoctorsWithoutSpecialty(specialtyId, pageable);
            verify(doctorMapper).toDto(doctor);
        }
    }
    
    @Nested
    @DisplayName("Statistics and List Operations Tests")
    class StatisticsAndListOperationsTests {
        
        @Test
        @DisplayName("getAppointmentStatsByDoctor should return list of statistics")
        void getAppointmentStatsByDoctor_ShouldReturnListOfStatistics() {
            // Arrange
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusMonths(1);
            List<Object[]> stats = new ArrayList<>();
            Object[] statRow = new Object[]{1L, "John", "Smith", 10L};
            stats.add(statRow);
            
            when(doctorRepository.countAppointmentsByDoctor(startDate, endDate)).thenReturn(stats);
            
            // Act
            List<Object[]> result = doctorQueryService.getAppointmentStatsByDoctor(startDate, endDate);
            
            // Assert
            assertEquals(1, result.size(), "Should have one result row");
            assertSame(statRow, result.get(0), "Should be the same row object");
            assertEquals(10L, result.get(0)[3], "Should have expected appointment count");
            verify(doctorRepository).countAppointmentsByDoctor(startDate, endDate);
        }
        
        @Test
        @DisplayName("getAllDoctors should return page of DTOs")
        void getAllDoctors_ShouldReturnPageOfDtos() {
            // Arrange
            when(doctorRepository.findAll(pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.getAllDoctors(pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertThat(result.getContent()).containsExactly(doctorDto);
            verify(doctorRepository).findAll(pageable);
            verify(doctorMapper).toDto(doctor);
        }
        
        @Test
        @DisplayName("getAllDoctorsBasicInfo should return page of basic info projections")
        void getAllDoctorsBasicInfo_ShouldReturnPageOfBasicInfo() {
            // Arrange
            Page<DoctorBasicInfo> basicInfoPage = new PageImpl<>(
                    Collections.singletonList(doctorBasicInfo), pageable, 1);
            when(doctorRepository.findAllBasicInfo(pageable)).thenReturn(basicInfoPage);
            
            // Act
            Page<DoctorBasicInfo> result = doctorQueryService.getAllDoctorsBasicInfo(pageable);
            
            // Assert
            assertEquals(1, result.getContent().size(), "Should have one result");
            assertSame(doctorBasicInfo, result.getContent().get(0), "Should be the same basic info object");
            verify(doctorRepository).findAllBasicInfo(pageable);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandlingTests {
        
        @Test
        @DisplayName("All search methods should handle empty results properly")
        void allSearchMethods_WithEmptyResults_ShouldReturnEmptyPage() {
            // Arrange
            Page<Doctor> emptyPage = Page.empty(pageable);
            
            // Configure all repository methods to return empty pages
            when(doctorRepository.findAll(pageable)).thenReturn(emptyPage);
            when(doctorRepository.findByNameContaining(anyString(), any(Pageable.class))).thenReturn(emptyPage);
            when(doctorRepository.findBySpecialtyId(anyLong(), any(Pageable.class))).thenReturn(emptyPage);
            // Add other repository method stubs as needed
            
            // Act & Assert - Test multiple search methods
            assertTrue(doctorQueryService.getAllDoctors(pageable).isEmpty());
            assertTrue(doctorQueryService.searchDoctorsByName("NonExistent", pageable).isEmpty());
            assertTrue(doctorQueryService.findDoctorsBySpecialty(999L, pageable).isEmpty());
            
            // Verify interactions
            verify(doctorRepository).findAll(pageable);
            verify(doctorRepository).findByNameContaining("NonExistent", pageable);
            verify(doctorRepository).findBySpecialtyId(999L, pageable);
        }
        
        @Test
        @DisplayName("getAppointmentStatsByDoctor should handle empty statistics")
        void getAppointmentStatsByDoctor_WithEmptyStats_ShouldReturnEmptyList() {
            // Arrange
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusMonths(1);
            List<Object[]> emptyStats = Collections.emptyList();
            
            when(doctorRepository.countAppointmentsByDoctor(startDate, endDate)).thenReturn(emptyStats);
            
            // Act
            List<Object[]> result = doctorQueryService.getAppointmentStatsByDoctor(startDate, endDate);
            
            // Assert
            assertTrue(result.isEmpty(), "Result should be empty");
            verify(doctorRepository).countAppointmentsByDoctor(startDate, endDate);
        }
        
        @Test
        @DisplayName("searchDoctors should handle null criteria values")
        void searchDoctors_WithNullCriteriaValues_ShouldPassNullsToRepository() {
            // Arrange
            DoctorSearchCriteria emptyCriteria = new DoctorSearchCriteria();
            // All fields are null by default
            
            when(doctorRepository.searchDoctors(
                    null, null, null, null, null, pageable)).thenReturn(doctorPage);
            when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
            
            // Act
            Page<DoctorDto> result = doctorQueryService.searchDoctors(emptyCriteria, pageable);
            
            // Assert
            assertFalse(result.isEmpty(), "Result should not be empty");
            verify(doctorRepository).searchDoctors(null, null, null, null, null, pageable);
            verify(doctorMapper).toDto(doctor);
        }
    }
}