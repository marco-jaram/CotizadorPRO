package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.model.Subscription;
import com.tuempresa.cotizador.model.enums.SubscriptionStatus;
import com.tuempresa.cotizador.repository.SubscriptionRepository;
import com.tuempresa.cotizador.security.model.Role;
import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.security.repository.UserRepository;
import com.tuempresa.cotizador.service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public void registrarUsuario(User user) {
        // 1. Validar que el email no exista
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("El email ya está registrado.");
        }

        // 2. Preparar COMPLETAMENTE el objeto User ANTES de guardarlo
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER); // Asignamos el rol aquí

        // 3. Guardar el usuario UNA SOLA VEZ
        // La instancia 'savedUser' ya tendrá su ID generado por la base de datos.
        User savedUser = userRepository.save(user);

        // 4. Crear y guardar la suscripción asociada
        Subscription subscription = new Subscription();
        subscription.setUser(savedUser); // Usamos el usuario recién guardado con su ID
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setPlanType("Prueba Gratuita");
        subscription.setStartDate(LocalDate.now());
        subscription.setTrialEndDate(LocalDate.now().plusDays(7));

        subscriptionRepository.save(subscription);
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

    @Override
    public boolean canUserAccessFeature(User user) {
        if (user == null) return false;

        // 1. Un admin SIEMPRE tiene acceso.
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // 2. Si no es admin, verificar la suscripción.
        return subscriptionRepository.findByUser(user)
                .map(subscription ->
                        subscription.getStatus() == SubscriptionStatus.ACTIVE ||
                                subscription.getStatus() == SubscriptionStatus.TRIAL
                )
                .orElse(false); // Si no tiene suscripción, no tiene acceso.
    }
}