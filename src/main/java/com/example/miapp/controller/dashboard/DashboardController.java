package com.example.miapp.controller.dashboard;

import com.example.miapp.service.dashboard.DashboardBusinessService;
import com.example.miapp.service.doctor.DoctorService;
import com.example.miapp.service.patient.PatientService;
import com.example.miapp.repository.UserRepository;
import com.example.miapp.entity.User;
import com.example.miapp.dto.doctor.DoctorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

/**
 * Controlador principal del dashboard del sistema hospitalario
 * Conectado con servicios reales para mostrar datos de la base de datos
 * Versión final ajustada para asegurar la redirección correcta de doctores
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardBusinessService dashboardService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final UserRepository userRepository;

    /**
     * Dashboard principal - Redirige según el rol del usuario
     * GET /dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        log.info("Usuario {} accediendo al dashboard con roles: {}", username, authorities);

        // Determinar redirección según el rol
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            
            switch (role) {
                case "ROLE_ADMIN":
                    log.info("Redirigiendo administrador {} al panel de administración", username);
                    return "redirect:/admin";
                    
                case "ROLE_DOCTOR":
                    log.info("Redirigiendo doctor {} al portal médico", username);
                    return "redirect:/doctor-portal";
                    
                case "ROLE_PATIENT":
                    log.info("Redirigiendo paciente {} al portal del paciente", username);
                    return "redirect:/portal";
            }
        }

        // Fallback - si no tiene roles específicos, dashboard genérico
        log.warn("Usuario {} sin rol específico, mostrando dashboard genérico", username);
        return showGenericDashboard(authentication, model);
    }

    /**
     * Dashboard genérico para usuarios sin rol específico
     */
    private String showGenericDashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());
        model.addAttribute("pageTitle", "Dashboard - Sistema Hospitalario");
        
        return "dashboard/generic";
    }

    /**
     * Dashboard de administrador - CON DATOS REALES
     * GET /admin
     */
    @GetMapping("/admin")
    public String adminDashboard(Authentication authentication, Model model) {
        
        String username = authentication.getName();
        log.info("Administrador {} accediendo al panel de administración", username);

        try {
            // Obtener datos reales del dashboard
            var dashboardData = dashboardService.getHospitalDashboard(LocalDate.now());
            
            // Obtener estadísticas reales
            long totalUsers = userRepository.count();
            long totalDoctors = doctorService.getAllDoctors(Pageable.unpaged()).getTotalElements();
            long totalPatients = patientService.getAllPatients(Pageable.unpaged()).getTotalElements();
            
            // Datos del modelo
            model.addAttribute("username", username);
            model.addAttribute("pageTitle", "Panel de Administración");
            model.addAttribute("userRole", "Administrador");
            
            // Estadísticas reales de la base de datos
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalDoctors", totalDoctors);
            model.addAttribute("totalPatients", totalPatients);
            
            // Datos del dashboard de citas
            if (dashboardData.getStats() != null) {
                model.addAttribute("totalAppointments", dashboardData.getStats().getTotalAppointments());
                model.addAttribute("scheduledAppointments", dashboardData.getStats().getScheduledAppointments());
                model.addAttribute("completedAppointments", dashboardData.getStats().getCompletedAppointments());
                model.addAttribute("canceledAppointments", dashboardData.getStats().getCanceledAppointments());
                model.addAttribute("totalRevenue", dashboardData.getStats().getTotalRevenue());
            } else {
                // Valores por defecto si no hay datos
                model.addAttribute("totalAppointments", 0);
                model.addAttribute("scheduledAppointments", 0);
                model.addAttribute("completedAppointments", 0);
                model.addAttribute("canceledAppointments", 0);
                model.addAttribute("totalRevenue", 0.0);
            }
            
            log.info("Dashboard de admin cargado - Usuarios: {}, Doctores: {}, Pacientes: {}", 
                    totalUsers, totalDoctors, totalPatients);
            
        } catch (Exception e) {
            log.error("Error al cargar dashboard de administrador: {}", e.getMessage(), e);
            
            // Valores por defecto en caso de error
            model.addAttribute("username", username);
            model.addAttribute("pageTitle", "Panel de Administración");
            model.addAttribute("userRole", "Administrador");
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalDoctors", 0);
            model.addAttribute("totalPatients", 0);
            model.addAttribute("errorMessage", "Error al cargar estadísticas del sistema: " + e.getMessage());
        }
        
        return "dashboard/admin";
    }

    /**
     * Portal del doctor - CON DATOS REALES
     * GET /doctor-portal
     * Implementación final con estrategia robusta de búsqueda
     */
    @GetMapping("/doctor-portal") 
    public String doctorPortal(Authentication authentication, Model model) {
        
        String username = authentication.getName();
        log.info("Doctor {} accediendo al portal médico", username);

        try {
            // Estrategia de búsqueda mejorada para encontrar al doctor
            Optional<DoctorDto> doctorOpt = Optional.empty();
            Optional<User> userOpt = Optional.empty();
            
            // Paso 1: Intentar encontrar primero al usuario por username
            userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Long userId = user.getId();
                log.info("Usuario encontrado con ID: {}, intentando buscar perfil de doctor", userId);
                
                // Paso 2: Buscar doctor por userId
                doctorOpt = doctorService.findDoctorByUserId(userId);
                if (doctorOpt.isPresent()) {
                    log.info("Doctor encontrado por userId: {}", userId);
                }
            }
            
            // Paso 3: Si no se encontró por userId, intentar por email
            if (doctorOpt.isEmpty()) {
                // Si el username parece un email, buscar directamente
                if (username.contains("@")) {
                    doctorOpt = doctorService.findDoctorByEmail(username);
                    log.info("Intentando buscar doctor por email directo: {}", username);
                } 
                // Si no es un email, probar con formatos comunes
                else {
                    String[] emailDomains = {"@hospital.com", "@example.com", "@clinic.org", "@medical.net"};
                    for (String domain : emailDomains) {
                        String testEmail = username + domain;
                        log.info("Intentando buscar doctor con email construido: {}", testEmail);
                        doctorOpt = doctorService.findDoctorByEmail(testEmail);
                        if (doctorOpt.isPresent()) {
                            log.info("Doctor encontrado con email: {}", testEmail);
                            break;
                        }
                    }
                }
            }
            
            // Paso 4: Procesar resultados según lo encontrado
            if (doctorOpt.isEmpty()) {
                // Si aún no encontramos al doctor, usamos datos generales
                log.warn("No se encontró información del doctor para el usuario: {}", username);
                var dashboardData = dashboardService.getRealTimeStats();
                
                // Datos básicos del usuario para la vista
                String displayName = userOpt.isPresent() ? userOpt.get().getUsername() : username;
                
                model.addAttribute("username", displayName);
                model.addAttribute("pageTitle", "Portal del Doctor");
                model.addAttribute("userRole", "Doctor");
                model.addAttribute("todayAppointments", dashboardData.getTodayAppointments());
                model.addAttribute("pendingAppointments", dashboardData.getPendingAppointments());
                model.addAttribute("completedToday", 0);
                model.addAttribute("nextAppointment", "No hay citas programadas");
                model.addAttribute("warningMessage", "No se encontró información completa para su perfil de doctor");
            } else {
                // Si encontramos al doctor, usamos sus datos específicos
                var doctor = doctorOpt.get();
                log.info("Doctor encontrado: {} {} (ID: {})", doctor.getFirstName(), doctor.getLastName(), doctor.getId());
                
                // Obtener dashboard específico del doctor
                var doctorDashboard = dashboardService.getDoctorDashboard(doctor.getId(), LocalDate.now());
                
                // Información básica
                model.addAttribute("username", doctor.getFirstName() + " " + doctor.getLastName());
                model.addAttribute("pageTitle", "Portal del Doctor " + doctor.getFirstName() + " " + doctor.getLastName());
                model.addAttribute("userRole", "Doctor");
                
                // Estadísticas de citas
                model.addAttribute("todayAppointments", doctorDashboard.getTotalAppointments());
                model.addAttribute("pendingAppointments", doctorDashboard.getTotalAppointments() - doctorDashboard.getCompletedAppointments());
                model.addAttribute("completedToday", doctorDashboard.getCompletedAppointments());
                model.addAttribute("nextAppointment", "Próxima cita disponible en su agenda");
                
                // Datos adicionales para el portal del doctor
                model.addAttribute("doctorId", doctor.getId());
                model.addAttribute("doctorEmail", doctor.getEmail());
                model.addAttribute("doctorSpecialties", doctor.getSpecialties());
                model.addAttribute("consultationFee", doctor.getConsultationFee());
                model.addAttribute("successMessage", "Bienvenido a su portal, Dr. " + doctor.getLastName());
            }
            
            // Log explícito para confirmar el uso de la plantilla correcta
            log.info("Renderizando vista de portal de doctor para usuario: {}", username);
            
        } catch (Exception e) {
            log.error("Error al cargar portal del doctor: {}", e.getMessage(), e);
            
            // Valores por defecto en caso de error
            model.addAttribute("username", username);
            model.addAttribute("pageTitle", "Portal del Doctor");
            model.addAttribute("userRole", "Doctor");
            model.addAttribute("todayAppointments", 0);
            model.addAttribute("pendingAppointments", 0);
            model.addAttribute("completedToday", 0);
            model.addAttribute("nextAppointment", "Error al cargar citas");
            model.addAttribute("errorMessage", "Error al cargar datos del doctor: " + e.getMessage());
        }
        
        // Especificar explícitamente la vista a usar - asegúrate de que existe en src/main/resources/templates/dashboard/doctor.html
        return "dashboard/doctor";
    }



    /**
     * Página de bienvenida/inicio público
     * GET / o GET /home
     */
    @GetMapping({"/", "/home"})
    public String home(Authentication authentication, Model model) {
        
        // Si el usuario está autenticado, redirigir al dashboard
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            log.info("Usuario autenticado {} accediendo a home, redirigiendo a dashboard", 
                    authentication.getName());
            return "redirect:/dashboard";
        }
        
        // Usuario no autenticado - mostrar página de inicio
        log.info("Usuario anónimo accediendo a la página de inicio");
        
        model.addAttribute("pageTitle", "Sistema Hospitalario");
        model.addAttribute("welcomeMessage", "Bienvenido al Sistema de Gestión Hospitalaria");
        
        return "home";
    }

    /**
     * Página de información del sistema
     * GET /about
     */
    @GetMapping("/about")
    public String about(Model model) {
        
        log.info("Acceso a página de información del sistema");
        
        model.addAttribute("pageTitle", "Acerca del Sistema");
        model.addAttribute("systemName", "Sistema de Gestión Hospitalaria");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("description", "Sistema integral para la gestión de hospitales y clínicas");
        
        return "about";
    }

    /**
     * Página de contacto
     * GET /contact
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        
        log.info("Acceso a página de contacto");
        
        model.addAttribute("pageTitle", "Contacto");
        model.addAttribute("hospitalName", "Hospital General");
        model.addAttribute("address", "Av. Principal 123, Ciudad");
        model.addAttribute("phone", "+1 (555) 123-4567");
        model.addAttribute("email", "info@hospital.com");
        
        return "contact";
    }
}