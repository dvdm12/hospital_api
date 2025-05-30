package com.example.miapp.config;

import com.example.miapp.entity.*;
import com.example.miapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                      UserRepository userRepository,
                                      SpecialtyRepository specialtyRepository,
                                      PatientRepository patientRepository,
                                      DoctorRepository doctorRepository,
                                      DoctorSpecialtyRepository doctorSpecialtyRepository,
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
                
                System.out.println("Roles inicializados correctamente");
            }

            // Crear especialidades médicas por defecto si no existen
            if (specialtyRepository.count() == 0) {
                // Lista de especialidades médicas comunes (20 en total)
                String[][] specialtiesData = {
                    // Primeras 10 especialidades originales
                    {"Medicina General", "Atención médica primaria y preventiva"},
                    {"Cardiología", "Diagnóstico y tratamiento de enfermedades del corazón"},
                    {"Pediatría", "Atención médica de niños y adolescentes"},
                    {"Dermatología", "Tratamiento de enfermedades de la piel"},
                    {"Neurología", "Diagnóstico y tratamiento de trastornos del sistema nervioso"},
                    {"Oftalmología", "Cuidado y tratamiento de enfermedades de los ojos"},
                    {"Ginecología", "Salud reproductiva y del sistema reproductivo femenino"},
                    {"Traumatología", "Tratamiento de lesiones y trastornos del sistema musculoesquelético"},
                    {"Psiquiatría", "Diagnóstico y tratamiento de trastornos mentales"},
                    {"Endocrinología", "Tratamiento de trastornos hormonales y metabólicos"},
                    
                    // 10 especialidades adicionales
                    {"Gastroenterología", "Diagnóstico y tratamiento de trastornos del sistema digestivo"},
                    {"Urología", "Tratamiento de trastornos del sistema urinario y reproductivo masculino"},
                    {"Nefrología", "Diagnóstico y tratamiento de enfermedades renales"},
                    {"Neumología", "Especialidad en enfermedades respiratorias y pulmonares"},
                    {"Hematología", "Estudio y tratamiento de trastornos de la sangre"},
                    {"Oncología", "Diagnóstico y tratamiento del cáncer"},
                    {"Reumatología", "Tratamiento de enfermedades reumáticas y autoinmunes"},
                    {"Otorrinolaringología", "Especialidad en oído, nariz y garganta"},
                    {"Anestesiología", "Administración de anestesia y control del dolor"},
                    {"Cirugía Plástica", "Cirugía reconstructiva y estética"}
                };
                
                for (String[] specialtyData : specialtiesData) {
                    Specialty specialty = new Specialty();
                    specialty.setName(specialtyData[0]);
                    specialty.setDescription(specialtyData[1]);
                    specialtyRepository.save(specialty);
                }
                
                System.out.println("Especialidades médicas inicializadas correctamente: " + specialtyRepository.count());
            }

            // USUARIO 1: Crear usuario administrador por defecto
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@hospital.com");
                adminUser.setCc("1193098262");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setStatus(User.UserStatus.ACTIVE);
                
                Set<Role> roles = new HashSet<>();
                roles.add(roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Role not found")));
                adminUser.setRoles(roles);
                
                userRepository.save(adminUser);
                System.out.println("Usuario administrador creado correctamente");
            }

            // USUARIO 2: Crear usuario médico con especialidades
            if (!userRepository.existsByUsername("doctor")) {
                // Crear usuario base
                User doctorUser = new User();
                doctorUser.setUsername("doctor");
                doctorUser.setEmail("doctor@hospital.com");
                doctorUser.setCc("1193098643");
                doctorUser.setPassword(passwordEncoder.encode("doctor123"));
                doctorUser.setStatus(User.UserStatus.ACTIVE);
                
                Set<Role> roles = new HashSet<>();
                roles.add(roleRepository.findByName(Role.ERole.ROLE_DOCTOR)
                        .orElseThrow(() -> new RuntimeException("Role not found")));
                doctorUser.setRoles(roles);
                
                // Guardar usuario para obtener ID
                doctorUser = userRepository.save(doctorUser);
                
                // Crear entidad Doctor asociada
                Doctor doctor = new Doctor();
                doctor.setFirstName("Juan");
                doctor.setLastName("Pérez");
                doctor.setPhone("123456789");
                doctor.setEmail(doctorUser.getEmail());
                doctor.setLicenseNumber("MED12345");
                doctor.setBiography("Médico experimentado con más de 5 años de práctica");
                doctor.setConsultationFee(50.0);
                doctor.setUser(doctorUser);
                
                // Guardar doctor para obtener ID
                doctor = doctorRepository.save(doctor);
                
                // Buscar especialidad de Medicina General
                Specialty generalMedicine = specialtyRepository.findAll().stream()
                        .filter(s -> s.getName().equals("Medicina General"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Especialidad Medicina General no encontrada"));
                
                // Crear relación Doctor-Especialidad primaria
                DoctorSpecialty doctorSpecialty = new DoctorSpecialty();
                doctorSpecialty.setDoctor(doctor);
                doctorSpecialty.setSpecialty(generalMedicine);
                doctorSpecialty.setExperienceLevel("Senior");
                doctorSpecialty.setCertificationDate(new Date()); // Fecha actual
                
                doctorSpecialtyRepository.save(doctorSpecialty);
                
                // Añadir una segunda especialidad (Cardiología)
                Specialty cardiology = specialtyRepository.findAll().stream()
                        .filter(s -> s.getName().equals("Cardiología"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Especialidad Cardiología no encontrada"));
                
                DoctorSpecialty cardiologySpecialty = new DoctorSpecialty();
                cardiologySpecialty.setDoctor(doctor);
                cardiologySpecialty.setSpecialty(cardiology);
                cardiologySpecialty.setExperienceLevel("Junior");
                cardiologySpecialty.setCertificationDate(new Date()); // Fecha actual
                
                doctorSpecialtyRepository.save(cardiologySpecialty);
                
                System.out.println("Doctor creado con especialidades: Medicina General y Cardiología");
            }

            // USUARIO 3: Crear usuario paciente
            if (!userRepository.existsByUsername("patient")) {
                User patientUser = new User();
                patientUser.setUsername("patient");
                patientUser.setEmail("patient@example.com");
                patientUser.setCc("1987564353");
                patientUser.setPassword(passwordEncoder.encode("patient123"));
                patientUser.setStatus(User.UserStatus.ACTIVE);
                
                Set<Role> roles = new HashSet<>();
                roles.add(roleRepository.findByName(Role.ERole.ROLE_PATIENT)
                        .orElseThrow(() -> new RuntimeException("Role not found")));
                patientUser.setRoles(roles);
                
                patientUser = userRepository.save(patientUser);
                
                // Crear entidad Patient asociada
                Patient patient = new Patient();
                patient.setFirstName("Carlos");
                patient.setLastName("Gómez");
                patient.setPhone("567891234");
                patient.setAddress("Calle Principal 123");
                patient.setGender(Patient.Gender.MALE);
                patient.setBloodType("O+");
                patient.setEmergencyContactName("Ana Gómez");
                patient.setEmergencyContactPhone("098765432");
                patient.setUser(patientUser);
                
                // Aquí necesitaríamos un PatientRepository para guardar el paciente
                patientRepository.save(patient);
                
                System.out.println("Usuario paciente creado correctamente");
            }
        };
    }
}