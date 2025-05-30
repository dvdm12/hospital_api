package com.example.miapp.service.auth;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import com.example.miapp.repository.RoleRepository;
import com.example.miapp.repository.UserRepository;
import com.example.miapp.security.JwtTokenProvider;
import com.example.miapp.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    // ========== MÉTODOS DE AUTENTICACIÓN ==========

    /**
     * Autentica un usuario con username y password
     */
    public Authentication authenticate(String username, String password) {
        log.info("Autenticando usuario: {}", username);
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * Genera token JWT para un usuario autenticado
     */
    public String generateJwtToken(Authentication authentication) {
        log.debug("Generando token JWT para usuario: {}", authentication.getName());
        return tokenProvider.generateToken(authentication);
    }

    /**
     * Actualiza la información del último login
     */
    @Transactional
    public void updateLastLogin(String username) {
        log.info("Actualizando último login para usuario: {}", username);
        
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogin(System.currentTimeMillis());
            if (user.isFirstLogin()) {
                user.setFirstLogin(false);
                log.info("Usuario {} completó su primer login", username);
            }
            userRepository.save(user);
        });
    }

    // ========== MÉTODOS DE REGISTRO ==========

    // Modificar el método registerUser en AuthService.java
/**
 * Registra un nuevo usuario en el sistema
 */
@Transactional
public User registerUser(String username, String email, String password, String cc, Set<String> strRoles) {
    log.info("Registrando nuevo usuario: {} con email: {}", username, email);

    // Validaciones de unicidad
    if (userRepository.existsByUsername(username)) {
        log.warn("Intento de registro con username existente: {}", username);
        throw new RuntimeException("El nombre de usuario ya existe");
    }

    if (userRepository.existsByEmail(email)) {
        log.warn("Intento de registro con email existente: {}", email);
        throw new RuntimeException("El email ya está en uso");
    }
    
    // Validar unicidad de cédula si se proporciona
    if (cc != null && !cc.isEmpty()) {
        // Asumiendo que tienes un método para verificar existencia por cc
        if (userRepository.existsByCc(cc)) {
            log.warn("Intento de registro con cédula existente: {}", cc);
            throw new RuntimeException("La cédula ya está registrada en el sistema");
        }
    }

    // Crear usuario
    User user = User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .cc(cc) // Nuevo campo cc
            .status(User.UserStatus.ACTIVE)
            .firstLogin(true)
            .build();

    // Asignar roles
    Set<Role> roles = new HashSet<>();

    if (strRoles == null || strRoles.isEmpty()) {
        Role patientRole = roleRepository.findByName(Role.ERole.ROLE_PATIENT)
                .orElseThrow(() -> new RuntimeException("Error: Role PATIENT not found."));
        roles.add(patientRole);
        log.info("Usuario {} registrado con rol por defecto: PATIENT", username);
    } else {
        strRoles.forEach(role -> {
            switch (role.toLowerCase()) {
                case "admin":
                    Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role ADMIN not found."));
                    roles.add(adminRole);
                    log.info("Rol ADMIN asignado a usuario: {}", username);
                    break;
                case "doctor":
                    Role doctorRole = roleRepository.findByName(Role.ERole.ROLE_DOCTOR)
                            .orElseThrow(() -> new RuntimeException("Error: Role DOCTOR not found."));
                    roles.add(doctorRole);
                    log.info("Rol DOCTOR asignado a usuario: {}", username);
                    break;
                case "patient":
                default:
                    Role patientRole = roleRepository.findByName(Role.ERole.ROLE_PATIENT)
                            .orElseThrow(() -> new RuntimeException("Error: Role PATIENT not found."));
                    roles.add(patientRole);
                    log.info("Rol PATIENT asignado a usuario: {}", username);
                    break;
            }
        });
    }

    user.setRoles(roles);
    User savedUser = userRepository.save(user);
    
    log.info("Usuario {} registrado exitosamente con ID: {}", username, savedUser.getId());
    return savedUser;
}

