package com.example.miapp.controller.auth;

import com.example.miapp.dto.auth.SignupRequest;
import com.example.miapp.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de autenticación completo para vistas web (Thymeleaf)
 * 100% integrado con AuthService - Maneja login, registro, cambio de contraseña y recuperación
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // ========== PÁGINAS DE AUTENTICACIÓN ==========

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

    @GetMapping("/auth/logout")
    public String logoutPage() {
        return "auth/logout";
    }

    /**
     * Página de registro - accesible sin autenticación
     * GET /auth/register
     */
    @GetMapping("/auth/register")
    public String showRegisterPage(Model model) {
        log.info("Accediendo a página pública de registro");
        
        // Si no existe un objeto signupRequest en el modelo, crear uno nuevo
        if (!model.containsAttribute("signupRequest")) {
            model.addAttribute("signupRequest", new SignupRequest());
        }
        
        model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
        
        return "auth/register";
    }

    /**
     * Página de registro (solo para administradores)
     * GET /register
     */
    @GetMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerPage(Model model, Authentication authentication) {
        log.info("Administrador {} accediendo a página de registro", authentication.getName());
        
        model.addAttribute("signupRequest", new SignupRequest());
        model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
        model.addAttribute("adminUser", authentication.getName());
        
        return "auth/register";
    }

    /**
     * Procesar registro de usuario (accesible sin autenticación)
     * POST /auth/register
     */
    @PostMapping("/auth/register")
    public String processRegistration(@Valid @ModelAttribute SignupRequest signupRequest,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        
        log.info("Procesando registro público de usuario: {}", signupRequest.getUsername());
        
        // Si hay errores de validación, volver al formulario
        if (result.hasErrors()) {
            log.warn("Errores de validación en registro público: {}", result.getAllErrors());
            
            model.addAttribute("signupRequest", signupRequest);
            model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
            
            return "register";
        }
        
        try {
            // Validaciones adicionales de negocio
            if (authService.existsByUsername(signupRequest.getUsername())) {
                model.addAttribute("errorMessage", 
                    "El nombre de usuario '" + signupRequest.getUsername() + "' ya está en uso");
                model.addAttribute("signupRequest", signupRequest);
                model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
                return "register";
            }

            if (authService.existsByEmail(signupRequest.getEmail())) {
                model.addAttribute("errorMessage", 
                    "El email '" + signupRequest.getEmail() + "' ya está registrado");
                model.addAttribute("signupRequest", signupRequest);
                model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
                return "register";
            }
            
            // Por defecto, asignar solo rol de paciente para registro público
            Set<String> roles = new HashSet<>();
            roles.add("patient");
            
            // Procesar registro usando AuthService
            var newUser = authService.registerUser(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                signupRequest.getPassword(),
                roles
            );
            
            log.info("Usuario {} registrado exitosamente con ID: {}", 
                    signupRequest.getUsername(), newUser.getId());
            
            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("successMessage", 
                "Su cuenta ha sido creada exitosamente. Ahora puede iniciar sesión.");
            
            return "redirect:/login?success=true";
            
        } catch (RuntimeException e) {
            log.error("Error al registrar usuario {}: {}", 
                    signupRequest.getUsername(), e.getMessage());
            
            // Manejo de errores específicos con mensajes amigables
            String errorMessage = switch (e.getMessage()) {
                case "El nombre de usuario ya existe" -> 
                    "El nombre de usuario '" + signupRequest.getUsername() + "' ya está en uso";
                case "El email ya está en uso" -> 
                    "El email '" + signupRequest.getEmail() + "' ya está registrado";
                case "Error: Role PATIENT not found." ->
                    "Error en la configuración de roles del sistema. Contacte al administrador técnico";
                default -> "Error al registrar el usuario: " + e.getMessage();
            };
            
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("signupRequest", signupRequest);
            model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
            
            return "register";
        }
    }

    /**
     * Procesar registro de usuario (solo administradores)
     * POST /auth/register-web
     */
    @PostMapping("/auth/register-web")
    @PreAuthorize("hasRole('ADMIN')")
    public String processWebRegistration(@Valid @ModelAttribute SignupRequest signupRequest,
                                       BindingResult result,
                                       RedirectAttributes redirectAttributes,
                                       Model model,
                                       Authentication authentication) {
        
        log.info("Administrador {} procesando registro de usuario: {}", 
                authentication.getName(), signupRequest.getUsername());
        
        // Si hay errores de validación, volver al formulario
        if (result.hasErrors()) {
            log.warn("Errores de validación en registro por admin {}: {}", 
                    authentication.getName(), result.getAllErrors());
            
            model.addAttribute("signupRequest", signupRequest);
            model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
            model.addAttribute("adminUser", authentication.getName());
            
            return "auth/register";
        }
        
        try {
            // Validaciones adicionales de negocio
            if (authService.existsByUsername(signupRequest.getUsername())) {
                model.addAttribute("errorMessage", 
                    "El nombre de usuario '" + signupRequest.getUsername() + "' ya está en uso");
                model.addAttribute("signupRequest", signupRequest);
                model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
                model.addAttribute("adminUser", authentication.getName());
                return "auth/register";
            }

            if (authService.existsByEmail(signupRequest.getEmail())) {
                model.addAttribute("errorMessage", 
                    "El email '" + signupRequest.getEmail() + "' ya está registrado");
                model.addAttribute("signupRequest", signupRequest);
                model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
                model.addAttribute("adminUser", authentication.getName());
                return "auth/register";
            }
            
            // Procesar registro usando AuthService
            var newUser = authService.registerUser(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                signupRequest.getPassword(),
                signupRequest.getRole()
            );
            
            log.info("Usuario {} registrado exitosamente por admin {} con ID: {}", 
                    signupRequest.getUsername(), authentication.getName(), newUser.getId());
            
            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Usuario '%s' registrado exitosamente con roles: %s", 
                    signupRequest.getUsername(), 
                    signupRequest.getRole() != null ? signupRequest.getRole() : "[PATIENT]"));
            
            return "redirect:/admin/users";
            
        } catch (RuntimeException e) {
            log.error("Error al registrar usuario {} por admin {}: {}", 
                    signupRequest.getUsername(), authentication.getName(), e.getMessage());
            
            // Manejo de errores específicos con mensajes amigables
            String errorMessage = switch (e.getMessage()) {
                case "El nombre de usuario ya existe" -> 
                    "El nombre de usuario '" + signupRequest.getUsername() + "' ya está en uso";
                case "El email ya está en uso" -> 
                    "El email '" + signupRequest.getEmail() + "' ya está registrado";
                case "Error: Role ADMIN not found.", "Error: Role DOCTOR not found.", "Error: Role PATIENT not found." ->
                    "Error en la configuración de roles del sistema. Contacte al administrador técnico";
                default -> "Error al registrar el usuario: " + e.getMessage();
            };
            
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("signupRequest", signupRequest);
            model.addAttribute("pageTitle", "Registrar Nuevo Usuario");
            model.addAttribute("adminUser", authentication.getName());
            
            return "auth/register";
        }
    }

    // ========== GESTIÓN DE CONTRASEÑAS ==========

    /**
     * Página de cambio de contraseña
     * GET /change-password
     */
    @GetMapping("/change-password")
    
    public String changePasswordPage(@RequestParam(required = false) String firstLogin,
                                   @RequestParam(required = false) String forced,
                                   Model model,
                                   Authentication authentication) {
        
        log.info("Usuario {} accediendo a página de cambio de contraseña", authentication.getName());
        
        // Verificar si es primer login
        boolean isFirstLogin = authService.isFirstLogin(authentication.getName());
        
        if (firstLogin != null || isFirstLogin) {
            model.addAttribute("firstLoginMessage", 
                "Bienvenido al sistema. Por seguridad, debe cambiar su contraseña inicial");
            model.addAttribute("isFirstLogin", true);
        }

        if (forced != null) {
            model.addAttribute("forcedMessage", 
                "El administrador requiere que cambie su contraseña antes de continuar");
        }
        
        model.addAttribute("pageTitle", "Cambiar Contraseña");
        model.addAttribute("username", authentication.getName());
        
        return "change-password";
    }

    /**
     * Procesar cambio de contraseña - INTEGRADO CON AUTHSERVICE
     * POST /auth/change-password
     */
    @PostMapping("/auth/change-password")
    public String processPasswordChange(@RequestParam String currentPassword,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                      RedirectAttributes redirectAttributes,
                                      Model model,
                                      Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Procesando cambio de contraseña para usuario: {}", username);
        
        // Validaciones del lado servidor
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            model.addAttribute("errorMessage", "La contraseña actual es requerida");
            return "change-password";
        }

        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("errorMessage", "La nueva contraseña debe tener al menos 6 caracteres");
            return "change-password";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Las contraseñas nuevas no coinciden");
            return "change-password";
        }

        if (currentPassword.equals(newPassword)) {
            model.addAttribute("errorMessage", "La nueva contraseña debe ser diferente a la actual");
            return "change-password";
        }
        
        try {
            // Usar AuthService real para cambiar contraseña
            authService.changePassword(username, currentPassword, newPassword);
            
            log.info("Contraseña cambiada exitosamente para usuario: {}", username);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Contraseña cambiada exitosamente. Su sesión seguirá activa.");
            
            // Redirigir según si era primer login o no
            boolean wasFirstLogin = authService.isFirstLogin(username);
            if (wasFirstLogin) {
                return "redirect:/dashboard?welcome=true";
            } else {
                return "redirect:/dashboard";
            }
            
        } catch (RuntimeException e) {
            log.error("Error al cambiar contraseña para usuario {}: {}", username, e.getMessage());
            
            String errorMessage = switch (e.getMessage()) {
                case "La contraseña actual es incorrecta" -> 
                    "La contraseña actual ingresada es incorrecta";
                case "La nueva contraseña debe ser diferente a la actual" ->
                    "La nueva contraseña debe ser diferente a la contraseña actual";
                case "Usuario no encontrado" ->
                    "Error del sistema. Su sesión puede haber expirado";
                default -> "Error al cambiar la contraseña: " + e.getMessage();
            };
            
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("username", username);
            return "change-password";
        }
    }

    // ========== RECUPERACIÓN DE CONTRASEÑA ==========

    /**
     * Página de recuperación de contraseña
     * GET /forgot-password
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        log.info("Acceso a página de recuperación de contraseña");
        
        model.addAttribute("pageTitle", "Recuperar Contraseña");
        
        return "forgot-password";
    }

    /**
     * Procesar solicitud de recuperación de contraseña - INTEGRADO CON AUTHSERVICE
     * POST /auth/forgot-password
     */
    @PostMapping("/auth/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        
        log.info("Procesando recuperación de contraseña para email: {}", email);
        
        // Validación básica de email
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            model.addAttribute("errorMessage", "Por favor ingrese un email válido");
            return "forgot-password";
        }
        
        try {
            // Usar AuthService real para enviar recuperación
            authService.sendPasswordResetEmail(email.trim().toLowerCase());
            
            log.info("Solicitud de recuperación procesada para email: {}", email);
            
            // Siempre mostrar el mismo mensaje por seguridad
            redirectAttributes.addFlashAttribute("successMessage", 
                "Si el email existe en nuestro sistema, recibirá instrucciones para restablecer su contraseña en los próximos minutos. Revise también su carpeta de spam.");
            
            return "redirect:/login";
            
        } catch (Exception e) {
            log.error("Error al procesar recuperación de contraseña para {}: {}", email, e.getMessage());
            
            // Por seguridad, siempre mostrar el mismo mensaje exitoso
            redirectAttributes.addFlashAttribute("successMessage", 
                "Si el email existe en nuestro sistema, recibirá instrucciones para restablecer su contraseña en los próximos minutos. Revise también su carpeta de spam.");
            
            return "redirect:/login";
        }
    }

    /**
     * Página de restablecimiento con token (futuro)
     * GET /reset-password
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token,
                                   Model model) {
        
        log.info("Acceso a página de restablecimiento de contraseña con token");
        
        if (token == null || token.trim().isEmpty()) {
            log.warn("Intento de acceso a reset sin token");
            return "redirect:/forgot-password";
        }
        
        // TODO: Validar token en implementación futura
        
        model.addAttribute("pageTitle", "Restablecer Contraseña");
        model.addAttribute("token", token);
        
        return "reset-password";
    }

    // ========== PÁGINAS DE ERROR Y ACCESO ==========

    /**
     * Página de acceso denegado
     * GET /access-denied
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model, HttpServletRequest request, Authentication authentication) {
        
        String username = authentication != null ? authentication.getName() : "Anónimo";
        log.warn("Acceso denegado para usuario {} desde IP: {} a recurso: {}", 
                username, getClientIpAddress(request), request.getRequestURI());
        
        model.addAttribute("errorTitle", "Acceso Denegado");
        model.addAttribute("errorMessage", "No tiene permisos para acceder a esta página");
        model.addAttribute("requestedResource", request.getRequestURI());
        model.addAttribute("username", username);
        
        return "error/access-denied";
    }

    /**
     * Página de cuenta bloqueada
     * GET /account-locked
     */
    @GetMapping("/account-locked")
    public String accountLocked(@RequestParam(required = false) String reason,
                               Model model) {
        
        log.info("Acceso a página de cuenta bloqueada");
        
        model.addAttribute("pageTitle", "Cuenta Bloqueada");
        
        String message = switch (reason != null ? reason : "general") {
            case "inactive" -> "Su cuenta ha sido desactivada por un administrador";
            case "expired" -> "Su cuenta ha expirado. Contacte al administrador";
            case "security" -> "Su cuenta ha sido bloqueada por razones de seguridad";
            default -> "Su cuenta no está disponible en este momento";
        };
        
        model.addAttribute("lockMessage", message);
        
        return "account-locked";
    }

    // ========== ENDPOINTS AJAX PARA SPA ==========

    /**
     * Manejo de errores de autenticación para AJAX
     * GET/POST /auth/ajax-error
     */
    @RequestMapping(value = "/auth/ajax-error", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String ajaxAuthError(@RequestParam(required = false) String type) {
        
        String errorType = type != null ? type : "expired";
        
        return switch (errorType) {
            case "expired" -> "{\"error\": \"Sesión expirada\", \"redirect\": \"/login?expired=true\"}";
            case "unauthorized" -> "{\"error\": \"No autorizado\", \"redirect\": \"/login?error=true\"}"; 
            case "forbidden" -> "{\"error\": \"Acceso denegado\", \"redirect\": \"/access-denied\"}";
            default -> "{\"error\": \"Error de autenticación\", \"redirect\": \"/login\"}";
        };
    }

    // ========== UTILIDADES Y VALIDACIONES ==========

    /**
     * Endpoint para validar disponibilidad de username (AJAX)
     * GET /auth/check-username
     */
    @GetMapping("/auth/check-username")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public String checkEmailAvailability(@RequestParam String email) {
        
        if (email == null || !email.contains("@")) {
            return "{\"available\": false, \"message\": \"Email inválido\"}";
        }
        
        boolean exists = authService.existsByEmail(email.trim().toLowerCase());
        
        return String.format("{\"available\": %s, \"message\": \"%s\"}", 
            !exists, 
            exists ? "Email ya registrado" : "Email disponible");
    }

    // ========== MÉTODOS PRIVADOS UTILITARIOS ==========

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