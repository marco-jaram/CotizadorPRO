package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.security.model.User;
import java.util.Optional;

public interface UsuarioService {
    void registrarUsuario(User user);
    Optional<User> findByEmail(String email);

    /**
     * Obtiene el objeto User completamente autenticado de la sesión actual.
     * Lanza una excepción si no hay ningún usuario en sesión.
     * @return El objeto User del usuario actual.
     */
    User getUsuarioActual();
    boolean canUserAccessFeature(User user);
}