// Crea esta nueva entidad y su repositorio
// src/main/java/com/tuempresa/cotizador/model/Subscription.java
package com.tuempresa.cotizador.model;

import com.tuempresa.cotizador.model.enums.SubscriptionStatus;
import com.tuempresa.cotizador.security.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Subscription {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    private LocalDate startDate;
    private LocalDate trialEndDate;
    private LocalDate currentPeriodEndDate;
    private String planType; // "MENSUAL", "ANUAL", etc.
}