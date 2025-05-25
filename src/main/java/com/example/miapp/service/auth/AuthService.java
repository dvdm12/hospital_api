package com.example.miapp.service.auth;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import com.example.miapp.repository.RoleRepository;
import com.example.miapp.repository.UserRepository;
import com.example.miapp.security.JwtTokenProvider;
import com.example.miapp.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
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
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
    }

    public String generateJwtToken(Authentication authentication) {
        return tokenProvider.generateToken(authentication);
    }

    @Transactional
    public void updateLastLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogin(System.currentTimeMillis());
            if (user.isFirstLogin()) {
                user.setFirstLogin(false);
            }
            userRepository.save(user);
        });
    }

    @Transactional
    public User registerUser(String username, String email, String password, Set<String> strRoles) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está en uso");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .status(User.UserStatus.ACTIVE)
                .firstLogin(true)
                .build();

        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role patientRole = roleRepository.findByName(Role.ERole.ROLE_PATIENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found."));
            roles.add(patientRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(adminRole);
                        break;
                    case "doctor":
                        Role doctorRole = roleRepository.findByName(Role.ERole.ROLE_DOCTOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(doctorRole);
                        break;
                    default:
                        Role patientRole = roleRepository.findByName(Role.ERole.ROLE_PATIENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(patientRole);
                }
            });
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

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
}