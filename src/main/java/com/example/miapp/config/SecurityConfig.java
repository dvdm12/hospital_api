package com.example.miapp.config;

import com.example.miapp.security.JwtAuthenticationEntryPoint;
import com.example.miapp.security.JwtAuthenticationFilter;
import com.example.miapp.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Collection;

/**
 * Configuración de seguridad completa para sistema médico
 * Maneja tanto API REST (JWT) como aplicación web (Form Login)
 * Versión completamente revisada y corregida
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(
            UserDetailsServiceImpl userDetailsService,
            JwtAuthenticationEntryPoint unauthorizedHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Encoder de contraseñas BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Manager de autenticación
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Proveedor de autenticación DAO
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Handler de éxito de autenticación personalizado
     * Redirige según el rol del usuario
     * Con logging detallado para depuración
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, 
                                              HttpServletResponse response, 
                                              Authentication authentication) throws IOException {
                
                // Registrar información de autenticación
                String username = authentication.getName();
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                log.info("Usuario autenticado: {} con roles: {}", username, authorities);
                
                String redirectUrl = determineTargetUrl(authentication);
                log.info("Redirigiendo a: {}", redirectUrl);
                
                // Realizar la redirección
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            }
            
            private String determineTargetUrl(Authentication authentication) {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                
                // Redirección por rol jerárquico
                if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    return "/admin";
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
                    return "/doctor-portal";
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
                    return "/portal";
                } else {
                    return "/dashboard";
                }
            }
        };
    }

    /**
     * Cadena de filtros de seguridad para API REST (JWT)
     * Orden 1 - Mayor prioridad (más específico)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            // Solo aplica a rutas /api/**
            .securityMatcher("/api/**")
            
            // Deshabilitar CSRF para API REST
            .csrf(AbstractHttpConfigurer::disable)
            
            // Manejo de excepciones JWT
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            
            // Sesiones stateless para API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Autorización para API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()           // Endpoints de autenticación
                .requestMatchers("/api/public/**").permitAll()         // Endpoints públicos
                .requestMatchers("/api/admin/**").hasRole("ADMIN")     // Solo administradores
                .requestMatchers("/api/doctor/**").hasAnyRole("ADMIN", "DOCTOR") // Admins y doctores
                .requestMatchers("/api/patient/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT") // Todos los roles médicos
                .anyRequest().authenticated()                          // Resto requiere autenticación
            )
            
            // Filtro JWT antes del filtro de autenticación estándar
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configurar el proveedor de autenticación
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * Cadena de filtros de seguridad para aplicación web (Form Login)
     * Orden 2 - Menor prioridad (más genérico)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
        http
            // Aplica a todas las rutas restantes (/** menos /api/**)
            .securityMatcher(AntPathRequestMatcher.antMatcher("/**"))
            
            // Autorización para aplicación web
            .authorizeHttpRequests(auth -> auth
                // Páginas públicas
                .requestMatchers(
                    "/", "/home", "/about", "/contact",
                    "/login", "/register", "/forgot-password", "/reset-password",
                    "/auth/register", "/auth/register-type", 
                    "/auth/register/patient", "/auth/check-username", "/auth/check-email",
                    "/auth/forgot-password", "/auth/reset-password"
                ).permitAll()
                
                // Recursos estáticos
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**", "/webjars/**", "/assets/**",
                    "/favicon.ico", "/error/**"
                ).permitAll()
                
                // Rutas específicas por rol
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // IMPORTANTE: Permitir acceso explícito a las rutas de registro de doctor
                .requestMatchers(
                    "/admin/register/doctor", "/admin/register-doctor", 
                    "/admin/register/patient", "/admin/register/admin"
                ).hasRole("ADMIN")
                
                // Rutas para doctores y portal
                .requestMatchers("/doctor-portal/**", "/doctor/**").hasAnyRole("ADMIN", "DOCTOR") 
                .requestMatchers("/portal/**", "/patient/**").hasAnyRole("ADMIN", "PATIENT")
                
                // Rutas compartidas entre roles médicos
                .requestMatchers("/patients/**", "/appointments/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/doctors/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/prescriptions/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/medical-records/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/reports/**").hasAnyRole("ADMIN", "DOCTOR")
                
                // Rutas de cambio de contraseña - accesibles para todos los usuarios autenticados
                .requestMatchers("/change-password", "/auth/change-password").authenticated()
                
                // Dashboard dinámico según rol
                .requestMatchers("/dashboard").authenticated()
                
                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configuración de form login con más detalle de depuración
            .formLogin(form -> form
                .loginPage("/login")                                    // Página de login personalizada
                .loginProcessingUrl("/auth/login-process")              // URL que procesa el login
                .successHandler(customAuthenticationSuccessHandler())   // Redirección personalizada por rol
                .failureUrl("/login?error=true")                        // Redirección en caso de error
                .usernameParameter("username")                          // Nombre del parámetro de usuario
                .passwordParameter("password")                          // Nombre del parámetro de contraseña
                .permitAll()                                            // Permitir acceso a las URLs del form login
            )
            
            // Configuración de logout
            .logout(logout -> logout
                .logoutUrl("/auth/logout")                              // URL para cerrar sesión
                .logoutSuccessUrl("/login?logout=true")                 // Redirección después del logout
                .deleteCookies("JSESSIONID", "jwt_token")               // Eliminar cookies
                .invalidateHttpSession(true)                            // Invalidar sesión
                .clearAuthentication(true)                              // Limpiar autenticación
                .permitAll()                                            // Permitir acceso al logout
            )
            
            // Habilitar protección CSRF para formularios web
            .csrf(Customizer.withDefaults())
            
            // Control de acceso denegado
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")                     // Página de acceso denegado
            )
            
            // Configuración de sesiones para web
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Crear sesión si es necesario
                .invalidSessionUrl("/login?expired=true")               // Redirección si la sesión es inválida
                .maximumSessions(1)                                     // Máximo una sesión por usuario
                .maxSessionsPreventsLogin(false)                        // Permite login múltiple (cierra sesión anterior)
            )
            
            // Configurar el proveedor de autenticación
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * Configuración adicional del proveedor de autenticación para depuración
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false); // Para debugging
        return provider;
    }
}