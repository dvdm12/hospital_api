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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Main business service that orchestrates doctor operations (Facade Pattern)
 * Applies SOLID principles and Design Patterns
 * Modified to ensure transactional integrity for user and doctor creation
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DoctorBusinessService implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorMapper doctorMapper;
    private final PasswordEncoder passwordEncoder;

    // Composed services following Single Responsibility
    private final DoctorValidationService validationService;
    private final DoctorQueryService queryService;
    private final DoctorManagementService managementService;

    /**
     * Creates a new doctor with user account (Template Method Pattern)
     * Implementación mejorada para garantizar atomicidad
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public DoctorDto createDoctor(CreateDoctorRequest request) {
        log.info("Creating new doctor: {} {}", request.getFirstName(), request.getLastName());

        // Step 1: Validate request
        validationService.validateDoctorCreation(request);

        try {
            // Step 2: Crear usuario SIN guardarlo todavía
            User user = buildDoctorUser(request);
            
            // Step 3: Crear entidad doctor SIN guardarla todavía
            Doctor doctor = buildDoctorEntity(request, user);
            
            // Step 4: Agregar especialidades al doctor
            addSpecialtiesToDoctor(doctor, request.getSpecialtyIds());
            
            // Step 5: Primero guardamos el usuario
            User savedUser = userRepository.save(user);
            
            // Aseguramos que el doctor tenga la referencia al usuario guardado
            doctor.setUser(savedUser);
            
            // Step 6: Ahora guardamos el doctor (todo en la misma transacción)
            Doctor savedDoctor = doctorRepository.save(doctor);
            
            log.info("Successfully created doctor with ID: {}", savedDoctor.getId());
            return doctorMapper.toDto(savedDoctor);
            
        } catch (Exception e) {
            log.error("Error creating doctor: {}", e.getMessage());
            throw new RuntimeException("Failed to create doctor: " + e.getMessage(), e);
            // La anotación @Transactional se encargará de hacer rollback
        }
    }

    /**
     * Updates existing doctor information
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public DoctorDto updateDoctor(Long doctorId, CreateDoctorRequest request) {
        log.info("Updating doctor with ID: {}", doctorId);

        // Validate update
        validationService.validateDoctorUpdate(doctorId, request);

        try {
            // Get existing doctor
            Doctor doctor = queryService.findDoctorById(doctorId);

            // Update doctor fields
            updateDoctorFields(doctor, request);

            // Update specialties if provided
            if (request.getSpecialtyIds() != null) {
                updateDoctorSpecialties(doctor, request.getSpecialtyIds());
            }

            Doctor updatedDoctor = doctorRepository.save(doctor);

            log.info("Successfully updated doctor with ID: {}", doctorId);
            return doctorMapper.toDto(updatedDoctor);
        } catch (Exception e) {
            log.error("Error updating doctor with ID {}: {}", doctorId, e.getMessage());
            throw new RuntimeException("Failed to update doctor: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes doctor after validation
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteDoctor(Long doctorId) {
        log.info("Deleting doctor with ID: {}", doctorId);

        try {
            Doctor doctor = queryService.findDoctorById(doctorId);
            validationService.validateDoctorDeletion(doctor);
            managementService.deleteDoctor(doctorId);

            log.info("Successfully deleted doctor with ID: {}", doctorId);
        } catch (Exception e) {
            log.error("Error deleting doctor with ID {}: {}", doctorId, e.getMessage());
            throw new RuntimeException("Failed to delete doctor: " + e.getMessage(), e);
        }
    }

    // Query operations - delegate to query service

    @Override
    @Transactional(readOnly = true)
    public DoctorDto getDoctor(Long doctorId) {
        return queryService.getDoctor(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Doctor findDoctorById(Long doctorId) {
        return queryService.findDoctorById(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DoctorDto> findDoctorByEmail(String email) {
        return queryService.findDoctorByEmail(email);
    }

    /**
 * Implementación del método findDoctorByUserId en DoctorBusinessService
 */
