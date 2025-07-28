package com.tuempresa.cotizador.model.enums;

public enum SubscriptionStatus {
    TRIAL,      // Periodo de prueba
    ACTIVE,     // Suscripción pagada y activa
    PAST_DUE,   // Pago vencido
    CANCELED,   // Cancelada por el usuario
    EXPIRED     // Expiró y no se renovó
}
