package com.example.miapp.repository;

import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSpecialty;
import com.example.miapp.entity.Specialty;
import com.example.miapp.entity.User;
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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorRepositoryTest {

    @Mock
    private DoctorRepository doctorRepository;

    private Doctor testDoctor;
    private Doctor testDoctor2;
    private User doctorUser;
    private Specialty cardiology;
    private Specialty neurology;
    private DoctorSpecialty doctorSpecialty;
    private List<Doctor> doctorList;
    private Page<Doctor> doctorPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Crear usuario para el doctor
        doctorUser = User.builder()
                .id(1L)
                .username("doctor1")
                .email("doctor1@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        // Crear especialidades
        cardiology = Specialty.builder()
                .id(1L)
                .name("Cardiology")
                .description("Heart and cardiovascular system")
                .build();
        
        neurology = Specialty.builder()
                .id(2L)
                .name("Neurology")
                .description("Brain and nervous system")
                .build();
        
        // Crear relación doctor-especialidad
        doctorSpecialty = DoctorSpecialty.builder()
                .id(1L)
                .experienceLevel("Senior")
                .certificationDate(new Date())
                .build();
        
        // Crear doctor de prueba
        testDoctor = Doctor.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("123456789")
                .licenseNumber("MED12345")
                .consultationFee(150.0)
                .biography("Experienced cardiologist with 15 years of practice")
                .user(doctorUser)
                .doctorSpecialties(List.of(doctorSpecialty))
                .build();
        
        // Establecer relación bidireccional
        doctorSpecialty.setDoctor(testDoctor);
        doctorSpecialty.setSpecialty(cardiology);
        
        // Crear segundo doctor
        testDoctor2 = Doctor.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("987654321")
                .licenseNumber("MED67890")
                .consultationFee(180.0)
                .biography("Neurologist specialized in pediatric cases")
                .user(User.builder().id(2L).username("doctor2").build())
                .build();

        DoctorSpecialty doctorSpecialty2 = DoctorSpecialty.builder()
            .id(2L)
            .experienceLevel("Junior")
            .certificationDate(new Date())
            .build();
    
        doctorSpecialty2.setDoctor(testDoctor2);
        doctorSpecialty2.setSpecialty(neurology);
    
        // Asignar especialidad al segundo doctor
        testDoctor2.setDoctorSpecialties(List.of(doctorSpecialty2));
        
        // Crear lista de doctores
        doctorList = new ArrayList<>();
        doctorList.add(testDoctor);
        doctorList.add(testDoctor2);
        
        // Configurar paginación
        pageable = PageRequest.of(0, 10);
        doctorPage = new PageImpl<>(doctorList, pageable, doctorList.size());
    }



    @Test
    @DisplayName("Debería encontrar doctores por especialidad de Neurología")
    void shouldFindByNeurologySpecialty() {
        // Given
        List<Doctor> neurologists = List.of(testDoctor2);
        Page<Doctor> neurologistsPage = new PageImpl<>(neurologists, pageable, neurologists.size());
        when(doctorRepository.findBySpecialtyId(2L, pageable)).thenReturn(neurologistsPage);
    
        // When
        Page<Doctor> result = doctorRepository.findBySpecialtyId(2L, pageable);
    
        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("Jane", result.getContent().get(0).getFirstName());
        verify(doctorRepository).findBySpecialtyId(2L, pageable);
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda básica")
    class BasicFindTests {
        
        @Test
        @DisplayName("Debería encontrar un doctor por email")
        void shouldFindByEmail() {
            // Given
            when(doctorRepository.findByEmail("john.smith@example.com")).thenReturn(Optional.of(testDoctor));
            
            // When
            Optional<Doctor> result = doctorRepository.findByEmail("john.smith@example.com");
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("john.smith@example.com", result.get().getEmail());
            verify(doctorRepository).findByEmail("john.smith@example.com");
        }
        
        @Test
        @DisplayName("Debería encontrar un doctor por número de licencia")
        void shouldFindByLicenseNumber() {
            // Given
            when(doctorRepository.findByLicenseNumber("MED12345")).thenReturn(Optional.of(testDoctor));
            
            // When
            Optional<Doctor> result = doctorRepository.findByLicenseNumber("MED12345");
            
            // Then
            assertTrue(result.isPresent());
            assertEquals("MED12345", result.get().getLicenseNumber());
            verify(doctorRepository).findByLicenseNumber("MED12345");
        }
        
        @Test
        @DisplayName("Debería encontrar un doctor por ID de usuario")
        void shouldFindByUserId() {
            // Given
            when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(testDoctor));
            
            // When
            Optional<Doctor> result = doctorRepository.findByUserId(1L);
            
            // Then
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getUser().getId());
            verify(doctorRepository).findByUserId(1L);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por nombre")
    class NameSearchTests {
        
        @Test
        @DisplayName("Debería encontrar doctores por nombre y apellido")
        void shouldFindByFirstNameAndLastName() {
            // Given
            List<Doctor> matchingDoctors = List.of(testDoctor);
            Page<Doctor> matchingPage = new PageImpl<>(matchingDoctors, pageable, matchingDoctors.size());
            when(doctorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("John", "Smith", pageable))
                    .thenReturn(matchingPage);
            
            // When
            Page<Doctor> result = doctorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
                    "John", "Smith", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("John", result.getContent().get(0).getFirstName());
            assertEquals("Smith", result.getContent().get(0).getLastName());
            verify(doctorRepository).findByFirstNameIgnoreCaseAndLastNameIgnoreCase("John", "Smith", pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar doctores por patrón de nombre")
        void shouldFindByNameContaining() {
            // Given
            when(doctorRepository.findByNameContaining("Jo", pageable)).thenReturn(doctorPage);
            
            // When
            Page<Doctor> result = doctorRepository.findByNameContaining("Jo", pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(doctorRepository).findByNameContaining("Jo", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por especialidad")
    class SpecialtySearchTests {
        
        @Test
        @DisplayName("Debería encontrar doctores por ID de especialidad")
        void shouldFindBySpecialtyId() {
            // Given
            List<Doctor> cardiologists = List.of(testDoctor);
            Page<Doctor> cardiologistsPage = new PageImpl<>(cardiologists, pageable, cardiologists.size());
            when(doctorRepository.findBySpecialtyId(1L, pageable)).thenReturn(cardiologistsPage);
            
            // When
            Page<Doctor> result = doctorRepository.findBySpecialtyId(1L, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("John", result.getContent().get(0).getFirstName());
            verify(doctorRepository).findBySpecialtyId(1L, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar doctores por múltiples IDs de especialidad")
        void shouldFindBySpecialtyIdIn() {
            // Given
            List<Long> specialtyIds = List.of(1L, 2L);
            when(doctorRepository.findBySpecialtyIdIn(specialtyIds, pageable)).thenReturn(doctorPage);
            
            // When
            Page<Doctor> result = doctorRepository.findBySpecialtyIdIn(specialtyIds, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(doctorRepository).findBySpecialtyIdIn(specialtyIds, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de operaciones de actualización")
    class UpdateOperationTests {
        
        @Test
        @DisplayName("Debería actualizar la tarifa de consulta")
        void shouldUpdateConsultationFee() {
            // Given
            when(doctorRepository.updateConsultationFee(1L, 200.0)).thenReturn(1);
            
            // When
            int affectedRows = doctorRepository.updateConsultationFee(1L, 200.0);
            
            // Then
            assertEquals(1, affectedRows);
            verify(doctorRepository).updateConsultationFee(1L, 200.0);
        }
        
        @Test
        @DisplayName("Debería actualizar la biografía")
        void shouldUpdateBiography() {
            // Given
            String newBio = "Updated biography with recent achievements";
            when(doctorRepository.updateBiography(1L, newBio)).thenReturn(1);
            
            // When
            int affectedRows = doctorRepository.updateBiography(1L, newBio);
            
            // Then
            assertEquals(1, affectedRows);
            verify(doctorRepository).updateBiography(1L, newBio);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por disponibilidad")
    class AvailabilitySearchTests {
        
        @Test
        @DisplayName("Debería encontrar doctores disponibles por día y hora")
        void shouldFindAvailableDoctors() {
            // Given
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(10, 0);
            
            when(doctorRepository.findAvailableDoctors(
                    DayOfWeek.MONDAY, startTime, endTime, pageable)).thenReturn(doctorPage);
            
            // When
            Page<Doctor> result = doctorRepository.findAvailableDoctors(
                    DayOfWeek.MONDAY, startTime, endTime, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(doctorRepository).findAvailableDoctors(DayOfWeek.MONDAY, startTime, endTime, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar doctores con citas en un rango de fechas")
        void shouldFindWithAppointmentsInRange() {
            // Given
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(7);
            
            when(doctorRepository.findWithAppointmentsInRange(startDate, endDate, pageable))
                    .thenReturn(doctorPage);
            
            // When
            Page<Doctor> result = doctorRepository.findWithAppointmentsInRange(
                    startDate, endDate, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(doctorRepository).findWithAppointmentsInRange(startDate, endDate, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de consultas estadísticas")
    class StatisticalQueryTests {
        
        @Test
        @DisplayName("Debería contar citas por doctor")
        void shouldCountAppointmentsByDoctor() {
            // Given
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(30);
            
            List<Object[]> appointmentCounts = List.of(
                    new Object[]{1L, "John", "Smith", 15L},
                    new Object[]{2L, "Jane", "Doe", 10L}
            );
            
            when(doctorRepository.countAppointmentsByDoctor(startDate, endDate))
                    .thenReturn(appointmentCounts);
            
            // When
            List<Object[]> result = doctorRepository.countAppointmentsByDoctor(startDate, endDate);
            
            // Then
            assertEquals(2, result.size());
            assertEquals(1L, result.get(0)[0]);  // ID
            assertEquals("John", result.get(0)[1]);  // firstName
            assertEquals("Smith", result.get(0)[2]);  // lastName
            assertEquals(15L, result.get(0)[3]);  // count
            verify(doctorRepository).countAppointmentsByDoctor(startDate, endDate);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por tarifa")
    class FeeSearchTests {
        
        @Test
        @DisplayName("Debería encontrar doctores con las tarifas más altas")
        void shouldFindByHighestConsultationFee() {
            // Given
            when(doctorRepository.findByHighestConsultationFee(pageable)).thenReturn(doctorPage);
            
            // When
            Page<Doctor> result = doctorRepository.findByHighestConsultationFee(pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(doctorRepository).findByHighestConsultationFee(pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar doctores con las tarifas más bajas")
        void shouldFindByLowestConsultationFee() {
            // Given
            when(doctorRepository.findByLowestConsultationFee(pageable)).thenReturn(doctorPage);
            
            // When
            Page<Doctor> result = doctorRepository.findByLowestConsultationFee(pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(doctorRepository).findByLowestConsultationFee(pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar doctores dentro de un rango de tarifas")
        void shouldFindByConsultationFeeBetween() {
            // Given
            Double minFee = 100.0;
            Double maxFee = 200.0;
            
            when(doctorRepository.findByConsultationFeeBetween(minFee, maxFee, pageable))
                    .thenReturn(doctorPage);
            
            // When
            Page<Doctor> result = doctorRepository.findByConsultationFeeBetween(
                    minFee, maxFee, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(doctorRepository).findByConsultationFeeBetween(minFee, maxFee, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por experiencia")
    class ExperienceSearchTests {
        
        @Test
        @DisplayName("Debería encontrar doctores por nivel de experiencia")
        void shouldFindByExperienceLevel() {
            // Given
            List<Doctor> seniorDoctors = List.of(testDoctor);
            Page<Doctor> seniorDoctorsPage = new PageImpl<>(seniorDoctors, pageable, seniorDoctors.size());
            
            when(doctorRepository.findByExperienceLevel("Senior", pageable))
                    .thenReturn(seniorDoctorsPage);
            
            // When
            Page<Doctor> result = doctorRepository.findByExperienceLevel("Senior", pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("John", result.getContent().get(0).getFirstName());
            verify(doctorRepository).findByExperienceLevel("Senior", pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda avanzada")
    class AdvancedSearchTests {
        
        @Test
        @DisplayName("Debería buscar doctores con múltiples criterios")
        void shouldSearchDoctors() {
            // Given
            String name = "Jo";
            Long specialtyId = 1L;
            DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
            Double minFee = 100.0;
            Double maxFee = 200.0;
            
            List<Doctor> matchingDoctors = List.of(testDoctor);
            Page<Doctor> matchingPage = new PageImpl<>(matchingDoctors, pageable, matchingDoctors.size());
            
            when(doctorRepository.searchDoctors(name, specialtyId, dayOfWeek, minFee, maxFee, pageable))
                    .thenReturn(matchingPage);
            
            // When
            Page<Doctor> result = doctorRepository.searchDoctors(
                    name, specialtyId, dayOfWeek, minFee, maxFee, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("John", result.getContent().get(0).getFirstName());
            verify(doctorRepository).searchDoctors(name, specialtyId, dayOfWeek, minFee, maxFee, pageable);
        }
        
        @Test
        @DisplayName("Debería buscar doctores con criterios parciales")
        void shouldSearchDoctorsWithPartialCriteria() {
            // Given
            when(doctorRepository.searchDoctors("Jo", null, null, null, null, pageable))
                    .thenReturn(doctorPage);
            
            // When
            Page<Doctor> result = doctorRepository.searchDoctors(
                    "Jo", null, null, null, null, pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            verify(doctorRepository).searchDoctors("Jo", null, null, null, null, pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de proyecciones")
    class ProjectionTests {
        
        @Test
        @DisplayName("Debería encontrar información básica de todos los doctores")
        void shouldFindAllBasicInfo() {
            // Given
            List<DoctorRepository.DoctorBasicInfo> basicInfoList = List.of(
                    new DoctorRepository.DoctorBasicInfo() {
                        @Override
                        public Long getId() {
                            return 1L;
                        }
                        
                        @Override
                        public String getFirstName() {
                            return "John";
                        }
                        
                        @Override
                        public String getLastName() {
                            return "Smith";
                        }
                        
                        @Override
                        public String getEmail() {
                            return "john.smith@example.com";
                        }
                        
                        @Override
                        public String getPhone() {
                            return "123456789";
                        }
                        
                        @Override
                        public Double getConsultationFee() {
                            return 150.0;
                        }
                    },
                    new DoctorRepository.DoctorBasicInfo() {
                        @Override
                        public Long getId() {
                            return 2L;
                        }
                        
                        @Override
                        public String getFirstName() {
                            return "Jane";
                        }
                        
                        @Override
                        public String getLastName() {
                            return "Doe";
                        }
                        
                        @Override
                        public String getEmail() {
                            return "jane.doe@example.com";
                        }
                        
                        @Override
                        public String getPhone() {
                            return "987654321";
                        }
                        
                        @Override
                        public Double getConsultationFee() {
                            return 180.0;
                        }
                    }
            );
            
            Page<DoctorRepository.DoctorBasicInfo> basicInfoPage = 
                    new PageImpl<>(basicInfoList, pageable, basicInfoList.size());
                    
            when(doctorRepository.findAllBasicInfo(pageable)).thenReturn(basicInfoPage);
            
            // When
            Page<DoctorRepository.DoctorBasicInfo> result = doctorRepository.findAllBasicInfo(pageable);
            
            // Then
            assertEquals(2, result.getTotalElements());
            assertEquals("John", result.getContent().get(0).getFirstName());
            assertEquals("Smith", result.getContent().get(0).getLastName());
            assertEquals(150.0, result.getContent().get(0).getConsultationFee());
            verify(doctorRepository).findAllBasicInfo(pageable);
        }
    }
    
    @Nested
    @DisplayName("Pruebas de búsqueda por número de especialidades")
    class SpecialtyCountTests {
        
        @Test
        @DisplayName("Debería encontrar doctores con un mínimo de especialidades")
        void shouldFindDoctorsWithMinimumSpecialties() {
            // Given
            List<Doctor> multiSpecialtyDoctors = List.of(testDoctor);
            Page<Doctor> multiSpecialtyPage = new PageImpl<>(multiSpecialtyDoctors, pageable, multiSpecialtyDoctors.size());
            
            when(doctorRepository.findDoctorsWithMinimumSpecialties(2L, pageable))
                    .thenReturn(multiSpecialtyPage);
            
            // When
            Page<Doctor> result = doctorRepository.findDoctorsWithMinimumSpecialties(2L, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("John", result.getContent().get(0).getFirstName());
            verify(doctorRepository).findDoctorsWithMinimumSpecialties(2L, pageable);
        }
        
        @Test
        @DisplayName("Debería encontrar doctores sin una especialidad específica")
        void shouldFindDoctorsWithoutSpecialty() {
            // Given
            List<Doctor> doctorsWithoutNeurology = List.of(testDoctor);
            Page<Doctor> doctorsWithoutNeurologyPage = new PageImpl<>(doctorsWithoutNeurology, pageable, doctorsWithoutNeurology.size());
            
            when(doctorRepository.findDoctorsWithoutSpecialty(2L, pageable))
                    .thenReturn(doctorsWithoutNeurologyPage);
            
            // When
            Page<Doctor> result = doctorRepository.findDoctorsWithoutSpecialty(2L, pageable);
            
            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals("John", result.getContent().get(0).getFirstName());
            verify(doctorRepository).findDoctorsWithoutSpecialty(2L, pageable);
        }
    }
}