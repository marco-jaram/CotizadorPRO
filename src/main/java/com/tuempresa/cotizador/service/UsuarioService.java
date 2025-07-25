package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.security.model.User;
import java.util.Optional;

public interface UsuarioService {
    void registrarUsuario(User user);
    Optional<User> findByEmail(String email);
}