// Método sobrecargado para compatibilidad con código existente
@Transactional
public User registerUser(String username, String email, String password, Set<String> strRoles) {
    return registerUser(username, email, password, null, strRoles);
}

    // ========== MÉTODOS DE GESTIÓN DE CONTRASEÑAS ==========

    /**
     * Cambia la contraseña del usuario autenticado
     */
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        log.info("Cambiando contraseña para usuario: {}", username);
        
        // Buscar el usuario
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para cambio de contraseña: {}", username);
                    return new RuntimeException("Usuario no encontrado");
                });
        
        // Verificar la contraseña actual
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Intento de cambio de contraseña con contraseña actual incorrecta para usuario: {}", username);
            throw new RuntimeException("La contraseña actual es incorrecta");
        }
        
        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.warn("Intento de cambio a la misma contraseña para usuario: {}", username);
            throw new RuntimeException("La nueva contraseña debe ser diferente a la actual");
        }
        
        // Actualizar con la nueva contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Marcar que ya no es primer login
        if (user.isFirstLogin()) {
            user.setFirstLogin(false);
            log.info("Usuario {} completó su primer cambio de contraseña", username);
        }
        
        userRepository.save(user);
        
        log.info("Contraseña cambiada exitosamente para usuario: {}", username);
    }

    /**
     * Envía email de recuperación de contraseña
     */
    @Transactional
    public void sendPasswordResetEmail(String email) {
        log.info("Procesando solicitud de recuperación de contraseña para email: {}", email);
        
        // Buscar usuario por email
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            // Por seguridad, no revelamos si el email existe o no
            log.warn("Intento de recuperación de contraseña para email no registrado: {}", email);
            return; // No lanzar excepción por seguridad
        }
        
        User user = userOpt.get();
        
        // Verificar que el usuario esté activo
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            log.warn("Intento de recuperación de contraseña para usuario inactivo: {}", user.getUsername());
            return; // No procesar para usuarios inactivos
        }
        
        // Generar token de recuperación
        String resetToken = generatePasswordResetToken();
        
        // En una implementación real:
        // 1. Guardar el token en la base de datos con expiración
        // 2. Enviar email con el enlace de recuperación
        
        // Por ahora, solo logueamos (placeholder para implementación real)
        log.info("Token de recuperación generado para usuario {}: {}", user.getUsername(), resetToken);
        log.info("Se enviaría email de recuperación a: {}", email);
        
        // TODO: Implementar envío real de email
        // emailService.sendPasswordResetEmail(email, resetToken);
        
        // TODO: Guardar token en base de datos si tienes tabla para ello
        // passwordResetTokenRepository.save(new PasswordResetToken(user, resetToken));
    }

    /**
     * Restablece la contraseña usando un token de recuperación
     */
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        log.info("Procesando restablecimiento de contraseña con token");
        
        // TODO: En implementación real, validar token desde base de datos
        // Por ahora, es un placeholder
        
        log.warn("Función de restablecimiento con token no implementada completamente");
        throw new RuntimeException("Función no disponible temporalmente");
    }

    // ========== MÉTODOS DE CONSULTA ==========

    /**
     * Obtiene información del usuario desde el contexto de autenticación
     */
    public Map<String, Object> getUserInfoFromAuth(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userDetails.getId());
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("email", userDetails.getEmail());
        userInfo.put("roles", roles);
        
        return userInfo;
    }

    /**
     * Verifica si un usuario está en su primer login
     */
    public boolean isFirstLogin(String username) {
        return userRepository.findByUsername(username)
                .map(User::isFirstLogin)
                .orElse(false);
    }

    /**
     * Obtiene información básica del usuario por username
     */
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Obtiene información básica del usuario por email
     */
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Verifica si un username ya existe
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Verifica si un email ya existe
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ========== MÉTODOS DE GESTIÓN DE USUARIOS ==========

    /**
     * Activa o desactiva un usuario
     */
    @Transactional
    public void toggleUserStatus(String username, boolean active) {
        log.info("Cambiando estado de usuario {} a: {}", username, active ? "ACTIVO" : "INACTIVO");
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        
        user.setStatus(active ? User.UserStatus.ACTIVE : User.UserStatus.INACTIVE);
        userRepository.save(user);
        
        log.info("Estado del usuario {} actualizado a: {}", username, user.getStatus());
    }

    /**
     * Fuerza el cambio de contraseña en el próximo login
     */
    @Transactional
    public void forcePasswordChangeOnNextLogin(String username) {
        log.info("Forzando cambio de contraseña en próximo login para usuario: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        
        user.setFirstLogin(true);
        userRepository.save(user);
        
        log.info("Usuario {} deberá cambiar contraseña en próximo login", username);
    }

    // ========== MÉTODOS PRIVADOS ==========

    /**
     * Genera un token de recuperación de contraseña
     */
    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}