package com.example.miapp.controller;

import com.example.miapp.dto.auth.JwtResponse;
import com.example.miapp.dto.auth.LoginRequest;
import com.example.miapp.dto.auth.MessageResponse;
import com.example.miapp.entity.User;
import com.example.miapp.repository.UserRepository;
import com.example.miapp.security.JwtTokenProvider;
import com.example.miapp.security.UserDetailsImpl;
import com.example.miapp.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para manejar la autenticación de usuarios.
 * Proporciona endpoints para inicio de sesión, cierre de sesión y renovación de tokens.
 * Identifica roles de usuario (admin, doctor, paciente).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * Endpoint para iniciar sesión y obtener un token JWT.
     * Identifica el tipo de usuario (admin, doctor, paciente) basado en sus roles.
     *
     * @param loginRequest Credenciales de inicio de sesión
     * @param response HttpServletResponse para configurar cookies
     * @return ResponseEntity con el token JWT y la información del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletResponse response) {
        log.info("Intento de login para usuario: {}", loginRequest.getUsername());
        
        try {
            // Autenticar contra Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // Establecer la autenticación en el contexto
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generar token JWT
            String jwt = tokenProvider.generateToken(authentication);
            
            // Generar refresh token (opcional, depende de la implementación)
            // String refreshToken = tokenProvider.generateRefreshToken(authentication);
            String refreshToken = ""; // Placeholder
            
            // Obtener detalles del usuario autenticado
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Obtener roles del usuario
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            
            // Identificar tipo de usuario basado en roles
            String userType = identifyUserType(roles);
            
            // Actualizar información de último login
            authService.updateLastLogin(userDetails.getUsername());
            
            // Opcional: Configurar cookie con el token
            Cookie jwtCookie = new Cookie("jwt_token", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true); // Requiere HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600); // 1 hora o lo que corresponda a la expiración del token
            response.addCookie(jwtCookie);
            
            log.info("Login exitoso para usuario: {} ({})", loginRequest.getUsername(), userType);
            
            // Crear respuesta con tipo de usuario
            JwtResponse jwtResponse = new JwtResponse(
                    jwt,
                    refreshToken,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles
            );
            
            // Añadir información adicional
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", jwtResponse.getToken());
            responseBody.put("refreshToken", jwtResponse.getRefreshToken());
            responseBody.put("id", jwtResponse.getId());
            responseBody.put("username", jwtResponse.getUsername());
            responseBody.put("email", jwtResponse.getEmail());
            responseBody.put("roles", jwtResponse.getRoles());
            responseBody.put("userType", userType);
            
            // Devolver respuesta con token y datos del usuario
            return ResponseEntity.ok(responseBody);
                    
        } catch (Exception e) {
            log.error("Error en autenticación para usuario {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Error de autenticación: Credenciales inválidas"));
        }
    }

    /**
     * Endpoint para cerrar sesión.
     *
     * @param request HttpServletRequest para obtener el token actual
     * @param response HttpServletResponse para eliminar cookies
     * @return ResponseEntity con mensaje de éxito
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // Obtener el token actual
        String jwt = tokenProvider.resolveToken(request);
        
        if (jwt != null) {
            // Opcional: Añadir token a una lista negra o invalidarlo
            // tokenBlacklistService.addToBlacklist(jwt);
            
            // Limpiar el contexto de seguridad
            SecurityContextHolder.clearContext();
            
            // Eliminar cookie si existe
            Cookie cookie = new Cookie("jwt_token", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            
            log.info("Logout exitoso");
            
            return ResponseEntity.ok(new MessageResponse("Sesión cerrada exitosamente"));
        }
        
        return ResponseEntity.ok(new MessageResponse("No hay sesión activa"));
    }
    
    /**
     * Endpoint para renovar el token JWT.
     * Este endpoint debería requerir autenticación.
     *
     * @param request HttpServletRequest para obtener el token actual
     * @return ResponseEntity con el nuevo token JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        // Obtener el token actual
        String jwt = tokenProvider.resolveToken(request);
        
        if (jwt != null && tokenProvider.validateToken(jwt)) {
            // Obtener el nombre de usuario del token
            String username = tokenProvider.getUsernameFromToken(jwt);
            
            // Cargar el usuario
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Crear autenticación manualmente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Generar nuevo token
            String newToken = tokenProvider.generateToken(authentication);
            
            // Obtener roles del usuario
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList());
            
            // Identificar tipo de usuario
            String userType = identifyUserType(roles);
            
            log.info("Token renovado para usuario: {} ({})", username, userType);
            
            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("refreshToken", "");
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("roles", roles);
            response.put("userType", userType);
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("No se pudo renovar el token"));
    }
    
    /**
     * Endpoint para verificar si un token es válido.
     * Útil para clientes que quieren verificar la validez de su token actual.
     *
     * @param request HttpServletRequest para obtener el token
     * @return ResponseEntity con información sobre la validez del token
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String jwt = tokenProvider.resolveToken(request);
        
        if (jwt != null) {
            boolean isValid = tokenProvider.validateToken(jwt);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                response.put("username", username);
                
                // Opcionalmente, determinar el tipo de usuario
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());
                    
                    response.put("roles", roles);
                    response.put("userType", identifyUserType(roles));
                }
            }
            
            return ResponseEntity.ok(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("message", "No se proporcionó token");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para obtener información del usuario autenticado.
     * Requiere autenticación.
     *
     * @param authentication Authentication del contexto de seguridad
     * @return ResponseEntity con la información del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Map<String, Object> userInfo = authService.getUserInfoFromAuth(authentication);
            
            // Añadir el tipo de usuario basado en roles
            if (userInfo.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) userInfo.get("roles");
                userInfo.put("userType", identifyUserType(roles));
            }
            
            return ResponseEntity.ok(userInfo);
        }
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("No autenticado"));
    }
    
    /**
     * Identifica el tipo de usuario basado en sus roles.
     * Prioriza Admin > Doctor > Paciente en caso de múltiples roles.
     *
     * @param roles Lista de roles del usuario
     * @return String indicando el tipo de usuario (ADMIN, DOCTOR, PATIENT, UNKNOWN)
     */
    private String identifyUserType(List<String> roles) {
        if (roles.contains("ROLE_ADMIN")) {
            return "ADMIN";
        } else if (roles.contains("ROLE_DOCTOR")) {
            return "DOCTOR";
        } else if (roles.contains("ROLE_PATIENT")) {
            return "PATIENT";
        } else {
            return "UNKNOWN";
        }
    }
}