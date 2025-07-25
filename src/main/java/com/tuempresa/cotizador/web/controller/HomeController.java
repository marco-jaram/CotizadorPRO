package com.tuempresa.cotizador.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        // Si el usuario ya está autenticado, lo mandamos a su dashboard principal.
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/cotizaciones";
        }
        // Si no, le mostramos una página de bienvenida o lo mandamos a login.
        return "redirect:/login";
    }
}