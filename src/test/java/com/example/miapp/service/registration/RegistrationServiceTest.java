package com.example.miapp.service.registration;

import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.dto.user.UserDto;
import com.example.miapp.entity.*;
import com.example.miapp.mapper.UserMapper;
import com.example.miapp.repository.*;
import com.example.miapp.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    @Mock
    private AuthService authService;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private SpecialtyRepository specialtyRepository;
    
    @Mock
    private DoctorSpecialtyRepository doctorSpecialtyRepository;
    
    @InjectMocks
    private RegistrationService registrationService;
    
    private User testUser;
    private UserDto testUserDto;
    private CreatePatientRequest patientRequest;
    private CreateDoctorRequest doctorRequest;
    private Specialty testSpecialty;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configurar usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setFirstLogin(true);
        Set<Role> roles = new HashSet<>();
        testUser.setRoles(roles);
        
        // Configurar DTO de usuario
        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setStatus(User.UserStatus.ACTIVE);
        testUserDto.setFirstLogin(true);
        testUserDto.setRoles(new HashSet<>());
        
        // Configurar request de paciente
        patientRequest = new CreatePatientRequest();
        patientRequest.setUsername("patient1");
        patientRequest.setEmail("patient1@example.com");
        patientRequest.setPassword("password123");
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setPhone("1234567890");
        patientRequest.setAddress("123 Main St");
        patientRequest.setGender(Patient.Gender.MALE);
        
        // Configurar request de doctor
        doctorRequest = new CreateDoctorRequest();
        doctorRequest.setUsername("doctor1");
        doctorRequest.setEmail("doctor1@example.com");
        doctorRequest.setPassword("password123");
        doctorRequest.setFirstName("Jane");
        doctorRequest.setLastName("Smith");
        doctorRequest.setPhone("9876543210");
        doctorRequest.setLicenseNumber("MED12345");
        doctorRequest.setBiography("Experienced doctor");
        doctorRequest.setConsultationFee(100.0);
        
        // Configurar especialidad de prueba
        testSpecialty = new Specialty();
        testSpecialty.setId(1L);
        testSpecialty.setName("Cardiology");
        testSpecialty.setDescription("Heart specialist");
        
        // Configuración común para los mocks
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(i -> i.getArgument(0));
        when(patientRepository.save(any(Patient.class))).thenAnswer(i -> i.getArgument(0));
        when(doctorSpecialtyRepository.save(any(DoctorSpecialty.class))).thenAnswer(i -> i.getArgument(0));
    }
    
    @Nested
    @DisplayName("Admin Registration Tests")
    class AdminRegistrationTests {
        
        @Test
        @DisplayName("Should successfully register an admin")
        void registerAdmin_Success() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            // Act
            UserDto result = registrationService.registerAdmin("admin1", "admin@example.com", "password123");
            
            // Assert
            assertNotNull(result);
            assertEquals(testUserDto.getId(), result.getId());
            assertEquals(testUserDto.getUsername(), result.getUsername());
            
            // Verify
            verify(authService).registerUser(
                    eq("admin1"), 
                    eq("admin@example.com"), 
                    eq("password123"), 
                    argThat(roles -> roles.contains("admin"))
            );
            verify(userMapper).toDto(testUser);
        }
        
        @Test
        @DisplayName("Should handle exceptions from AuthService")
        void registerAdmin_AuthServiceException() {
            // Arrange
            String errorMessage = "Username already exists";
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenThrow(new RuntimeException(errorMessage));
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                registrationService.registerAdmin("admin1", "admin@example.com", "password123")
            );
            
            assertEquals(errorMessage, exception.getMessage());
            
            // Verify
            verify(authService).registerUser(anyString(), anyString(), anyString(), anySet());
            verify(userMapper, never()).toDto(any());
        }
    }
    
    @Nested
    @DisplayName("Patient Registration Tests")
    class PatientRegistrationTests {
        
        @Test
        @DisplayName("Should successfully register a patient")
        void registerPatient_Success() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            // Act
            UserDto result = registrationService.registerPatient(patientRequest);
            
            // Assert
            assertNotNull(result);
            assertEquals(testUserDto.getId(), result.getId());
            
            // Verify
            verify(authService).registerUser(
                    eq(patientRequest.getUsername()), 
                    eq(patientRequest.getEmail()), 
                    eq(patientRequest.getPassword()), 
                    argThat(roles -> roles.contains("patient"))
            );
            verify(patientRepository).save(argThat(patient -> 
                patient.getFirstName().equals(patientRequest.getFirstName()) &&
                patient.getLastName().equals(patientRequest.getLastName()) &&
                patient.getPhone().equals(patientRequest.getPhone()) &&
                patient.getUser() == testUser
            ));
            verify(userMapper).toDto(testUser);
        }
        
        @Test
        @DisplayName("Should handle exceptions from AuthService during patient registration")
        void registerPatient_AuthServiceException() {
            // Arrange
            String errorMessage = "Email already in use";
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenThrow(new RuntimeException(errorMessage));
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                registrationService.registerPatient(patientRequest)
            );
            
            assertEquals(errorMessage, exception.getMessage());
            
            // Verify
            verify(patientRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should handle exceptions from PatientRepository")
        void registerPatient_RepositoryException() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            String errorMessage = "Database error";
            when(patientRepository.save(any(Patient.class)))
                .thenThrow(new RuntimeException(errorMessage));
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                registrationService.registerPatient(patientRequest)
            );
            
            assertEquals(errorMessage, exception.getMessage());
            
            // Verify transaction rollback would occur due to @Transactional
        }
    }
    
    @Nested
    @DisplayName("Doctor Registration Tests")
    class DoctorRegistrationTests {
        
        @Test
        @DisplayName("Should successfully register a doctor with specialties")
        void registerDoctor_Success() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            when(specialtyRepository.findById(anyLong()))
                .thenReturn(Optional.of(testSpecialty));
            
            Set<Long> specialtyIds = new HashSet<>();
            specialtyIds.add(1L);
            
            // Act
            UserDto result = registrationService.registerDoctor(doctorRequest, specialtyIds);
            
            // Assert
            assertNotNull(result);
            assertEquals(testUserDto.getId(), result.getId());
            
            // Verify
            verify(authService).registerUser(
                    eq(doctorRequest.getUsername()), 
                    eq(doctorRequest.getEmail()), 
                    eq(doctorRequest.getPassword()), 
                    argThat(roles -> roles.contains("doctor"))
            );
            
            verify(doctorRepository).save(argThat(doctor -> 
                doctor.getFirstName().equals(doctorRequest.getFirstName()) &&
                doctor.getLastName().equals(doctorRequest.getLastName()) &&
                doctor.getLicenseNumber().equals(doctorRequest.getLicenseNumber()) &&
                doctor.getUser() == testUser
            ));
            
            verify(specialtyRepository).findById(1L);
            
            verify(doctorSpecialtyRepository).save(argThat(ds ->
                ds.getDoctor() != null &&
                ds.getSpecialty() == testSpecialty &&
                "Junior".equals(ds.getExperienceLevel())
            ));
            
            verify(userMapper).toDto(testUser);
        }
        
        @Test
        @DisplayName("Should throw exception when registering doctor without specialties")
        void registerDoctor_NoSpecialties() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                registrationService.registerDoctor(doctorRequest, Collections.emptySet())
            );
            
            assertTrue(exception.getMessage().contains("especialidad"));
            
            // Verify
            verify(authService, never()).registerUser(anyString(), anyString(), anyString(), anySet());
            verify(doctorRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Should throw exception when specialty not found")
        void registerDoctor_SpecialtyNotFound() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            when(specialtyRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
            
            Set<Long> specialtyIds = new HashSet<>();
            specialtyIds.add(999L);
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                registrationService.registerDoctor(doctorRequest, specialtyIds)
            );
            
            assertTrue(exception.getMessage().contains("Especialidad no encontrada"));
            
            // Verify
            verify(authService).registerUser(anyString(), anyString(), anyString(), anySet());
            verify(doctorRepository).save(any());
            verify(specialtyRepository).findById(999L);
            verify(doctorSpecialtyRepository, never()).save(any());
            
            // Due to @Transactional, a rollback would occur in real execution
        }
        
        @Test
        @DisplayName("Should register doctor with multiple specialties")
        void registerDoctor_MultipleSpecialties() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            Specialty specialty1 = new Specialty();
            specialty1.setId(1L);
            specialty1.setName("Cardiology");
            
            Specialty specialty2 = new Specialty();
            specialty2.setId(2L);
            specialty2.setName("Neurology");
            
            when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty1));
            when(specialtyRepository.findById(2L)).thenReturn(Optional.of(specialty2));
            
            Set<Long> specialtyIds = new HashSet<>();
            specialtyIds.add(1L);
            specialtyIds.add(2L);
            
            // Act
            UserDto result = registrationService.registerDoctor(doctorRequest, specialtyIds);
            
            // Assert
            assertNotNull(result);
            
            // Verify
            verify(specialtyRepository).findById(1L);
            verify(specialtyRepository).findById(2L);
            verify(doctorSpecialtyRepository, times(2)).save(any());
        }
        
        @Test
        @DisplayName("Should handle exceptions from AuthService during doctor registration")
        void registerDoctor_AuthServiceException() {
            // Arrange
            String errorMessage = "Username already exists";
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenThrow(new RuntimeException(errorMessage));
            
            Set<Long> specialtyIds = new HashSet<>();
            specialtyIds.add(1L);
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                registrationService.registerDoctor(doctorRequest, specialtyIds)
            );
            
            assertEquals(errorMessage, exception.getMessage());
            
            // Verify
            verify(doctorRepository, never()).save(any());
            verify(specialtyRepository, never()).findById(anyLong());
            verify(doctorSpecialtyRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        
        @Test
        @DisplayName("Should handle null values in patient request")
        void registerPatient_WithNullValues() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            CreatePatientRequest request = new CreatePatientRequest();
            request.setUsername("patient1");
            request.setEmail("patient1@example.com");
            request.setPassword("password123");
            request.setFirstName("John");
            request.setLastName("Doe");
            // Other fields are null
            
            // Act
            UserDto result = registrationService.registerPatient(request);
            
            // Assert
            assertNotNull(result);
            
            // Verify
            verify(patientRepository).save(argThat(patient -> 
                patient.getFirstName().equals(request.getFirstName()) &&
                patient.getLastName().equals(request.getLastName()) &&
                patient.getGender() == null &&
                patient.getBloodType() == null
            ));
        }
        
        @Test
        @DisplayName("Should create doctor with default consultation fee if null")
        void registerDoctor_WithNullConsultationFee() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            when(specialtyRepository.findById(anyLong()))
                .thenReturn(Optional.of(testSpecialty));
            
            CreateDoctorRequest request = new CreateDoctorRequest();
            request.setUsername("doctor1");
            request.setEmail("doctor1@example.com");
            request.setPassword("password123");
            request.setFirstName("Jane");
            request.setLastName("Smith");
            request.setPhone("9876543210");
            request.setLicenseNumber("MED12345");
            request.setConsultationFee(null); // Null consultation fee
            
            Set<Long> specialtyIds = new HashSet<>();
            specialtyIds.add(1L);
            
            // Act
            UserDto result = registrationService.registerDoctor(request, specialtyIds);
            
            // Assert
            assertNotNull(result);
            
            // Verify
            verify(doctorRepository).save(argThat(doctor -> 
                doctor.getFirstName().equals(request.getFirstName()) &&
                doctor.getLastName().equals(request.getLastName()) &&
                Double.compare(doctor.getConsultationFee(), 0.0) == 0 
            ));
        }
    }
}