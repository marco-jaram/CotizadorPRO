package com.tuempresa.cotizador.security.config;

import com.tuempresa.cotizador.security.service.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Permitir acceso público a estas rutas
                        .requestMatchers("/", "/registro", "/login", "/css/**", "/js/**", "/webjars/**").permitAll()
                        // ¡NUEVA REGLA! Solo ADMIN pueden acceder a /admin/**
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Cualquier otra petición requiere que el usuario esté autenticado
                        .anyRequest().authenticated()
                )
                // Configuración del formulario de login
                .formLogin(form -> form
                        .loginPage("/login") // Le decimos cuál es nuestra página de login personalizada
                        .successHandler(customAuthenticationSuccessHandler)
                        .permitAll() // El acceso a la página de login es público
                )
                // Configuración del logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // A dónde ir después de un logout exitoso
                        .permitAll()
                );

        return http.build();
    }
}