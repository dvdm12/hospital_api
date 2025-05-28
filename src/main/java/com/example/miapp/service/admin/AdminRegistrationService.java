package com.example.miapp.service.admin;

import com.example.miapp.dto.auth.SignupRequest;
import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.User;
import com.example.miapp.service.auth.AuthService;
import com.example.miapp.service.doctor.DoctorBusinessService;
import com.example.miapp.service.patient.PatientBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio especializado para el registro de usuarios por administradores
 * Implementa el patrón Facade para simplificar el proceso de registro
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRegistrationService {

    private final AuthService authService;
    private final PatientBusinessService patientService;
    private final DoctorBusinessService doctorService;
    
    /**
     * Registra un nuevo usuario basado en la solicitud y los roles seleccionados
     */
    @Transactional
    public void registerUser(SignupRequest signupRequest, 
                            String firstName, 
                            String lastName, 
                            String adminUsername) {
        
        validateUniqueUserIdentifiers(signupRequest);
        
        Set<String> roles = signupRequest.getRole();
        boolean isDoctor = roles != null && roles.contains("doctor");
        boolean isPatient = roles != null && roles.contains("patient");
        
        // Verificar datos obligatorios según rol
        if ((isDoctor || isPatient) && (firstName == null || firstName.isEmpty() 
                || lastName == null || lastName.isEmpty())) {
            throw new IllegalArgumentException(
                "Los campos de nombre y apellido son obligatorios para roles de doctor y paciente");
        }
        
        // Registro según tipo de usuario
        if (isDoctor) {
            registerDoctor(signupRequest, firstName, lastName, adminUsername);
        } else if (isPatient) {
            registerPatient(signupRequest, firstName, lastName, adminUsername);
        } else {
            // Usuario administrativo
            registerAdminUser(signupRequest, adminUsername);
        }
    }
    
    /**
     * Verifica disponibilidad de nombre de usuario
     */
    public String checkUsernameAvailability(String username) {
        if (username == null || username.trim().length() < 3) {
            return "{\"available\": false, \"message\": \"Username debe tener al menos 3 caracteres\"}";
        }
        
        boolean exists = authService.existsByUsername(username.trim());
        
        return String.format("{\"available\": %s, \"message\": \"%s\"}", 
            !exists, 
            exists ? "Username no disponible" : "Username disponible");
    }
    
    /**
     * Verifica disponibilidad de email
     */
    public String checkEmailAvailability(String email) {
        if (email == null || !email.contains("@")) {
            return "{\"available\": false, \"message\": \"Email inválido\"}";
        }
        
        boolean exists = authService.existsByEmail(email.trim().toLowerCase());
        
        return String.format("{\"available\": %s, \"message\": \"%s\"}", 
            !exists, 
            exists ? "Email ya registrado" : "Email disponible");
    }
    
    // Métodos privados para diferentes tipos de registro
    
    private void registerDoctor(SignupRequest signupRequest, 
                               String firstName, 
                               String lastName, 
                               String adminUsername) {
        log.info("Registrando nuevo doctor: {} {} por admin {}", 
                firstName, lastName, adminUsername);
        
        CreateDoctorRequest doctorRequest = new CreateDoctorRequest();
        doctorRequest.setUsername(signupRequest.getUsername());
        doctorRequest.setEmail(signupRequest.getEmail());
        doctorRequest.setPassword(signupRequest.getPassword());
        doctorRequest.setFirstName(firstName);
        doctorRequest.setLastName(lastName);
        
        // Campos obligatorios mínimos
        doctorRequest.setPhone("Por completar");
        doctorRequest.setLicenseNumber("POR ASIGNAR-" + System.currentTimeMillis());
        doctorRequest.setConsultationFee(0.0);
        
        // CORRECCIÓN: Asignar al menos una especialidad por defecto
        Set<Long> defaultSpecialtyIds = new HashSet<>();
        defaultSpecialtyIds.add(1L); // ID de la especialidad por defecto (Medicina General)
        doctorRequest.setSpecialtyIds(defaultSpecialtyIds);
        
        doctorService.createDoctor(doctorRequest);
        log.info("Doctor registrado exitosamente por admin {}: {}", 
                adminUsername, signupRequest.getUsername());
    }
    
    private void registerPatient(SignupRequest signupRequest, 
                                String firstName, 
                                String lastName, 
                                String adminUsername) {
        log.info("Registrando nuevo paciente: {} {} por admin {}", 
                firstName, lastName, adminUsername);
        
        CreatePatientRequest patientRequest = new CreatePatientRequest();
        patientRequest.setUsername(signupRequest.getUsername());
        patientRequest.setEmail(signupRequest.getEmail());
        patientRequest.setPassword(signupRequest.getPassword());
        patientRequest.setFirstName(firstName);
        patientRequest.setLastName(lastName);
        
        // Campos obligatorios mínimos
        patientRequest.setBirthDate(new Date()); // Fecha por defecto
        patientRequest.setPhone("Por completar");
        patientRequest.setAddress("Por completar");
        patientRequest.setGender(Patient.Gender.OTHER);
        
        patientService.createPatient(patientRequest);
        log.info("Paciente registrado exitosamente por admin {}: {}", 
                adminUsername, signupRequest.getUsername());
    }
    
    private void registerAdminUser(SignupRequest signupRequest, String adminUsername) {
        log.info("Registrando nuevo usuario administrativo por admin {}: {}", 
                adminUsername, signupRequest.getUsername());
        
        User newUser = authService.registerUser(
            signupRequest.getUsername(),
            signupRequest.getEmail(),
            signupRequest.getPassword(),
            signupRequest.getRole()
        );
        
        log.info("Usuario {} registrado exitosamente por admin {} con ID: {}", 
                signupRequest.getUsername(), adminUsername, newUser.getId());
    }
    
    private void validateUniqueUserIdentifiers(SignupRequest signupRequest) {
        if (authService.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        if (authService.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }
    }
}