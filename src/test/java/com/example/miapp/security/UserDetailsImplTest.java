package com.example.miapp.security;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    void testBuild() {
        // Preparar
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setStatus(User.UserStatus.ACTIVE);
        
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setId(1);
        role.setName(Role.ERole.ROLE_ADMIN);
        roles.add(role);
        user.setRoles(roles);

        // Ejecutar
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // Verificar
        assertEquals(1L, userDetails.getId());
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("test@example.com", userDetails.getEmail());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        
        // Verificar roles
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }

    @Test
    void testEquals() {
        // Preparar
        UserDetailsImpl user1 = new UserDetailsImpl(1L, "user1", "user1@example.com", 
                "password", Set.of(new SimpleGrantedAuthority("ROLE_USER")), true);
        UserDetailsImpl user2 = new UserDetailsImpl(1L, "user1", "user1@example.com", 
                "password", Set.of(new SimpleGrantedAuthority("ROLE_USER")), true);
        UserDetailsImpl user3 = new UserDetailsImpl(2L, "user2", "user2@example.com", 
                "password", Set.of(new SimpleGrantedAuthority("ROLE_USER")), true);

        // Verificar
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertNotEquals(user1, null);
        assertNotEquals(user1, new Object());
        assertEquals(user1, user1);
    }
}