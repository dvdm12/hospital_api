package com.example.miapp.security;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import com.example.miapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setStatus(User.UserStatus.ACTIVE);
        
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setId(1);
        role.setName(Role.ERole.ROLE_ADMIN);
        roles.add(role);
        testUser.setRoles(roles);
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        // Preparar
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Ejecutar
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Verificar
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Preparar
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Ejecutar y verificar
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistentuser");
        });
        verify(userRepository, times(1)).findByUsername("nonexistentuser");
    }
}