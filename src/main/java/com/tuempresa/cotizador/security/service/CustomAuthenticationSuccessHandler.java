package com.tuempresa.cotizador.security.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    // Usamos RedirectStrategy para tener más control sobre la redirección
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Obtenemos los roles (autoridades) del usuario que acaba de iniciar sesión
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Iteramos sobre sus roles para decidir a dónde redirigir
        authorities.forEach(authority -> {
            try {
                // Si el rol es ROLE_ADMIN (Spring añade "ROLE_" automáticamente)
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    redirectStrategy.sendRedirect(request, response, "/admin/dashboard");
                    return; // Terminamos la ejecución para este rol
                }
                // Si el rol es ROLE_USER
                else if (authority.getAuthority().equals("ROLE_USER")) {
                    redirectStrategy.sendRedirect(request, response, "/cotizaciones");
                    return; // Terminamos la ejecución para este rol
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}