package com.example.miapp.service.doctor;

import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.dto.doctor.DoctorDto;
import com.example.miapp.dto.doctor.DoctorScheduleDto;
import com.example.miapp.dto.doctor.DoctorSearchCriteria;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSchedule;
import com.example.miapp.entity.DoctorSpecialty;
import com.example.miapp.entity.Role;
import com.example.miapp.entity.Specialty;
import com.example.miapp.entity.User;
import com.example.miapp.mapper.DoctorMapper;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.RoleRepository;
import com.example.miapp.repository.SpecialtyRepository;
import com.example.miapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorBusinessServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private SpecialtyRepository specialtyRepository;
    
    @Mock
    private DoctorMapper doctorMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private DoctorValidationService validationService;
    
    @Mock
    private DoctorQueryService queryService;
    
    @Mock
    private DoctorManagementService managementService;
    
    @Captor
    private ArgumentCaptor<Doctor> doctorCaptor;
    
    @InjectMocks
    private DoctorBusinessService doctorBusinessService;
    
    private CreateDoctorRequest createDoctorRequest;
    private Doctor doctor;
    private User user;
    private DoctorDto doctorDto;
    private Role doctorRole;
    private Specialty specialty;
    private Pageable pageable;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        createDoctorRequest = new CreateDoctorRequest();
        createDoctorRequest.setFirstName("John");
        createDoctorRequest.setLastName("Doe");
        createDoctorRequest.setEmail("john.doe@example.com");
        createDoctorRequest.setPhone("1234567890");
        createDoctorRequest.setLicenseNumber("MED12345");
        createDoctorRequest.setUsername("johndoe");
        createDoctorRequest.setPassword("password123");
        createDoctorRequest.setBiography("Experienced doctor");
        createDoctorRequest.setConsultationFee(100.0);
        createDoctorRequest.setSpecialtyIds(Set.of(1L));

        doctorRole = new Role();
        doctorRole.setId(1);
        doctorRole.setName(Role.ERole.ROLE_DOCTOR);

        user = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john.doe@example.com")
                .password("encoded_password")
                .status(User.UserStatus.ACTIVE)
                .roles(Set.of(doctorRole))
                .firstLogin(true)
                .build();

        specialty = new Specialty();
        specialty.setId(1L);
        specialty.setName("Cardiology");
        specialty.setDescription("Heart related issues");

        // Creamos y configuramos un Doctor que ya tiene una especialidad asociada
        List<DoctorSpecialty> specialties = new ArrayList<>();
        DoctorSpecialty doctorSpecialty = new DoctorSpecialty();
        doctorSpecialty.setId(1L);
        doctorSpecialty.setSpecialty(specialty);
        doctorSpecialty.setExperienceLevel("BEGINNER");
        doctorSpecialty.setCertificationDate(new Date());
        specialties.add(doctorSpecialty);

        doctor = Doctor.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .licenseNumber("MED12345")
                .biography("Experienced doctor")
                .consultationFee(100.0)
                .user(user)
                .workSchedules(new HashSet<>())
                .doctorSpecialties(specialties) // Ya tiene especialidades
                .build();

        // Establecer la relación bidireccional
        doctorSpecialty.setDoctor(doctor);

        doctorDto = new DoctorDto();
        doctorDto.setId(1L);
        doctorDto.setFirstName("John");
        doctorDto.setLastName("Doe");
        doctorDto.setFullName("John Doe");
        doctorDto.setEmail("john.doe@example.com");
        doctorDto.setPhone("1234567890");
        doctorDto.setLicenseNumber("MED12345");
        doctorDto.setBiography("Experienced doctor");
        doctorDto.setConsultationFee(100.0);
        doctorDto.setSpecialties(List.of("Cardiology"));
        
        pageable = Pageable.unpaged();
    }

    @Test
    void createDoctor_Success() {
        // Arrange
        when(roleRepository.findByName(Role.ERole.ROLE_DOCTOR)).thenReturn(Optional.of(doctorRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(specialtyRepository.findById(anyLong())).thenReturn(Optional.of(specialty));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toDto(any(Doctor.class))).thenReturn(doctorDto);

        // Act
        DoctorDto result = doctorBusinessService.createDoctor(createDoctorRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        
        // Verify interactions
        verify(validationService).validateDoctorCreation(createDoctorRequest);
        verify(roleRepository).findByName(Role.ERole.ROLE_DOCTOR);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(specialtyRepository).findById(1L);
        verify(doctorRepository).save(any(Doctor.class));
        verify(doctorMapper).toDto(any(Doctor.class));
    }

    @Test
    void createDoctor_WithEmptySpecialties() {
        // Arrange
        createDoctorRequest.setSpecialtyIds(null);
        
        when(roleRepository.findByName(Role.ERole.ROLE_DOCTOR)).thenReturn(Optional.of(doctorRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toDto(any(Doctor.class))).thenReturn(doctorDto);

        // Act
        DoctorDto result = doctorBusinessService.createDoctor(createDoctorRequest);

        // Assert
        assertNotNull(result);
        
        // Verify interactions
        verify(validationService).validateDoctorCreation(createDoctorRequest);
        verify(doctorRepository).save(any(Doctor.class));
        verify(doctorMapper).toDto(any(Doctor.class));
        verify(specialtyRepository, never()).findById(anyLong()); // No debería buscar especialidades
    }

    @Test
    void updateDoctor_FullUpdate() {
        // Arrange
        when(queryService.findDoctorById(1L)).thenReturn(doctor);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);
        when(specialtyRepository.findById(anyLong())).thenReturn(Optional.of(specialty));

        // Act
        DoctorDto result = doctorBusinessService.updateDoctor(1L, createDoctorRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        
        // Verify interactions
        verify(validationService).validateDoctorUpdate(1L, createDoctorRequest);
        verify(queryService).findDoctorById(1L);
        verify(doctorRepository).save(doctor);
        verify(doctorMapper).toDto(doctor);
        verify(specialtyRepository).findById(1L);
    }

    @Test
    void updateDoctor_WithoutSpecialties() {
        // Arrange
        when(queryService.findDoctorById(1L)).thenReturn(doctor);
        createDoctorRequest.setSpecialtyIds(null);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);

        // Act
        DoctorDto result = doctorBusinessService.updateDoctor(1L, createDoctorRequest);

        // Assert
        assertNotNull(result);
        
        // Verify interactions
        verify(validationService).validateDoctorUpdate(1L, createDoctorRequest);
        verify(queryService).findDoctorById(1L);
        verify(doctorRepository).save(doctor);
        verify(specialtyRepository, never()).findById(anyLong()); // No debería buscar especialidades
    }

    @Test
    void updateDoctor_PartialUpdate() {
        // Arrange
        when(queryService.findDoctorById(1L)).thenReturn(doctor);
        
        // Configurar solicitud con solo algunos campos
        CreateDoctorRequest partialRequest = new CreateDoctorRequest();
        partialRequest.setFirstName("Jane");
        partialRequest.setSpecialtyIds(null); // No actualizar especialidades
        
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toDto(doctor)).thenReturn(doctorDto);

        // Act
        DoctorDto result = doctorBusinessService.updateDoctor(1L, partialRequest);

        // Assert
        assertNotNull(result);
        
        // Verify el doctor capturado para confirmar que solo se actualizó el campo correcto
        verify(doctorRepository).save(doctorCaptor.capture());
        Doctor savedDoctor = doctorCaptor.getValue();
        assertEquals("Jane", savedDoctor.getFirstName());
        assertEquals("Doe", savedDoctor.getLastName()); // No debería cambiar
    }

    @Test
    void deleteDoctor_Success() {
        // Arrange
        when(queryService.findDoctorById(1L)).thenReturn(doctor);
        doNothing().when(validationService).validateDoctorDeletion(doctor);
        doNothing().when(managementService).deleteDoctor(1L);

        // Act
        doctorBusinessService.deleteDoctor(1L);

        // Verify interactions
        verify(queryService).findDoctorById(1L);
        verify(validationService).validateDoctorDeletion(doctor);
        verify(managementService).deleteDoctor(1L);
    }

    @Test
    void getDoctor_Success() {
        // Arrange
        when(queryService.getDoctor(1L)).thenReturn(doctorDto);

        // Act
        DoctorDto result = doctorBusinessService.getDoctor(1L);

        // Assert
        assertNotNull(result);
        assertEquals(doctorDto, result);
        
        // Verify delegation
        verify(queryService).getDoctor(1L);
    }

    @Test
    void findDoctorById_Success() {
        // Arrange
        when(queryService.findDoctorById(1L)).thenReturn(doctor);

        // Act
        Doctor result = doctorBusinessService.findDoctorById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(doctor, result);
        
        // Verify delegation
        verify(queryService).findDoctorById(1L);
    }

    @Test
    void findDoctorByEmail_Success() {
        // Arrange
        when(queryService.findDoctorByEmail("test@example.com")).thenReturn(Optional.of(doctorDto));

        // Act
        Optional<DoctorDto> result = doctorBusinessService.findDoctorByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        
        // Verify delegation
        verify(queryService).findDoctorByEmail("test@example.com");
    }

    @Test
    void searchDoctorsByName_Success() {
        // Arrange
        Page<DoctorDto> doctorPage = new PageImpl<>(List.of(doctorDto));
        when(queryService.searchDoctorsByName("Doe", pageable)).thenReturn(doctorPage);

        // Act
        Page<DoctorDto> result = doctorBusinessService.searchDoctorsByName("Doe", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        // Verify delegation
        verify(queryService).searchDoctorsByName("Doe", pageable);
    }

    @Test
    void findDoctorsBySpecialty_Success() {
        // Arrange
        Page<DoctorDto> doctorPage = new PageImpl<>(List.of(doctorDto));
        when(queryService.findDoctorsBySpecialty(1L, pageable)).thenReturn(doctorPage);

        // Act
        Page<DoctorDto> result = doctorBusinessService.findDoctorsBySpecialty(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        // Verify delegation
        verify(queryService).findDoctorsBySpecialty(1L, pageable);
    }

    @Test
    void findAvailableDoctors_Success() {
        // Arrange
        Page<DoctorDto> doctorPage = new PageImpl<>(List.of(doctorDto));
        DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        
        when(queryService.findAvailableDoctors(dayOfWeek, startTime, endTime, pageable))
            .thenReturn(doctorPage);

        // Act
        Page<DoctorDto> result = doctorBusinessService.findAvailableDoctors(
            dayOfWeek, startTime, endTime, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        // Verify delegation
        verify(queryService).findAvailableDoctors(dayOfWeek, startTime, endTime, pageable);
    }

    @Test
    void searchDoctors_Success() {
        // Arrange
        Page<DoctorDto> doctorPage = new PageImpl<>(List.of(doctorDto));
        DoctorSearchCriteria criteria = new DoctorSearchCriteria();
        criteria.setName("Doe");
        
        when(queryService.searchDoctors(criteria, pageable)).thenReturn(doctorPage);

        // Act
        Page<DoctorDto> result = doctorBusinessService.searchDoctors(criteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        // Verify delegation
        verify(queryService).searchDoctors(criteria, pageable);
    }

    @Test
    void getAllDoctors_Success() {
        // Arrange
        Page<DoctorDto> doctorPage = new PageImpl<>(List.of(doctorDto));
        when(queryService.getAllDoctors(pageable)).thenReturn(doctorPage);

        // Act
        Page<DoctorDto> result = doctorBusinessService.getAllDoctors(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        // Verify delegation
        verify(queryService).getAllDoctors(pageable);
    }

    @Test
    void updateConsultationFee_Success() {
        // Arrange
        doNothing().when(validationService).validateConsultationFeeUpdate(anyDouble());
        doNothing().when(managementService).updateConsultationFee(anyLong(), anyDouble());

        // Act
        doctorBusinessService.updateConsultationFee(1L, 150.0);

        // Verify delegation
        verify(validationService).validateConsultationFeeUpdate(150.0);
        verify(managementService).updateConsultationFee(1L, 150.0);
    }

    @Test
    void updateBiography_Success() {
        // Arrange
        String newBiography = "Updated biography";
        doNothing().when(managementService).updateBiography(anyLong(), anyString());

        // Act
        doctorBusinessService.updateBiography(1L, newBiography);

        // Verify delegation
        verify(managementService).updateBiography(1L, newBiography);
    }

    @Test
    void addSpecialtyToDoctor_Success() {
        // Arrange
        Long doctorId = 1L;
        Long specialtyId = 2L;
        String experienceLevel = "EXPERT";
        Date certificationDate = new Date();
        
        doNothing().when(managementService).addSpecialtyToDoctor(
            anyLong(), anyLong(), anyString(), any(Date.class));

        // Act
        doctorBusinessService.addSpecialtyToDoctor(
            doctorId, specialtyId, experienceLevel, certificationDate);

        // Verify delegation
        verify(managementService).addSpecialtyToDoctor(
            doctorId, specialtyId, experienceLevel, certificationDate);
    }

    @Test
    void removeSpecialtyFromDoctor_Success() {
        // Arrange
        Long doctorId = 1L;
        Long specialtyId = 2L;
        
        doNothing().when(managementService).removeSpecialtyFromDoctor(anyLong(), anyLong());

        // Act
        doctorBusinessService.removeSpecialtyFromDoctor(doctorId, specialtyId);

        // Verify delegation
        verify(managementService).removeSpecialtyFromDoctor(doctorId, specialtyId);
    }

    @Test
    void addWorkSchedule_Success() {
        // Arrange
        DoctorScheduleDto scheduleDto = new DoctorScheduleDto();
        scheduleDto.setDayOfWeek(DayOfWeek.MONDAY);
        scheduleDto.setStartTime(LocalTime.of(9, 0));
        scheduleDto.setEndTime(LocalTime.of(17, 0));
        scheduleDto.setSlotDurationMinutes(30);
        scheduleDto.setActive(true);
        scheduleDto.setLocation("Room 101");
        
        doNothing().when(validationService).validateDoctorSchedule(any(DoctorSchedule.class));
        doNothing().when(managementService).addWorkSchedule(anyLong(), any(DoctorSchedule.class));

        // Act
        doctorBusinessService.addWorkSchedule(1L, scheduleDto);

        // Verify delegation
        verify(validationService).validateDoctorSchedule(any(DoctorSchedule.class));
        verify(managementService).addWorkSchedule(eq(1L), any(DoctorSchedule.class));
    }

    @Test
    void updateWorkSchedule_Success() {
        // Arrange
        DoctorScheduleDto scheduleDto = new DoctorScheduleDto();
        scheduleDto.setId(1L);
        scheduleDto.setDayOfWeek(DayOfWeek.MONDAY);
        scheduleDto.setStartTime(LocalTime.of(10, 0));
        scheduleDto.setEndTime(LocalTime.of(18, 0));
        scheduleDto.setSlotDurationMinutes(30);
        scheduleDto.setActive(true);
        scheduleDto.setLocation("Room 102");
        
        doNothing().when(validationService).validateDoctorSchedule(any(DoctorSchedule.class));
        doNothing().when(managementService).updateWorkSchedule(anyLong(), any(DoctorSchedule.class));

        // Act
        doctorBusinessService.updateWorkSchedule(1L, scheduleDto);

        // Verify delegation
        verify(validationService).validateDoctorSchedule(any(DoctorSchedule.class));
        verify(managementService).updateWorkSchedule(eq(1L), any(DoctorSchedule.class));
    }

    @Test
    void removeWorkSchedule_Success() {
        // Arrange
        Long doctorId = 1L;
        Long scheduleId = 3L;
        
        doNothing().when(managementService).removeWorkSchedule(anyLong(), anyLong());

        // Act
        doctorBusinessService.removeWorkSchedule(doctorId, scheduleId);

        // Verify delegation
        verify(managementService).removeWorkSchedule(doctorId, scheduleId);
    }

    @Test
void getAppointmentStatsByDoctor_Success() {
    // Arrange
    LocalDateTime startDate = LocalDateTime.now().minusDays(30);
    LocalDateTime endDate = LocalDateTime.now();
    
    // Crear correctamente una lista de Object[]
    Object[] stat = new Object[]{1L, "Doctor1", "Doe", 10L};
    List<Object[]> stats = new ArrayList<>();
    stats.add(stat);
    
    when(queryService.getAppointmentStatsByDoctor(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(stats);

    // Act
    List<Object[]> result = doctorBusinessService.getAppointmentStatsByDoctor(startDate, endDate);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    
    // Verify delegation
    verify(queryService).getAppointmentStatsByDoctor(startDate, endDate);
}

    @Test
    void findDoctorsByConsultationFeeRange_Success() {
        // Arrange
        Double minFee = 50.0;
        Double maxFee = 200.0;
        Page<DoctorDto> doctorPage = new PageImpl<>(List.of(doctorDto));
        
        when(queryService.findDoctorsByConsultationFeeRange(anyDouble(), anyDouble(), any(Pageable.class)))
            .thenReturn(doctorPage);

        // Act
        Page<DoctorDto> result = doctorBusinessService.findDoctorsByConsultationFeeRange(
            minFee, maxFee, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        // Verify delegation
        verify(queryService).findDoctorsByConsultationFeeRange(minFee, maxFee, pageable);
    }
}