@Override
@Transactional(readOnly = true)
public Optional<DoctorDto> findDoctorByUserId(Long userId) {
    log.info("Buscando doctor por ID de usuario: {}", userId);
    
    try {
        // Utilizamos el repositorio directamente para buscar por userId
        Optional<Doctor> doctorOpt = doctorRepository.findByUserId(userId);
        
        // Registramos si se encontró o no el doctor
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            log.info("Doctor encontrado por userId {}: {} {} (ID: {})", 
                    userId, doctor.getFirstName(), doctor.getLastName(), doctor.getId());
        } else {
            log.warn("No se encontró ningún doctor para el userId: {}", userId);
        }
        
        // Convertimos el doctor a DTO si está presente
        return doctorOpt.map(doctorMapper::toDto);
    } catch (Exception e) {
        log.error("Error al buscar doctor por userId {}: {}", userId, e.getMessage(), e);
        return Optional.empty();
    }
}

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorDto> searchDoctorsByName(String namePattern, Pageable pageable) {
        return queryService.searchDoctorsByName(namePattern, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorDto> findDoctorsBySpecialty(Long specialtyId, Pageable pageable) {
        return queryService.findDoctorsBySpecialty(specialtyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorDto> findAvailableDoctors(DayOfWeek dayOfWeek, LocalTime startTime, 
                                              LocalTime endTime, Pageable pageable) {
        return queryService.findAvailableDoctors(dayOfWeek, startTime, endTime, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorDto> searchDoctors(DoctorSearchCriteria criteria, Pageable pageable) {
        return queryService.searchDoctors(criteria, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorDto> getAllDoctors(Pageable pageable) {
        return queryService.getAllDoctors(pageable);
    }

    // Management operations - delegate to management service

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateConsultationFee(Long doctorId, Double consultationFee) {
        validationService.validateConsultationFeeUpdate(consultationFee);
        managementService.updateConsultationFee(doctorId, consultationFee);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateBiography(Long doctorId, String biography) {
        managementService.updateBiography(doctorId, biography);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addSpecialtyToDoctor(Long doctorId, Long specialtyId, String experienceLevel, Date certificationDate) {
        managementService.addSpecialtyToDoctor(doctorId, specialtyId, experienceLevel, certificationDate);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeSpecialtyFromDoctor(Long doctorId, Long specialtyId) {
        managementService.removeSpecialtyFromDoctor(doctorId, specialtyId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addWorkSchedule(Long doctorId, DoctorScheduleDto scheduleDto) {
        DoctorSchedule schedule = convertToScheduleEntity(scheduleDto);
        validationService.validateDoctorSchedule(schedule);
        managementService.addWorkSchedule(doctorId, schedule);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateWorkSchedule(Long doctorId, DoctorScheduleDto scheduleDto) {
        DoctorSchedule schedule = convertToScheduleEntity(scheduleDto);
        validationService.validateDoctorSchedule(schedule);
        managementService.updateWorkSchedule(doctorId, schedule);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeWorkSchedule(Long doctorId, Long scheduleId) {
        managementService.removeWorkSchedule(doctorId, scheduleId);
    }

    // Statistics and reporting

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAppointmentStatsByDoctor(LocalDateTime startDate, LocalDateTime endDate) {
        return queryService.getAppointmentStatsByDoctor(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorDto> findDoctorsByConsultationFeeRange(Double minFee, Double maxFee, Pageable pageable) {
        return queryService.findDoctorsByConsultationFeeRange(minFee, maxFee, pageable);
    }

    // Private helper methods (Template Method Pattern steps)

    /**
     * Construye un objeto User sin guardarlo en la base de datos
     */
    private User buildDoctorUser(CreateDoctorRequest request) {
        Role doctorRole = roleRepository.findByName(Role.ERole.ROLE_DOCTOR)
                .orElseThrow(() -> new RuntimeException("Doctor role not found"));

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(User.UserStatus.ACTIVE)
                .roles(Set.of(doctorRole))
                .firstLogin(true)
                .build();
    }

    /**
     * Construye un objeto Doctor sin guardarlo en la base de datos
     */
    private Doctor buildDoctorEntity(CreateDoctorRequest request, User user) {
        return Doctor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .licenseNumber(request.getLicenseNumber())
                .biography(request.getBiography())
                .consultationFee(request.getConsultationFee())
                .profilePicture(request.getProfilePicture())
                .user(user)
                .workSchedules(new HashSet<>())
                .doctorSpecialties(new ArrayList<>())
                .build();
    }

    /**
     * Añade especialidades al doctor
     */
    private void addSpecialtiesToDoctor(Doctor doctor, Set<Long> specialtyIds) {
        if (specialtyIds == null || specialtyIds.isEmpty()) {
            return;
        }

        for (Long specialtyId : specialtyIds) {
            Specialty specialty = specialtyRepository.findById(specialtyId)
                    .orElseThrow(() -> new RuntimeException("Specialty not found: " + specialtyId));

            DoctorSpecialty doctorSpecialty = DoctorSpecialty.builder()
                    .doctor(doctor)
                    .specialty(specialty)
                    .experienceLevel("BEGINNER") // Default, can be updated later
                    .certificationDate(new Date())
                    .build();

            doctor.getDoctorSpecialties().add(doctorSpecialty);
        }
    }

    /**
     * Actualiza los campos del doctor
     */
    private void updateDoctorFields(Doctor doctor, CreateDoctorRequest request) {
        if (request.getFirstName() != null) {
            doctor.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            doctor.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            doctor.setPhone(request.getPhone());
        }
        if (request.getBiography() != null) {
            doctor.setBiography(request.getBiography());
        }
        if (request.getConsultationFee() != null) {
            doctor.setConsultationFee(request.getConsultationFee());
        }
        if (request.getProfilePicture() != null) {
            doctor.setProfilePicture(request.getProfilePicture());
        }
    }

    /**
     * Actualiza las especialidades del doctor
     */
    private void updateDoctorSpecialties(Doctor doctor, Set<Long> newSpecialtyIds) {
        // Clear existing specialties
        doctor.getDoctorSpecialties().clear();
        
        // Add new specialties
        addSpecialtiesToDoctor(doctor, newSpecialtyIds);
    }

    /**
     * Convierte un DTO de horario a entidad
     */
    private DoctorSchedule convertToScheduleEntity(DoctorScheduleDto dto) {
        return DoctorSchedule.builder()
                .id(dto.getId())
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .slotDurationMinutes(dto.getSlotDurationMinutes())
                .active(dto.isActive())
                .location(dto.getLocation())
                .build();
    }
}