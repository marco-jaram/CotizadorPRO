package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.security.repository.UserRepository;
import com.tuempresa.cotizador.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void registrarUsuario(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("El email ya está registrado.");
        }
        // Hasheamos la contraseña antes de guardarla
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    @Override
    public User getUsuarioActual() {
        // Obtenemos la información de autenticación de la sesión actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verificamos que el usuario esté realmente autenticado
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No hay un usuario autenticado en la sesión.");
        }

        // El "nombre" del usuario en Spring Security es, por defecto, su email/username
        String email = authentication.getName();

        // Buscamos el usuario completo en nuestra base de datos a partir de su email
        return findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario con email " + email + " no encontrado en la base de datos."));
    }
}