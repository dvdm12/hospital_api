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
        testUser.setCc("1234567890"); // Nuevo campo cc
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
        testUserDto.setCc("1234567890"); // Nuevo campo cc
        testUserDto.setStatus(User.UserStatus.ACTIVE);
        testUserDto.setFirstLogin(true);
        testUserDto.setRoles(new HashSet<>());
        
        // Configurar request de paciente
        patientRequest = new CreatePatientRequest();
        patientRequest.setUsername("patient1");
        patientRequest.setEmail("patient1@example.com");
        patientRequest.setPassword("password123");
        patientRequest.setCc("1122334455"); // Nuevo campo cc
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
        doctorRequest.setCc("9988776655"); // Nuevo campo cc
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
        @DisplayName("Should successfully register an admin with CC")
        void registerAdmin_WithCC_Success() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            // Act
            UserDto result = registrationService.registerAdmin("admin1", "admin@example.com", "password123", "1234567890");
            
            // Assert
            assertNotNull(result);
            assertEquals(testUserDto.getId(), result.getId());
            assertEquals(testUserDto.getUsername(), result.getUsername());
            assertEquals(testUserDto.getCc(), result.getCc()); // Verificar cédula
            
            // Verify
            verify(authService).registerUser(
                    eq("admin1"), 
                    eq("admin@example.com"), 
                    eq("password123"), 
                    eq("1234567890"), // Verificar que se pasa la cédula
                    argThat(roles -> roles.contains("admin"))
            );
            verify(userMapper).toDto(testUser);
        }
        
        @Test
        @DisplayName("Should successfully register an admin without CC")
        void registerAdmin_WithoutCC_Success() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), isNull(), anySet()))
                .thenReturn(testUser);
            
            // Act
            UserDto result = registrationService.registerAdmin("admin1", "admin@example.com", "password123");
            
            // Assert
            assertNotNull(result);
            assertEquals(testUserDto.getId(), result.getId());
            
            // Verify
            verify(authService).registerUser(
                    eq("admin1"), 
                    eq("admin@example.com"), 
                    eq("password123"), 
                    isNull(), // CC debe ser null
                    argThat(roles -> roles.contains("admin"))
            );
            verify(userMapper).toDto(testUser);
        }
        
        @Test
        @DisplayName("Should handle exceptions from AuthService")
        void registerAdmin_AuthServiceException() {
            // Arrange
            String errorMessage = "Username already exists";
            when(authService.registerUser(anyString(), anyString(), anyString(), anyString(), anySet()))
                .thenThrow(new RuntimeException(errorMessage));
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                registrationService.registerAdmin("admin1", "admin@example.com", "password123", "1234567890")
            );
            
            assertEquals(errorMessage, exception.getMessage());
            
            // Verify
            verify(authService).registerUser(anyString(), anyString(), anyString(), anyString(), anySet());
            verify(userMapper, never()).toDto(any());
        }
    }
    
    @Nested
    @DisplayName("Patient Registration Tests")
    class PatientRegistrationTests {
        
        @Test
        @DisplayName("Should successfully register a patient with CC")
        void registerPatient_WithCC_Success() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anyString(), anySet()))
                .thenReturn(testUser);
            
            // Act
            UserDto result = registrationService.registerPatient(patientRequest);
            
            // Assert
            assertNotNull(result);
            assertEquals(testUserDto.getId(), result.getId());
            assertEquals(testUserDto.getCc(), result.getCc()); // Verificar cédula
            
            // Verify
            verify(authService).registerUser(
                    eq(patientRequest.getUsername()), 
                    eq(patientRequest.getEmail()), 
                    eq(patientRequest.getPassword()), 
                    eq(patientRequest.getCc()), // Verificar que se pasa la cédula
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
        @DisplayName("Should handle duplicate CC exception")
        void registerPatient_DuplicateCC() {
            // Arrange
            String errorMessage = "La cédula ya está registrada en el sistema";
            when(authService.registerUser(anyString(), anyString(), anyString(), anyString(), anySet()))
                .thenThrow(new RuntimeException(errorMessage));
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                registrationService.registerPatient(patientRequest)
            );
            
            assertEquals(errorMessage, exception.getMessage());
            
            // Verify
            verify(authService).registerUser(
                    eq(patientRequest.getUsername()), 
                    eq(patientRequest.getEmail()), 
                    eq(patientRequest.getPassword()), 
                    eq(patientRequest.getCc()), 
                    anySet()
            );
            verify(patientRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Doctor Registration Tests")
    class DoctorRegistrationTests {
        
        @Test
        @DisplayName("Should successfully register a doctor with CC")
        void registerDoctor_WithCC_Success() {
            // Arrange
            when(authService.registerUser(anyString(), anyString(), anyString(), anyString(), anySet()))
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
            assertEquals(testUserDto.getCc(), result.getCc()); // Verificar cédula
            
            // Verify
            verify(authService).registerUser(
                    eq(doctorRequest.getUsername()), 
                    eq(doctorRequest.getEmail()), 
                    eq(doctorRequest.getPassword()), 
                    eq(doctorRequest.getCc()), // Verificar que se pasa la cédula
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
        @DisplayName("Should handle duplicate CC exception")
        void registerDoctor_DuplicateCC() {
            // Arrange
            String errorMessage = "La cédula ya está registrada en el sistema";
            when(authService.registerUser(anyString(), anyString(), anyString(), anyString(), anySet()))
                .thenThrow(new RuntimeException(errorMessage));
            
            Set<Long> specialtyIds = new HashSet<>();
            specialtyIds.add(1L);
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                registrationService.registerDoctor(doctorRequest, specialtyIds)
            );
            
            assertEquals(errorMessage, exception.getMessage());
            
            // Verify
            verify(authService).registerUser(
                    eq(doctorRequest.getUsername()), 
                    eq(doctorRequest.getEmail()), 
                    eq(doctorRequest.getPassword()), 
                    eq(doctorRequest.getCc()), 
                    anySet()
            );
            verify(doctorRepository, never()).save(any());
            verify(doctorSpecialtyRepository, never()).save(any());
        }
    }
}