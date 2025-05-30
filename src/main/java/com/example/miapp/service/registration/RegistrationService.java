package com.example.miapp.service.registration;

import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.dto.user.UserDto;
import com.example.miapp.entity.*;
import com.example.miapp.mapper.UserMapper;
import com.example.miapp.repository.*;
import com.example.miapp.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Servicio para el registro de diferentes tipos de usuarios en el sistema.
 * Coordina el proceso completo de registro, incluyendo la creación de usuarios,
 * asignación de roles y creación de entidades específicas por tipo de usuario.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorSpecialtyRepository doctorSpecialtyRepository;

    /**
     * Registra un nuevo administrador en el sistema.
     *
     * @param username Nombre de usuario
     * @param email Correo electrónico
     * @param password Contraseña
     * @param cc Número de cédula o identificación
     * @return DTO con la información del usuario creado
     */
    @Transactional
    public UserDto registerAdmin(String username, String email, String password, String cc) {
        log.info("Iniciando registro de administrador: {}", username);
        
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        
        User user = authService.registerUser(username, email, password, cc, roles);
        
        log.info("Administrador registrado exitosamente: {}", username);
        return userMapper.toDto(user);
    }
    
    /**
     * Método sobrecargado para mantener compatibilidad con código existente
     */
    @Transactional
    public UserDto registerAdmin(String username, String email, String password) {
        return registerAdmin(username, email, password, null); // Sin cédula
    }

    /**
     * Registra un nuevo paciente en el sistema con su información específica.
     *
     * @param request DTO con la información del paciente
     * @return DTO con la información del usuario creado
     */
    @Transactional
    public UserDto registerPatient(CreatePatientRequest request) {
        log.info("Iniciando registro de paciente: {}", request.getUsername());
        
        // Crear usuario con rol paciente
        Set<String> roles = new HashSet<>();
        roles.add("patient");
        
        User user = authService.registerUser(
                request.getUsername(), 
                request.getEmail(), 
                request.getPassword(),
                request.getCc(), // Nuevo campo cc
                roles);
        
        // Crear entidad paciente
        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setBirthDate(request.getBirthDate());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        patient.setGender(request.getGender());
        patient.setBloodType(request.getBloodType());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setInsuranceProvider(request.getInsuranceProvider());
        patient.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
        patient.setUser(user);
        
        patientRepository.save(patient);
        
        log.info("Paciente registrado exitosamente: {}", request.getUsername());
        return userMapper.toDto(user);
    }

    /**
     * Registra un nuevo doctor en el sistema con su información específica y especialidades.
     *
     * @param request DTO con la información del doctor
     * @param specialtyIds IDs de las especialidades del doctor
     * @return DTO con la información del usuario creado
     * @throws IllegalArgumentException si no se proporciona al menos una especialidad
     * @throws RuntimeException si alguna especialidad no existe o hay errores en el proceso
     */
    @Transactional
    public UserDto registerDoctor(CreateDoctorRequest request, Set<Long> specialtyIds) {
        log.info("Iniciando registro de doctor: {}", request.getUsername());
        
        // Validar que tenga al menos una especialidad
        if (specialtyIds == null || specialtyIds.isEmpty()) {
            log.error("Intento de registro de doctor sin especialidades: {}", request.getUsername());
            throw new IllegalArgumentException("El doctor debe tener al menos una especialidad asignada");
        }
        
        // Crear usuario con rol doctor
        Set<String> roles = new HashSet<>();
        roles.add("doctor");
        
        User user = authService.registerUser(
                request.getUsername(), 
                request.getEmail(), 
                request.getPassword(),
                request.getCc(), 
                roles);
        
        // Crear entidad doctor
        Doctor doctor = new Doctor();
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setPhone(request.getPhone());
        doctor.setEmail(request.getEmail());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setBiography(request.getBiography());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setProfilePicture(request.getProfilePicture());
        doctor.setUser(user);
        
        doctor = doctorRepository.save(doctor);
        
        // Asignar especialidades
        for (Long specialtyId : specialtyIds) {
            Specialty specialty = specialtyRepository.findById(specialtyId)
                    .orElseThrow(() -> {
                        log.error("Especialidad no encontrada durante registro de doctor: {}", specialtyId);
                        return new RuntimeException("Especialidad no encontrada: " + specialtyId);
                    });
            
            DoctorSpecialty doctorSpecialty = new DoctorSpecialty();
            doctorSpecialty.setDoctor(doctor);
            doctorSpecialty.setSpecialty(specialty);
            // Valores por defecto para certificación y experiencia
            doctorSpecialty.setExperienceLevel("Junior");
            
            doctorSpecialtyRepository.save(doctorSpecialty);
            log.debug("Especialidad {} asignada al doctor {}", specialty.getName(), doctor.getFullName());
        }
        
        log.info("Doctor registrado exitosamente: {} con {} especialidades", 
                request.getUsername(), specialtyIds.size());
        return userMapper.toDto(user);
    }
}