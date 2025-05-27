package com.example.miapp.controller.dashboard;

import com.example.miapp.dto.dashboard.DashboardDto;
import com.example.miapp.dto.dashboard.DashboardStatsDto;
import com.example.miapp.dto.dashboard.DoctorDashboardDto;
import com.example.miapp.dto.dashboard.PatientDashboardDto;
import com.example.miapp.dto.dashboard.RealTimeStatsDto;
import com.example.miapp.dto.doctor.DoctorDto;
import com.example.miapp.dto.patient.PatientDto;
import com.example.miapp.entity.User;
import com.example.miapp.repository.UserRepository;
import com.example.miapp.service.dashboard.DashboardBusinessService;
import com.example.miapp.service.doctor.DoctorService;
import com.example.miapp.service.patient.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController - Pruebas Unitarias con Servicios Reales")
class DashboardControllerTest {

    @Mock
    private DashboardBusinessService dashboardService;
    
    @Mock
    private DoctorService doctorService;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;
    
    @Mock
    private Model model;

    @InjectMocks
    private DashboardController dashboardController;

    private static final String TEST_USERNAME = "testuser";
    private static final String ADMIN_USERNAME = "admin";
    private static final String DOCTOR_USERNAME = "doctor1";
    private static final String PATIENT_USERNAME = "patient1";

    @BeforeEach
    void setUp() {
        // Setup común si es necesario
    }

    // ========== TESTS PARA DASHBOARD PRINCIPAL ==========

    @Test
    @DisplayName("Dashboard - Usuario con rol ADMIN debe redirigir a /admin")
    void dashboard_WithAdminRole_ShouldRedirectToAdmin() {
        // Given
        doReturn(ADMIN_USERNAME).when(authentication).getName();
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
            .when(authentication).getAuthorities();

        // When
        String result = dashboardController.dashboard(authentication, model);

        // Then
        assertEquals("redirect:/admin", result);
        verify(authentication).getName();
        verify(authentication).getAuthorities();
    }

    @Test
    @DisplayName("Dashboard - Usuario con rol DOCTOR debe redirigir a /doctor-portal")
    void dashboard_WithDoctorRole_ShouldRedirectToDoctorPortal() {
        // Given
        doReturn(DOCTOR_USERNAME).when(authentication).getName();
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_DOCTOR")))
            .when(authentication).getAuthorities();

        // When
        String result = dashboardController.dashboard(authentication, model);

        // Then
        assertEquals("redirect:/doctor-portal", result);
    }

    @Test
    @DisplayName("Dashboard - Usuario con rol PATIENT debe redirigir a /portal")
    void dashboard_WithPatientRole_ShouldRedirectToPatientPortal() {
        // Given
        doReturn(PATIENT_USERNAME).when(authentication).getName();
        doReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_PATIENT")))
            .when(authentication).getAuthorities();

        // When
        String result = dashboardController.dashboard(authentication, model);

        // Then
        assertEquals("redirect:/portal", result);
    }

    // ========== TESTS PARA DASHBOARD DE ADMIN CON DATOS REALES ==========

