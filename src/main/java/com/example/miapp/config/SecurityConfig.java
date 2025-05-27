package com.example.miapp.config;

import com.example.miapp.security.JwtAuthenticationEntryPoint;
import com.example.miapp.security.JwtAuthenticationFilter;
import com.example.miapp.security.UserDetailsServiceImpl;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.util.Collection;

/**
 * Configuración de seguridad completa para sistema médico
 * Maneja tanto API REST (JWT) como aplicación web (Form Login)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, 
                                              HttpServletResponse response, 
                                              Authentication authentication) throws IOException {
                
                String redirectUrl = determineTargetUrl(authentication);
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

    @GetMapping("/forgot-password")
public String showForgotPasswordForm(Model model) {
    // ...
    return "auth/forgot-password"; 
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
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
            .securityMatcher(new AntPathRequestMatcher("/**"))
            
            // Autorización para aplicación web
            .authorizeHttpRequests(auth -> auth
                // Páginas públicas - NO incluir /auth/** (CRÍTICO)
                .requestMatchers("/login", "/register", "/forgot-password", "/auth/register").permitAll()  // Agregado /auth/register
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/error", "/favicon.ico").permitAll()
                
                // Rutas específicas por rol
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/doctor-portal/**", "/doctor/**").hasRole("DOCTOR") 
                .requestMatchers("/portal/**", "/patient/**").hasRole("PATIENT")
                
                // Rutas compartidas entre roles médicos
                .requestMatchers("/patients/**", "/appointments/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/doctors/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/prescriptions/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/medical-records/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/reports/**").hasAnyRole("ADMIN", "DOCTOR")
                
                // Dashboard dinámico según rol
                .requestMatchers("/dashboard").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                
                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configuración de form login
            .formLogin(form -> form
                .loginPage("/login")                                    // Página de login personalizada
                .loginProcessingUrl("/auth/login-process")              // URL que procesa el login (DEBE ser interceptada)
                .successHandler(customAuthenticationSuccessHandler())   // Redirección personalizada por rol
                .failureUrl("/login?error=true")                       // Redirección en caso de error
                .usernameParameter("username")                          // Nombre del parámetro de usuario
                .passwordParameter("password")                          // Nombre del parámetro de contraseña
                .permitAll()                                           // Permitir acceso a las URLs del form login
            )
            
            // Configuración de logout
            .logout(logout -> logout
                .logoutUrl("/auth/logout")                             // URL para cerrar sesión
                .logoutSuccessUrl("/auth/logout")               // Redirección después del logout
                .deleteCookies("JSESSIONID", "jwt_token")             // Eliminar cookies
                .invalidateHttpSession(true)                          // Invalidar sesión
                .clearAuthentication(true)                            // Limpiar autenticación
                .permitAll()                                          // Permitir acceso al logout
            )
            
            // Habilitar protección CSRF para formularios web
            .csrf(Customizer.withDefaults())
            
            // Control de acceso denegado
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")                   // Página de acceso denegado
            )
            
            // Configuración de sesiones para web
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Crear sesión si es necesario
                .maximumSessions(1)                                   // Máximo una sesión por usuario
                .maxSessionsPreventsLogin(false)                      // Permite login múltiple (cierra sesión anterior)
            );

        return http.build();
    }

    /**
     * Configuración adicional del proveedor de autenticación
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