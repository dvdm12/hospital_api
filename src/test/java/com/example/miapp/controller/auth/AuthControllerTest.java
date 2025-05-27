package com.example.miapp.controller.auth;

import com.example.miapp.dto.auth.SignupRequest;
import com.example.miapp.entity.User;
import com.example.miapp.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthController - Pruebas Unitarias")
class AuthControllerTest {

    @Mock
    private AuthService authService;
    
    @Mock
    private Model model;
    
    @Mock
    private BindingResult bindingResult;
    
    @Mock
    private RedirectAttributes redirectAttributes;
    
    @Mock
    private HttpServletRequest httpServletRequest;
    
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Setup básico para mocks comunes - configuración lenient
        lenient().when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(authentication.getName()).thenReturn("testuser");
    }

    // ========== TESTS PARA LOGIN PAGE ==========

    @Test
    @DisplayName("GET /login - Página de login sin parámetros")
    void loginPage_WithoutParameters_ShouldReturnLoginView() {
        // When
        String result = authController.loginPage(null, null, null, null, model, httpServletRequest);

        // Then
        assertEquals("auth/login", result);
        verify(model).addAttribute("pageTitle", "Iniciar Sesión - Sistema Hospitalario");
    }

    @Test
    @DisplayName("GET /login - Con parámetro error")
    void loginPage_WithError_ShouldAddErrorMessage() {
        // When
        String result = authController.loginPage("true", null, null, null, model, httpServletRequest);

        // Then
        assertEquals("auth/login", result);
        verify(model).addAttribute("errorMessage", "Usuario o contraseña incorrectos");
        verify(model).addAttribute("pageTitle", "Iniciar Sesión - Sistema Hospitalario");
    }

    @Test
    @DisplayName("GET /login - Con parámetro logout")
    void loginPage_WithLogout_ShouldAddLogoutMessage() {
        // When
        String result = authController.loginPage(null, "true", null, null, model, httpServletRequest);

        // Then
        assertEquals("auth/login", result);
        verify(model).addAttribute("logoutMessage", "Sesión cerrada correctamente");
        verify(model).addAttribute("pageTitle", "Iniciar Sesión - Sistema Hospitalario");
    }

    @Test
    @DisplayName("GET /login - Con parámetro expired")
    void loginPage_WithExpired_ShouldAddExpiredMessage() {
        // When
        String result = authController.loginPage(null, null, "true", null, model, httpServletRequest);

        // Then
        assertEquals("auth/login", result);
        verify(model).addAttribute("expiredMessage", "Su sesión ha expirado. Por favor, inicie sesión nuevamente");
        verify(model).addAttribute("pageTitle", "Iniciar Sesión - Sistema Hospitalario");
    }

    @Test
    @DisplayName("GET /login - Con parámetro success")
    void loginPage_WithSuccess_ShouldAddSuccessMessage() {
        // When
        String result = authController.loginPage(null, null, null, "true", model, httpServletRequest);

        // Then
        assertEquals("auth/login", result);
        verify(model).addAttribute("successMessage", "Operación completada exitosamente. Inicie sesión para continuar");
        verify(model).addAttribute("pageTitle", "Iniciar Sesión - Sistema Hospitalario");
    }

    // ========== TESTS PARA REGISTER PAGE ==========

    @Test
    @DisplayName("GET /register - Página de registro")
    void registerPage_ShouldReturnRegisterView() {
        // When
        String result = authController.registerPage(model, authentication);

        // Then
        assertEquals("auth/register", result);
        verify(model).addAttribute(eq("signupRequest"), any(SignupRequest.class));
        verify(model).addAttribute("pageTitle", "Registrar Nuevo Usuario");
        verify(model).addAttribute("adminUser", "testuser");
    }

    // ========== TESTS PARA PROCESS WEB REGISTRATION ==========

    @Test
    @DisplayName("POST /auth/register-web - Registro exitoso")
    void processWebRegistration_ValidRequest_ShouldRedirectToAdminUsers() {
        // Given
        SignupRequest signupRequest = createValidSignupRequest();
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(authService.existsByEmail(anyString())).thenReturn(false);
        when(authService.registerUser(anyString(), anyString(), anyString(), any()))
            .thenReturn(mockUser);

        // When
        String result = authController.processWebRegistration(signupRequest, bindingResult, redirectAttributes, model, authentication);

        // Then
        assertEquals("redirect:/admin/users", result);
        verify(authService).registerUser(
            signupRequest.getUsername(),
            signupRequest.getEmail(), 
            signupRequest.getPassword(),
            signupRequest.getRole()
        );
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), 
            argThat(message -> message.toString().contains("registrado exitosamente")));
    }

    @Test
    @DisplayName("POST /auth/register-web - Con errores de validación")
    void processWebRegistration_WithValidationErrors_ShouldReturnRegisterView() {
        // Given
        SignupRequest signupRequest = createValidSignupRequest();
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String result = authController.processWebRegistration(signupRequest, bindingResult, redirectAttributes, model, authentication);

        // Then
        assertEquals("auth/register", result);
        verify(model).addAttribute("signupRequest", signupRequest);
        verify(model).addAttribute("pageTitle", "Registrar Nuevo Usuario");
        verify(model).addAttribute("adminUser", "testuser");
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/register-web - Usuario ya existe")
    void processWebRegistration_UserAlreadyExists_ShouldReturnRegisterViewWithError() {
        // Given
        SignupRequest signupRequest = createValidSignupRequest();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.existsByUsername(anyString())).thenReturn(true);

        // When
        String result = authController.processWebRegistration(signupRequest, bindingResult, redirectAttributes, model, authentication);

        // Then
        assertEquals("auth/register", result);
        verify(model).addAttribute(eq("errorMessage"), 
            eq("El nombre de usuario 'testuser' ya está en uso"));
        verify(model).addAttribute("signupRequest", signupRequest);
        verify(model).addAttribute("adminUser", "testuser");
    }

    @Test
    @DisplayName("POST /auth/register-web - Email ya existe")
    void processWebRegistration_EmailAlreadyExists_ShouldReturnRegisterViewWithError() {
        // Given
        SignupRequest signupRequest = createValidSignupRequest();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(authService.existsByEmail(anyString())).thenReturn(true);

        // When
        String result = authController.processWebRegistration(signupRequest, bindingResult, redirectAttributes, model, authentication);

        // Then
        assertEquals("auth/register", result);
        verify(model).addAttribute(eq("errorMessage"), 
            eq("El email 'test@example.com' ya está registrado"));
        verify(model).addAttribute("signupRequest", signupRequest);
        verify(model).addAttribute("adminUser", "testuser");
    }

    @Test
    @DisplayName("POST /auth/register-web - Error de role no encontrado")
    void processWebRegistration_RoleNotFound_ShouldReturnRegisterViewWithError() {
        // Given
        SignupRequest signupRequest = createValidSignupRequest();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(authService.existsByEmail(anyString())).thenReturn(false);
        when(authService.registerUser(anyString(), anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("Error: Role DOCTOR not found."));

        // When
        String result = authController.processWebRegistration(signupRequest, bindingResult, redirectAttributes, model, authentication);

        // Then
        assertEquals("auth/register", result);
        verify(model).addAttribute(eq("errorMessage"), 
            eq("Error en la configuración de roles del sistema. Contacte al administrador técnico"));
    }

    // ========== TESTS PARA ACCESS DENIED ==========

    @Test
    @DisplayName("GET /access-denied - Página de acceso denegado con usuario autenticado")
    void accessDenied_WithAuthenticatedUser_ShouldReturnAccessDeniedView() {
        // Given
        when(httpServletRequest.getRequestURI()).thenReturn("/admin/dashboard");

        // When
        String result = authController.accessDenied(model, httpServletRequest, authentication);

        // Then
        assertEquals("error/access-denied", result);
        verify(model).addAttribute("errorTitle", "Acceso Denegado");
        verify(model).addAttribute("errorMessage", "No tiene permisos para acceder a esta página");
        verify(model).addAttribute("requestedResource", "/admin/dashboard");
        verify(model).addAttribute("username", "testuser");
    }

    @Test
    @DisplayName("GET /access-denied - Página de acceso denegado sin usuario autenticado")
    void accessDenied_WithoutAuthenticatedUser_ShouldReturnAccessDeniedView() {
        // Given
        when(httpServletRequest.getRequestURI()).thenReturn("/admin/dashboard");

        // When
        String result = authController.accessDenied(model, httpServletRequest, null);

        // Then
        assertEquals("error/access-denied", result);
        verify(model).addAttribute("errorTitle", "Acceso Denegado");
        verify(model).addAttribute("errorMessage", "No tiene permisos para acceder a esta página");
        verify(model).addAttribute("requestedResource", "/admin/dashboard");
        verify(model).addAttribute("username", "Anónimo");
    }

    // ========== TESTS PARA ACCOUNT LOCKED ==========

    @Test
    @DisplayName("GET /account-locked - Cuenta bloqueada sin razón específica")
    void accountLocked_WithoutReason_ShouldReturnAccountLockedView() {
        // When
        String result = authController.accountLocked(null, model);

        // Then
        assertEquals("auth/account-locked", result);
        verify(model).addAttribute("pageTitle", "Cuenta Bloqueada");
        verify(model).addAttribute("lockMessage", "Su cuenta no está disponible en este momento");
    }

    @Test
    @DisplayName("GET /account-locked - Cuenta inactiva")
    void accountLocked_WithInactiveReason_ShouldReturnAccountLockedView() {
        // When
        String result = authController.accountLocked("inactive", model);

        // Then
        assertEquals("auth/account-locked", result);
        verify(model).addAttribute("pageTitle", "Cuenta Bloqueada");
        verify(model).addAttribute("lockMessage", "Su cuenta ha sido desactivada por un administrador");
    }

    // ========== TESTS PARA CHANGE PASSWORD ==========

    @Test
    @DisplayName("GET /change-password - Página de cambio de contraseña normal")
    void changePasswordPage_Normal_ShouldReturnChangePasswordView() {
        // Given
        when(authService.isFirstLogin("testuser")).thenReturn(false);

        // When
        String result = authController.changePasswordPage(null, null, model, authentication);

        // Then
        assertEquals("auth/change-password", result);
        verify(model).addAttribute("pageTitle", "Cambiar Contraseña");
        verify(model).addAttribute("username", "testuser");
    }

    @Test
    @DisplayName("GET /change-password - Con primer login")
    void changePasswordPage_WithFirstLogin_ShouldReturnChangePasswordViewWithMessage() {
        // Given
        when(authService.isFirstLogin("testuser")).thenReturn(true);

        // When
        String result = authController.changePasswordPage("true", null, model, authentication);

        // Then
        assertEquals("auth/change-password", result);
        verify(model).addAttribute("pageTitle", "Cambiar Contraseña");
        verify(model).addAttribute("username", "testuser");
        verify(model).addAttribute("firstLoginMessage", 
            "Bienvenido al sistema. Por seguridad, debe cambiar su contraseña inicial");
        verify(model).addAttribute("isFirstLogin", true);
    }

    @Test
    @DisplayName("GET /change-password - Con parámetro forced")
    void changePasswordPage_WithForcedChange_ShouldReturnChangePasswordViewWithMessage() {
        // Given
        when(authService.isFirstLogin("testuser")).thenReturn(false);

        // When
        String result = authController.changePasswordPage(null, "true", model, authentication);

        // Then
        assertEquals("auth/change-password", result);
        verify(model).addAttribute("forcedMessage", 
            "El administrador requiere que cambie su contraseña antes de continuar");
    }

    @Test
    @DisplayName("POST /auth/change-password - Cambio exitoso")
    void processPasswordChange_ValidPasswords_ShouldRedirectToDashboard() {
        // Given
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        when(authService.isFirstLogin("testuser")).thenReturn(false);
        doNothing().when(authService).changePassword("testuser", currentPassword, newPassword);

        // When
        String result = authController.processPasswordChange(
            currentPassword, newPassword, confirmPassword, redirectAttributes, model, authentication);

        // Then
        assertEquals("redirect:/dashboard", result);
        verify(authService).changePassword("testuser", currentPassword, newPassword);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), 
            eq("Contraseña cambiada exitosamente. Su sesión seguirá activa."));
    }

    @Test
    @DisplayName("POST /auth/change-password - Cambio exitoso en primer login")
    void processPasswordChange_FirstLogin_ShouldRedirectToDashboardWithWelcome() {
        // Given
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        when(authService.isFirstLogin("testuser")).thenReturn(true);
        doNothing().when(authService).changePassword("testuser", currentPassword, newPassword);

        // When
        String result = authController.processPasswordChange(
            currentPassword, newPassword, confirmPassword, redirectAttributes, model, authentication);

        // Then
        assertEquals("redirect:/dashboard?welcome=true", result);
        verify(authService).changePassword("testuser", currentPassword, newPassword);
    }

    @Test
    @DisplayName("POST /auth/change-password - Contraseña actual vacía")
    void processPasswordChange_EmptyCurrentPassword_ShouldReturnChangePasswordViewWithError() {
        // Given
        String currentPassword = "";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";

        // When
        String result = authController.processPasswordChange(
            currentPassword, newPassword, confirmPassword, redirectAttributes, model, authentication);

        // Then
        assertEquals("auth/change-password", result);
        verify(model).addAttribute("errorMessage", "La contraseña actual es requerida");
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/change-password - Contraseña muy corta")
    void processPasswordChange_ShortPassword_ShouldReturnChangePasswordViewWithError() {
        // Given
        String currentPassword = "oldPassword123";
        String newPassword = "123"; // Muy corta
        String confirmPassword = "123";

        // When
        String result = authController.processPasswordChange(
            currentPassword, newPassword, confirmPassword, redirectAttributes, model, authentication);

        // Then
        assertEquals("auth/change-password", result);
        verify(model).addAttribute("errorMessage", "La nueva contraseña debe tener al menos 6 caracteres");
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/change-password - Contraseñas no coinciden")
    void processPasswordChange_PasswordsDontMatch_ShouldReturnChangePasswordViewWithError() {
        // Given
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String confirmPassword = "differentPassword123";

        // When
        String result = authController.processPasswordChange(
            currentPassword, newPassword, confirmPassword, redirectAttributes, model, authentication);

        // Then
        assertEquals("auth/change-password", result);
        verify(model).addAttribute("errorMessage", "Las contraseñas nuevas no coinciden");
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/change-password - Misma contraseña")
    void processPasswordChange_SamePassword_ShouldReturnChangePasswordViewWithError() {
        // Given
        String currentPassword = "password123";
        String newPassword = "password123";
        String confirmPassword = "password123";

        // When
        String result = authController.processPasswordChange(
            currentPassword, newPassword, confirmPassword, redirectAttributes, model, authentication);

        // Then
        assertEquals("auth/change-password", result);
        verify(model).addAttribute("errorMessage", "La nueva contraseña debe ser diferente a la actual");
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/change-password - Contraseña actual incorrecta")
    void processPasswordChange_WrongCurrentPassword_ShouldReturnChangePasswordViewWithError() {
        // Given
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        doThrow(new RuntimeException("La contraseña actual es incorrecta"))
            .when(authService).changePassword("testuser", currentPassword, newPassword);

        // When
        String result = authController.processPasswordChange(
            currentPassword, newPassword, confirmPassword, redirectAttributes, model, authentication);

        // Then
        assertEquals("auth/change-password", result);
        verify(model).addAttribute("errorMessage", "La contraseña actual ingresada es incorrecta");
        verify(model).addAttribute("username", "testuser");
    }

    // ========== TESTS PARA FORGOT PASSWORD ==========

    @Test
    @DisplayName("GET /forgot-password - Página de recuperación")
    void forgotPasswordPage_ShouldReturnForgotPasswordView() {
        // When
        String result = authController.forgotPasswordPage(model);

        // Then
        assertEquals("auth/forgot-password", result);
        verify(model).addAttribute("pageTitle", "Recuperar Contraseña");
    }

    @Test
    @DisplayName("POST /auth/forgot-password - Solicitud exitosa")
    void processForgotPassword_ValidEmail_ShouldRedirectToLogin() {
        // Given
        String email = "test@example.com";
        doNothing().when(authService).sendPasswordResetEmail(email);

        // When
        String result = authController.processForgotPassword(email, redirectAttributes, model);

        // Then
        assertEquals("redirect:/login", result);
        verify(authService).sendPasswordResetEmail(email);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), 
            argThat(message -> message.toString().contains("recibirá instrucciones")));
    }

    @Test
    @DisplayName("POST /auth/forgot-password - Email inválido")
    void processForgotPassword_InvalidEmail_ShouldReturnForgotPasswordViewWithError() {
        // Given
        String email = "invalid-email";

        // When
        String result = authController.processForgotPassword(email, redirectAttributes, model);

        // Then
        assertEquals("auth/forgot-password", result);
        verify(model).addAttribute("errorMessage", "Por favor ingrese un email válido");
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/forgot-password - Email vacío")
    void processForgotPassword_EmptyEmail_ShouldReturnForgotPasswordViewWithError() {
        // Given
        String email = "";

        // When
        String result = authController.processForgotPassword(email, redirectAttributes, model);

        // Then
        assertEquals("auth/forgot-password", result);
        verify(model).addAttribute("errorMessage", "Por favor ingrese un email válido");
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/forgot-password - Error en servicio")
    void processForgotPassword_ServiceError_ShouldRedirectToLoginWithSuccessMessage() {
        // Given
        String email = "test@example.com";
        doThrow(new RuntimeException("Service error"))
            .when(authService).sendPasswordResetEmail(email);

        // When
        String result = authController.processForgotPassword(email, redirectAttributes, model);

        // Then
        assertEquals("redirect:/login", result);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), 
            argThat(message -> message.toString().contains("recibirá instrucciones")));
    }

    // ========== TESTS PARA RESET PASSWORD ==========

    @Test
    @DisplayName("GET /reset-password - Con token válido")
    void resetPasswordPage_WithToken_ShouldReturnResetPasswordView() {
        // Given
        String token = "valid-token-123";

        // When
        String result = authController.resetPasswordPage(token, model);

        // Then
        assertEquals("auth/reset-password", result);
        verify(model).addAttribute("pageTitle", "Restablecer Contraseña");
        verify(model).addAttribute("token", token);
    }

    @Test
    @DisplayName("GET /reset-password - Sin token")
    void resetPasswordPage_WithoutToken_ShouldRedirectToForgotPassword() {
        // When
        String result = authController.resetPasswordPage(null, model);

        // Then
        assertEquals("redirect:/forgot-password", result);
        verifyNoInteractions(model);
    }

    @Test
    @DisplayName("GET /reset-password - Token vacío")
    void resetPasswordPage_WithEmptyToken_ShouldRedirectToForgotPassword() {
        // When
        String result = authController.resetPasswordPage("", model);

        // Then
        assertEquals("redirect:/forgot-password", result);
        verifyNoInteractions(model);
    }

    // ========== TESTS PARA AJAX ERROR ==========

    @Test
    @DisplayName("GET/POST /auth/ajax-error - Sin tipo")
    void ajaxAuthError_WithoutType_ShouldReturnExpiredErrorJson() {
        // When
        String result = authController.ajaxAuthError(null);

        // Then
        assertEquals("{\"error\": \"Sesión expirada\", \"redirect\": \"/login?expired=true\"}", result);
    }

    @Test
    @DisplayName("GET/POST /auth/ajax-error - Tipo unauthorized")
    void ajaxAuthError_WithUnauthorizedType_ShouldReturnUnauthorizedErrorJson() {
        // When
        String result = authController.ajaxAuthError("unauthorized");

        // Then
        assertEquals("{\"error\": \"No autorizado\", \"redirect\": \"/login?error=true\"}", result);
    }

    @Test
    @DisplayName("GET/POST /auth/ajax-error - Tipo forbidden")
    void ajaxAuthError_WithForbiddenType_ShouldReturnForbiddenErrorJson() {
        // When
        String result = authController.ajaxAuthError("forbidden");

        // Then
        assertEquals("{\"error\": \"Acceso denegado\", \"redirect\": \"/access-denied\"}", result);
    }

    // ========== TESTS PARA CHECK USERNAME ==========

    @Test
    @DisplayName("GET /auth/check-username - Username disponible")
    void checkUsernameAvailability_Available_ShouldReturnAvailableJson() {
        // Given
        String username = "newuser";
        when(authService.existsByUsername(username)).thenReturn(false);

        // When
        String result = authController.checkUsernameAvailability(username);

        // Then
        assertEquals("{\"available\": true, \"message\": \"Username disponible\"}", result);
        verify(authService).existsByUsername(username);
    }

    @Test
    @DisplayName("GET /auth/check-username - Username no disponible")
    void checkUsernameAvailability_NotAvailable_ShouldReturnNotAvailableJson() {
        // Given
        String username = "existinguser";
        when(authService.existsByUsername(username)).thenReturn(true);

        // When
        String result = authController.checkUsernameAvailability(username);

        // Then
        assertEquals("{\"available\": false, \"message\": \"Username no disponible\"}", result);
        verify(authService).existsByUsername(username);
    }

    @Test
    @DisplayName("GET /auth/check-username - Username muy corto")
    void checkUsernameAvailability_TooShort_ShouldReturnErrorJson() {
        // Given
        String username = "ab";

        // When
        String result = authController.checkUsernameAvailability(username);

        // Then
        assertEquals("{\"available\": false, \"message\": \"Username debe tener al menos 3 caracteres\"}", result);
        verifyNoInteractions(authService);
    }

    // ========== TESTS PARA CHECK EMAIL ==========

    @Test
    @DisplayName("GET /auth/check-email - Email disponible")
    void checkEmailAvailability_Available_ShouldReturnAvailableJson() {
        // Given
        String email = "new@example.com";
        when(authService.existsByEmail(email)).thenReturn(false);

        // When
        String result = authController.checkEmailAvailability(email);

        // Then
        assertEquals("{\"available\": true, \"message\": \"Email disponible\"}", result);
        verify(authService).existsByEmail(email);
    }

    @Test
    @DisplayName("GET /auth/check-email - Email no disponible")
    void checkEmailAvailability_NotAvailable_ShouldReturnNotAvailableJson() {
        // Given
        String email = "existing@example.com";
        when(authService.existsByEmail(email)).thenReturn(true);

        // When
        String result = authController.checkEmailAvailability(email);

        // Then
        assertEquals("{\"available\": false, \"message\": \"Email ya registrado\"}", result);
        verify(authService).existsByEmail(email);
    }

    @Test
    @DisplayName("GET /auth/check-email - Email inválido")
    void checkEmailAvailability_Invalid_ShouldReturnErrorJson() {
        // Given
        String email = "invalid-email";

        // When
        String result = authController.checkEmailAvailability(email);

        // Then
        assertEquals("{\"available\": false, \"message\": \"Email inválido\"}", result);
        assertTrue(result.contains("\"available\": false"));
        verifyNoInteractions(authService);
    }

    // ========== TESTS ADICIONALES CON ASSERTIONS ==========

    @Test
    @DisplayName("POST /auth/register-web - Validar contenido del mensaje de éxito")
    void processWebRegistration_Success_ShouldContainExpectedMessageContent() {
        // Given
        SignupRequest signupRequest = createValidSignupRequest();
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(authService.existsByEmail(anyString())).thenReturn(false);
        when(authService.registerUser(anyString(), anyString(), anyString(), any()))
            .thenReturn(mockUser);

        // When
        String result = authController.processWebRegistration(signupRequest, bindingResult, redirectAttributes, model, authentication);

        // Then
        assertEquals("redirect:/admin/users", result);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), 
            argThat(message -> {
                String msg = message.toString();
                assertTrue(msg.contains("registrado exitosamente"));
                assertTrue(msg.contains("testuser"));
                return true;
            }));
    }

    @Test
    @DisplayName("GET /login - Validar estructura de respuesta JSON AJAX error")
    void ajaxAuthError_ShouldContainRequiredJsonFields() {
        // When
        String result = authController.ajaxAuthError("expired");

        // Then
        assertTrue(result.contains("\"error\":"));
        assertTrue(result.contains("\"redirect\":"));
        assertTrue(result.contains("Sesión expirada"));
        assertTrue(result.startsWith("{") && result.endsWith("}"));
    }

    @Test
    @DisplayName("POST /auth/change-password - Validar mensaje de contraseña cambiada")
    void processPasswordChange_Success_ShouldContainSuccessMessage() {
        // Given
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        when(authService.isFirstLogin("testuser")).thenReturn(false);
        doNothing().when(authService).changePassword("testuser", currentPassword, newPassword);

        // When
        String result = authController.processPasswordChange(
            currentPassword, newPassword, confirmPassword, redirectAttributes, model, authentication);

        // Then
        assertEquals("redirect:/dashboard", result);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), 
            argThat(message -> {
                String msg = message.toString();
                assertTrue(msg.contains("cambiada exitosamente"));
                assertTrue(msg.contains("sesión seguirá activa"));
                return true;
            }));
    }

    @Test
    @DisplayName("GET /auth/check-username - Validar formato JSON de respuesta")
    void checkUsernameAvailability_ShouldReturnValidJsonFormat() {
        // Given
        String username = "validuser";
        when(authService.existsByUsername(username)).thenReturn(false);

        // When
        String result = authController.checkUsernameAvailability(username);

        // Then
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        assertTrue(result.contains("\"available\":"));
        assertTrue(result.contains("\"message\":"));
        assertTrue(result.contains("true"));
    }

    @Test
    @DisplayName("POST /auth/forgot-password - Validar mensaje de seguridad")
    void processForgotPassword_ShouldAlwaysShowSecurityMessage() {
        // Given
        String email = "test@example.com";
        doNothing().when(authService).sendPasswordResetEmail(email);

        // When
        String result = authController.processForgotPassword(email, redirectAttributes, model);

        // Then
        assertEquals("redirect:/login", result);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), 
            argThat(message -> {
                String msg = message.toString();
                assertTrue(msg.contains("recibirá instrucciones"));
                assertTrue(msg.contains("próximos minutos"));
                assertTrue(msg.contains("carpeta de spam"));
                return true;
            }));
    }

    // ========== MÉTODO HELPER ==========

    private SignupRequest createValidSignupRequest() {
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole(Set.of("DOCTOR"));
        return request;
    }
}