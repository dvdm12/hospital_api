package com.example.miapp.config;

import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import com.example.miapp.repository.RoleRepository;
import com.example.miapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Crear roles si no existen
            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setName(Role.ERole.ROLE_ADMIN);
                roleRepository.save(adminRole);

                Role doctorRole = new Role();
                doctorRole.setName(Role.ERole.ROLE_DOCTOR);
                roleRepository.save(doctorRole);

                Role patientRole = new Role();
                patientRole.setName(Role.ERole.ROLE_PATIENT);
                roleRepository.save(patientRole);
            }

            // Crear usuario administrador por defecto si no existe
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@hospital.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setStatus(User.UserStatus.ACTIVE);
                
                Set<Role> roles = new HashSet<>();
                roles.add(roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Role not found")));
                adminUser.setRoles(roles);
                
                userRepository.save(adminUser);
            }

            // Crear usuario médico de ejemplo
            if (!userRepository.existsByUsername("doctor1")) {
                User doctorUser = new User();
                doctorUser.setUsername("doctor1");
                doctorUser.setEmail("doctor1@hospital.com");
                doctorUser.setPassword(passwordEncoder.encode("doctor123"));
                doctorUser.setStatus(User.UserStatus.ACTIVE);
                
                Set<Role> roles = new HashSet<>();
                roles.add(roleRepository.findByName(Role.ERole.ROLE_DOCTOR)
                        .orElseThrow(() -> new RuntimeException("Role not found")));
                doctorUser.setRoles(roles);
                
                userRepository.save(doctorUser);
            }

            // Crear usuario paciente de ejemplo
            if (!userRepository.existsByUsername("patient1")) {
                User patientUser = new User();
                patientUser.setUsername("patient1");
                patientUser.setEmail("patient1@example.com");
                patientUser.setPassword(passwordEncoder.encode("patient123"));
                patientUser.setStatus(User.UserStatus.ACTIVE);
                
                Set<Role> roles = new HashSet<>();
                roles.add(roleRepository.findByName(Role.ERole.ROLE_PATIENT)
                        .orElseThrow(() -> new RuntimeException("Role not found")));
                patientUser.setRoles(roles);
                
                userRepository.save(patientUser);
            }
        };
    }
}