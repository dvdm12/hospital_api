package com.example.miapp.controller.auth;

import com.example.miapp.dto.auth.SignupRequest;
import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.dto.doctor.DoctorDto;
import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.entity.User;
import com.example.miapp.exception.DoctorValidationException;
import com.example.miapp.repository.SpecialtyRepository;
import com.example.miapp.service.auth.AuthService;
import com.example.miapp.service.doctor.DoctorBusinessService;
import com.example.miapp.service.patient.PatientBusinessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de autenticación para vistas web (Thymeleaf)
 * Maneja login, registro de doctores y funciones de administración
 * Actualizado para manejar las excepciones de validación de doctores
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final DoctorBusinessService doctorService;
    private final SpecialtyRepository specialtyRepository;

    /**
     * Página de login con manejo completo de estados
     * GET /login
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                          @RequestParam(required = false) String logout,
                          @RequestParam(required = false) String expired,
                          @RequestParam(required = false) String success,
                          Model model,
                          HttpServletRequest request) {
        
        log.info("Mostrando página de login - IP: {}", getClientIpAddress(request));
        
        // Manejo de mensajes de estado
        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña incorrectos");
            log.warn("Intento de login fallido desde IP: {}", getClientIpAddress(request));
        }
        
        if (logout != null) {
            model.addAttribute("logoutMessage", "Sesión cerrada correctamente");
            log.info("Usuario desconectado correctamente desde IP: {}", getClientIpAddress(request));
        }
        
        if (expired != null) {
            model.addAttribute("expiredMessage", "Su sesión ha expirado. Por favor, inicie sesión nuevamente");
            log.info("Sesión expirada detectada desde IP: {}", getClientIpAddress(request));
        }

        if (success != null) {
            model.addAttribute("successMessage", "Operación completada exitosamente. Inicie sesión para continuar");
        }
        
        // Información adicional para la vista
        model.addAttribute("pageTitle", "Iniciar Sesión - Sistema Hospitalario");
        
        return "auth/login";
    }

    /**
     * Página de logout
     * GET /auth/logout
     */
    @GetMapping("/auth/logout")
    public String logoutPage() {
        return "auth/logout";
    }

    /**
     * Página de administración de usuarios (solo admin)
     * GET /admin/users
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String userManagementPage(Model model, Authentication authentication) {
        log.info("Administrador {} accediendo a gestión de usuarios", authentication.getName());
        
        model.addAttribute("adminUser", authentication.getName());
        model.addAttribute("pageTitle", "Administración de Usuarios");
        
        return "admin/users";
    }
    
    /**
     * Página de registro de doctor (solo admin)
     * GET /admin/register-doctor y GET /admin/register/doctor (para compatibilidad)
     */
    @GetMapping({"/admin/register-doctor", "/admin/register/doctor"})
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDoctorRegisterPage(Model model, Authentication authentication) {
        log.info("Administrador {} accediendo a registro de doctor", authentication.getName());
        
        if (!model.containsAttribute("doctorRequest")) {
            CreateDoctorRequest doctorRequest = new CreateDoctorRequest();
            // Pre-configurar valores por defecto
            doctorRequest.setConsultationFee(0.0);
            doctorRequest.setLicenseNumber("POR ASIGNAR-" + System.currentTimeMillis());
            model.addAttribute("doctorRequest", doctorRequest);
        }
        
        model.addAttribute("adminUser", authentication.getName());
        model.addAttribute("pageTitle", "Registrar Nuevo Doctor");
        
        // Cargar especialidades desde la base de datos
        model.addAttribute("specialties", specialtyRepository.findAll());
        log.info("Cargadas {} especialidades para el formulario de registro de doctor", 
                specialtyRepository.count());
        
        return "admin/register-doctor";
    }
    
    /**
     * Procesar registro de doctor (solo admin)
     * POST /admin/register-doctor y POST /admin/register/doctor (para compatibilidad)
     * Versión mejorada con mejor manejo de excepciones y validación
     */
    @PostMapping({"/admin/register-doctor", "/admin/register/doctor"})
    @PreAuthorize("hasRole('ADMIN')")
    public String processAdminDoctorRegistration(@Valid @ModelAttribute("doctorRequest") CreateDoctorRequest doctorRequest,
                                         BindingResult result,
                                         RedirectAttributes redirectAttributes,
                                         Model model,
                                         Authentication authentication,
                                         HttpServletRequest request) {
        
        log.info("Administrador {} registrando doctor: {} - IP: {}", 
                authentication.getName(), doctorRequest.getUsername(), getClientIpAddress(request));
        
        // Si hay errores de validación de bean, volver al formulario
        if (result.hasErrors()) {
            log.warn("Errores de validación en registro de doctor por admin: {}", result.getAllErrors());
            
            model.addAttribute("doctorRequest", doctorRequest);
            model.addAttribute("adminUser", authentication.getName());
            model.addAttribute("specialties", specialtyRepository.findAll());
            model.addAttribute("errorMessage", "Por favor corrija los errores en el formulario");
            
            return "admin/register-doctor";
        }
        
        try {
            // Asegurar que haya al menos una especialidad seleccionada
            if (doctorRequest.getSpecialtyIds() == null || doctorRequest.getSpecialtyIds().isEmpty()) {
                // Si no hay especialidades en la base de datos, no podemos asignar una por defecto
                if (specialtyRepository.count() == 0) {
                    model.addAttribute("errorMessage", 
                        "No hay especialidades disponibles en el sistema. Por favor, cree algunas primero.");
                    model.addAttribute("adminUser", authentication.getName());
                    model.addAttribute("specialties", specialtyRepository.findAll());
                    return "admin/register-doctor";
                }
                
                // Intenta asignar la primera especialidad disponible
                specialtyRepository.findAll().stream().findFirst().ifPresent(specialty -> {
                    Set<Long> defaultSpecialtyIds = new HashSet<>();
                    defaultSpecialtyIds.add(specialty.getId());
                    doctorRequest.setSpecialtyIds(defaultSpecialtyIds);
                    
                    log.info("Asignando especialidad por defecto (ID: {}) para doctor: {}", 
                            specialty.getId(), doctorRequest.getUsername());
                });
            }
            
            // Asegurar que el número de licencia nunca sea nulo
            if (doctorRequest.getLicenseNumber() == null || doctorRequest.getLicenseNumber().trim().isEmpty()) {
                doctorRequest.setLicenseNumber("POR ASIGNAR-" + System.currentTimeMillis());
                log.info("Generando número de licencia automático: {}", doctorRequest.getLicenseNumber());
            }
            
            // Asegurar que la tarifa de consulta nunca sea nula
            if (doctorRequest.getConsultationFee() == null) {
                doctorRequest.setConsultationFee(0.0);
                log.info("Estableciendo tarifa de consulta por defecto: 0.0");
            }
            
            // Crear doctor - ahora utilizamos directamente el servicio mejorado
            // Las validaciones específicas se manejan dentro del servicio
            DoctorDto newDoctor = doctorService.createDoctor(doctorRequest);
            
            log.info("Doctor registrado exitosamente por admin {} con ID: {}", 
                    authentication.getName(), newDoctor.getId());
            
            // Crear un nuevo doctor request para limpiar el formulario
            com.example.miapp.dto.doctor.CreateDoctorRequest newDoctorRequest = new com.example.miapp.dto.doctor.CreateDoctorRequest();
            newDoctorRequest.setConsultationFee(0.0);
            newDoctorRequest.setLicenseNumber("POR ASIGNAR-" + System.currentTimeMillis());
            
            // Preparar el modelo para volver a la misma página
            model.addAttribute("doctorRequest", newDoctorRequest);
            model.addAttribute("adminUser", authentication.getName());
            model.addAttribute("specialties", specialtyRepository.findAll());
            model.addAttribute("successMessage", 
                String.format("Doctor '%s %s' registrado exitosamente con ID: %d", 
                    doctorRequest.getFirstName(), doctorRequest.getLastName(), newDoctor.getId()));
            
            // Volver a la misma página de registro
            return "admin/register-doctor";
            
        } catch (DoctorValidationException e) {
            // Manejo específico para errores de validación de doctores
            log.warn("Error de validación al registrar doctor por admin {}: {}", 
                    authentication.getName(), e.getMessage());
            
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("doctorRequest", doctorRequest);
            model.addAttribute("adminUser", authentication.getName());
            model.addAttribute("specialties", specialtyRepository.findAll());
            
            return "admin/register-doctor";
            
        } catch (DataIntegrityViolationException e) {
            // Manejo específico para errores de integridad de datos (duplicados, etc.)
            log.error("Error de integridad de datos al registrar doctor por admin {}: {}", 
                    authentication.getName(), e.getMessage());
            
            String errorMsg = "Error de integridad en la base de datos. ";
            
            // Intentar extraer mensaje más amigable
            if (e.getMessage().contains("unique constraint") || e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains("username")) {
                    errorMsg += "El nombre de usuario ya existe.";
                } else if (e.getMessage().contains("email")) {
                    errorMsg += "El email ya está registrado.";
                } else if (e.getMessage().contains("license")) {
                    errorMsg += "El número de licencia ya está registrado.";
                } else {
                    errorMsg += "Hay información duplicada en la solicitud.";
                }
            } else {
                errorMsg += "Por favor verifique la información ingresada.";
            }
            
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("doctorRequest", doctorRequest);
            model.addAttribute("adminUser", authentication.getName());
            model.addAttribute("specialties", specialtyRepository.findAll());
            
            return "admin/register-doctor";
            
        } catch (Exception e) {
            // Manejo genérico para otros errores inesperados
            log.error("Error inesperado al registrar doctor por admin {}: {}", 
                    authentication.getName(), e.getMessage(), e);
            
            model.addAttribute("errorMessage", "Error al crear doctor: " + e.getMessage());
            model.addAttribute("doctorRequest", doctorRequest);
            model.addAttribute("adminUser", authentication.getName());
            model.addAttribute("specialties", specialtyRepository.findAll());
            
            return "admin/register-doctor";
        }
    }
    
    /**
     * Endpoint para validar disponibilidad de username (AJAX)
     * GET /auth/check-username
     */
    @GetMapping("/auth/check-username")
    @ResponseBody
    public String checkUsernameAvailability(@RequestParam String username) {
        
        if (username == null || username.trim().length() < 3) {
            return "{\"available\": false, \"message\": \"Username debe tener al menos 3 caracteres\"}";
        }
        
        boolean exists = authService.existsByUsername(username.trim());
        
        return String.format("{\"available\": %s, \"message\": \"%s\"}", 
            !exists, 
            exists ? "Username no disponible" : "Username disponible");
    }

    /**
     * Endpoint para validar disponibilidad de email (AJAX)
     * GET /auth/check-email
     */
    @GetMapping("/auth/check-email")
    @ResponseBody
    public String checkEmailAvailability(@RequestParam String email) {
        
        if (email == null || !email.contains("@")) {
            return "{\"available\": false, \"message\": \"Email inválido\"}";
        }
        
        boolean exists = authService.existsByEmail(email.trim().toLowerCase());
        
        return String.format("{\"available\": %s, \"message\": \"%s\"}", 
            !exists, 
            exists ? "Email ya registrado" : "Email disponible");
    }

    /**
     * Obtiene la dirección IP real del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}