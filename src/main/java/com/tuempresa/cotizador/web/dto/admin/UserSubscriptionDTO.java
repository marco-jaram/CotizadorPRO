package com.tuempresa.cotizador.web.dto.admin;

import com.tuempresa.cotizador.model.enums.SubscriptionStatus;
import com.tuempresa.cotizador.security.model.Role;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para transportar información combinada del usuario (User) y su suscripción (Subscription).
 * Diseñado para ser utilizado en el dashboard de administrador.
 */
@Data
public class UserSubscriptionDTO {

    // --- Campos de la entidad User ---
    private Long userId;
    private String nombre;
    private String email;
    private Role role;

    // --- Campos de la entidad Subscription ---
    private SubscriptionStatus subscriptionStatus;
    private LocalDate startDate;
    private LocalDate trialEndDate;
    private LocalDate currentPeriodEndDate;
    private String planType;

    /**
     * Método de utilidad para determinar si la suscripción de este usuario está activa.
     * Una suscripción se considera activa si está en estado ACTIVE o TRIAL.
     * @return true si la suscripción está activa, false en caso contrario.
     */
    public boolean isSubscriptionActive() {
        return subscriptionStatus == SubscriptionStatus.ACTIVE || subscriptionStatus == SubscriptionStatus.TRIAL;
    }
}