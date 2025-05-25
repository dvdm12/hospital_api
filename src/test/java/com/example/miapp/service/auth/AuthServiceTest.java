package com.example.miapp.service.auth;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import com.example.miapp.repository.RoleRepository;
import com.example.miapp.repository.UserRepository;
import com.example.miapp.security.JwtTokenProvider;
import com.example.miapp.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testRole = new Role();
        testRole.setId(1);
        testRole.setName(Role.ERole.ROLE_PATIENT);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setFirstLogin(true);
        
        Set<Role> roles = new HashSet<>();
        roles.add(testRole);
        testUser.setRoles(roles);
        
        userDetails = new UserDetailsImpl(
            1L, 
            "testuser",
            "test@example.com",
            "encodedPassword",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")),
            true
        );
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void authenticate_ReturnsAuthentication() {
        // Preparar
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        // Ejecutar
        Authentication result = authService.authenticate("testuser", "password");

        // Verificar
        assertNotNull(result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void generateJwtToken_ReturnsToken() {
        // Preparar
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.string");

        // Ejecutar
        String token = authService.generateJwtToken(authentication);

        // Verificar
        assertEquals("jwt.token.string", token);
        verify(tokenProvider).generateToken(authentication);
    }

    @Test
    void updateLastLogin_UpdatesUserLoginInfo() {
        // Preparar
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Ejecutar
        authService.updateLastLogin("testuser");

        // Verificar
        assertFalse(testUser.isFirstLogin());
        assertNotNull(testUser.getLastLogin());
        verify(userRepository).save(testUser);
    }

    @Test
    void registerUser_Success_ReturnsUser() {
        // Preparar
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.ERole.ROLE_PATIENT)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar
        User result = authService.registerUser("newuser", "new@example.com", "password", null);

        // Verificar
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());
        assertTrue(result.isFirstLogin());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(testRole));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameExists_ThrowsException() {
        // Preparar
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Ejecutar y verificar
        assertThrows(RuntimeException.class, () -> {
            authService.registerUser("existinguser", "new@example.com", "password", null);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        // Preparar
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Ejecutar y verificar
        assertThrows(RuntimeException.class, () -> {
            authService.registerUser("newuser", "existing@example.com", "password", null);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserInfoFromAuth_ReturnsUserInfo() {
        // Ejecutar
        Map<String, Object> userInfo = authService.getUserInfoFromAuth(authentication);

        // Verificar
        assertNotNull(userInfo);
        assertEquals(1L, userInfo.get("id"));
        assertEquals("testuser", userInfo.get("username"));
        assertEquals("test@example.com", userInfo.get("email"));
        assertTrue(userInfo.get("roles") instanceof List);
        List<String> roles = (List<String>) userInfo.get("roles");
        assertEquals(1, roles.size());
        assertEquals("ROLE_PATIENT", roles.get(0));
    }
}