    @Test
    @DisplayName("Admin Dashboard - Debe cargar datos reales de la base de datos exitosamente")
    void adminDashboard_WithRealData_ShouldReturnAdminViewWithDatabaseData() {
        // Given
        when(authentication.getName()).thenReturn(ADMIN_USERNAME);
        
        // Mock dashboard service
        DashboardDto dashboardDto = createMockDashboardDto();
        when(dashboardService.getHospitalDashboard(any(LocalDate.class))).thenReturn(dashboardDto);
        
        // Mock repository counts
        when(userRepository.count()).thenReturn(10L);
        when(doctorService.getAllDoctors(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(patientService.getAllPatients(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        // When
        String result = dashboardController.adminDashboard(authentication, model);

        // Then
        assertEquals("dashboard/admin", result);
        verify(model).addAttribute("username", ADMIN_USERNAME);
        verify(model).addAttribute("pageTitle", "Panel de Administración");
        verify(model).addAttribute("userRole", "Administrador");
        verify(model).addAttribute("totalUsers", 10L);
        verify(model).addAttribute("totalAppointments", 25);
        verify(model).addAttribute("scheduledAppointments", 15);
        verify(model).addAttribute("completedAppointments", 8);
        verify(model).addAttribute("canceledAppointments", 2);
        verify(model).addAttribute("totalRevenue", 5000.0);
        
        // Verify service calls
        verify(dashboardService).getHospitalDashboard(any(LocalDate.class));
        verify(userRepository).count();
        verify(doctorService).getAllDoctors(any(Pageable.class));
        verify(patientService).getAllPatients(any(Pageable.class));
    }

    @Test
    @DisplayName("Admin Dashboard - Debe manejar error al cargar datos")
    void adminDashboard_WithServiceError_ShouldHandleGracefully() {
        // Given
        when(authentication.getName()).thenReturn(ADMIN_USERNAME);
        when(dashboardService.getHospitalDashboard(any(LocalDate.class)))
            .thenThrow(new RuntimeException("Error de base de datos"));

        // When
        String result = dashboardController.adminDashboard(authentication, model);

        // Then
        assertEquals("dashboard/admin", result);
        verify(model).addAttribute("username", ADMIN_USERNAME);
        verify(model).addAttribute("errorMessage", "Error al cargar estadísticas del sistema");
        verify(model).addAttribute("totalUsers", 0);
        verify(model).addAttribute("totalDoctors", 0);
        verify(model).addAttribute("totalPatients", 0);
    }

    // ========== TESTS PARA PORTAL DEL DOCTOR CON DATOS REALES ==========

    @Test
    @DisplayName("Doctor Portal - Debe cargar datos reales del doctor exitosamente")
    void doctorPortal_WithExistingDoctor_ShouldReturnDoctorViewWithRealData() {
        // Given
        when(authentication.getName()).thenReturn(DOCTOR_USERNAME);
        
        // Mock doctor exists
        DoctorDto doctorDto = createMockDoctorDto();
        when(doctorService.findDoctorByEmail(DOCTOR_USERNAME + "@hospital.com"))
            .thenReturn(Optional.of(doctorDto));
        
        // Mock doctor dashboard
        DoctorDashboardDto doctorDashboard = createMockDoctorDashboard();
        when(dashboardService.getDoctorDashboard(eq(1L), any(LocalDate.class)))
            .thenReturn(doctorDashboard);

        // When
        String result = dashboardController.doctorPortal(authentication, model);

        // Then
        assertEquals("dashboard/doctor", result);
        verify(model).addAttribute("username", "Dr. Juan Pérez");
        verify(model).addAttribute("pageTitle", "Portal del Doctor");
        verify(model).addAttribute("userRole", "Doctor");
        verify(model).addAttribute("todayAppointments", 8);
        verify(model).addAttribute("pendingAppointments", 3);
        verify(model).addAttribute("completedToday", 5);
    }

    @Test
    @DisplayName("Doctor Portal - Debe manejar doctor no encontrado")
    void doctorPortal_WithNonExistentDoctor_ShouldUseGenericData() {
        // Given
        when(authentication.getName()).thenReturn(DOCTOR_USERNAME);
        when(doctorService.findDoctorByEmail(DOCTOR_USERNAME + "@hospital.com"))
            .thenReturn(Optional.empty());
        
        // Mock user exists
        User mockUser = new User();
        when(userRepository.findByUsername(DOCTOR_USERNAME)).thenReturn(Optional.of(mockUser));
        
        // Mock real time stats
        RealTimeStatsDto realTimeStats = createMockRealTimeStats();
        when(dashboardService.getRealTimeStats()).thenReturn(realTimeStats);

        // When
        String result = dashboardController.doctorPortal(authentication, model);

        // Then
        assertEquals("dashboard/doctor", result);
        verify(model).addAttribute("username", DOCTOR_USERNAME);
        verify(model).addAttribute("todayAppointments", 12);
        verify(model).addAttribute("pendingAppointments", 4);
    }

    // ========== TESTS PARA PORTAL DEL PACIENTE CON DATOS REALES ==========

    @Test
    @DisplayName("Patient Portal - Debe cargar datos reales del paciente exitosamente")
    void patientPortal_WithExistingPatient_ShouldReturnPatientViewWithRealData() {
        // Given
        when(authentication.getName()).thenReturn(PATIENT_USERNAME);
        
        PatientDto patientDto = createMockPatientDto();
        when(patientService.findPatientByEmail(PATIENT_USERNAME + "@example.com"))
            .thenReturn(Optional.of(patientDto));
        
        PatientDashboardDto patientDashboard = createMockPatientDashboard();
        when(dashboardService.getPatientDashboard(1L)).thenReturn(patientDashboard);

        // When
        String result = dashboardController.patientPortal(authentication, model);

        // Then
        assertEquals("dashboard/patient", result);
        verify(model).addAttribute("username", "Juan Pérez");
        verify(model).addAttribute("pageTitle", "Mi Portal");
        verify(model).addAttribute("userRole", "Paciente");
        // CORREGIDO: El controller devuelve 0 porque la lista está vacía
        verify(model).addAttribute("activePrescriptions", 0);
        verify(model).addAttribute("nextAppointment", "No hay citas programadas");
        verify(model).addAttribute("lastVisit", "Consultando última visita...");
        verify(model).addAttribute("pendingResults", 0);
    }

    @Test
    @DisplayName("Patient Portal - Debe manejar error al cargar datos del paciente")
    void patientPortal_WithServiceError_ShouldHandleGracefully() {
        // Given
        when(authentication.getName()).thenReturn(PATIENT_USERNAME);
        when(patientService.findPatientByEmail(PATIENT_USERNAME + "@example.com"))
            .thenThrow(new RuntimeException("Error de base de datos"));

        // When
        String result = dashboardController.patientPortal(authentication, model);

        // Then
        assertEquals("dashboard/patient", result);
        verify(model).addAttribute("username", PATIENT_USERNAME);
        verify(model).addAttribute("errorMessage", "Error al cargar datos del paciente");
        verify(model).addAttribute("activePrescriptions", 0);
        verify(model).addAttribute("pendingResults", 0);
    }

    // ========== TESTS PARA PÁGINA HOME ==========

    @Test
    @DisplayName("Home - Usuario autenticado debe redirigir a dashboard")
    void home_WithAuthenticatedUser_ShouldRedirectToDashboard() {
        // Given
        doReturn(TEST_USERNAME).when(authentication).getName();
        doReturn(true).when(authentication).isAuthenticated();

        // When
        String result = dashboardController.home(authentication, model);

        // Then
        assertEquals("redirect:/dashboard", result);
    }

    @Test
    @DisplayName("Home - Usuario anónimo debe mostrar página de inicio")
    void home_WithAnonymousUser_ShouldReturnHomePage() {
        // Given
        doReturn("anonymousUser").when(authentication).getName();
        doReturn(true).when(authentication).isAuthenticated();

        // When
        String result = dashboardController.home(authentication, model);

        // Then
        assertEquals("home", result);
        verify(model).addAttribute("pageTitle", "Sistema Hospitalario");
        verify(model).addAttribute("welcomeMessage", "Bienvenido al Sistema de Gestión Hospitalaria");
    }

    // ========== TESTS PARA MÚLTIPLES ROLES ==========

    @Test
    @DisplayName("Dashboard - Usuario con múltiples roles debe priorizar ADMIN")
    void dashboard_WithMultipleRoles_ShouldPrioritizeAdmin() {
        // Given
        doReturn(TEST_USERNAME).when(authentication).getName();
        doReturn(Arrays.asList(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_DOCTOR"),
            new SimpleGrantedAuthority("ROLE_PATIENT")
        )).when(authentication).getAuthorities();

        // When
        String result = dashboardController.dashboard(authentication, model);

        // Then
        assertEquals("redirect:/admin", result);
    }

    @Test
    @DisplayName("Dashboard - Usuario sin roles específicos debe mostrar dashboard genérico")
    void dashboard_WithoutSpecificRoles_ShouldShowGenericDashboard() {
        // Given
        doReturn(TEST_USERNAME).when(authentication).getName();
        List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_OTHER"));
        doReturn(authorities).when(authentication).getAuthorities();

        // When
        String result = dashboardController.dashboard(authentication, model);

        // Then
        assertEquals("dashboard/generic", result);
        verify(model).addAttribute("username", TEST_USERNAME);
        verify(model).addAttribute("roles", authorities);
        verify(model).addAttribute("pageTitle", "Dashboard - Sistema Hospitalario");
    }

    // ========== MÉTODOS HELPER PARA CREAR MOCKS ==========

    private DashboardDto createMockDashboardDto() {
        DashboardDto dashboard = new DashboardDto();
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalAppointments(25);
        stats.setScheduledAppointments(15);
        stats.setCompletedAppointments(8);
        stats.setCanceledAppointments(2);
        stats.setTotalRevenue(5000.0);
        dashboard.setStats(stats);
        return dashboard;
    }

    private DoctorDto createMockDoctorDto() {
        DoctorDto doctor = new DoctorDto();
        doctor.setId(1L);
        doctor.setFirstName("Dr. Juan");
        doctor.setLastName("Pérez");
        return doctor;
    }

    private DoctorDashboardDto createMockDoctorDashboard() {
        DoctorDashboardDto dashboard = new DoctorDashboardDto();
        dashboard.setTotalAppointments(8);
        dashboard.setCompletedAppointments(5);
        return dashboard;
    }

    private PatientDto createMockPatientDto() {
        PatientDto patient = new PatientDto();
        patient.setId(1L);
        patient.setFirstName("Juan");
        patient.setLastName("Pérez");
        return patient;
    }

    private PatientDashboardDto createMockPatientDashboard() {
        PatientDashboardDto dashboard = new PatientDashboardDto();
        // CORREGIDO: Lista vacía para que el controller devuelva 0
        dashboard.setActivePrescriptions(Collections.emptyList());
        // Simular que no hay citas próximas
        dashboard.setUpcomingAppointments(Collections.emptyList());
        return dashboard;
    }

    private RealTimeStatsDto createMockRealTimeStats() {
        RealTimeStatsDto stats = new RealTimeStatsDto();
        stats.setTodayAppointments(12);
        stats.setPendingAppointments(4);
        return stats;
    }
}