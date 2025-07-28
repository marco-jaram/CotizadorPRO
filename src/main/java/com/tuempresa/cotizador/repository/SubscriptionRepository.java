package com.tuempresa.cotizador.repository;

import com.tuempresa.cotizador.model.Subscription;
import com.tuempresa.cotizador.model.enums.SubscriptionStatus;
import com.tuempresa.cotizador.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Busca la suscripción asociada a un usuario específico.
     * Debido a la relación uno-a-uno, este es el método más común para
     * obtener los detalles de la suscripción de un usuario.
     *
     * @param user El usuario cuya suscripción se quiere encontrar.
     * @return un Optional que contiene la suscripción si existe.
     */
    Optional<Subscription> findByUser(User user);

    /**
     * Encuentra todas las suscripciones que coinciden con un estado particular.
     * Muy útil para el panel de administrador para filtrar usuarios (ej. "Ver todos los activos",
     * "Ver todos los que están en prueba").
     *
     * @param status El estado de la suscripción a buscar.
     * @return Una lista de suscripciones que coinciden con el estado.
     */
    List<Subscription> findAllByStatus(SubscriptionStatus status);

    /**
     * Encuentra todas las suscripciones en un estado específico (ej. TRIAL)
     * cuya fecha de fin de prueba es anterior o igual a la fecha proporcionada.
     * Ideal para un proceso automático (batch) que desactive las pruebas expiradas.
     *
     * @param status El estado de la suscripción (normalmente SubscriptionStatus.TRIAL).
     * @param date La fecha límite.
     * @return Una lista de suscripciones de prueba que han expirado.
     */
    List<Subscription> findAllByStatusAndTrialEndDateBefore(SubscriptionStatus status, LocalDate date);

    /**
     * Encuentra todas las suscripciones en un estado específico (ej. ACTIVE)
     * cuyo período de pago actual finaliza en o antes de la fecha dada.
     * Perfecto para un proceso que marque las cuentas como "pago vencido" (PAST_DUE) o envíe recordatorios.
     *
     * @param status El estado de la suscripción (normalmente SubscriptionStatus.ACTIVE).
     * @param date La fecha límite del período actual.
     * @return Una lista de suscripciones activas cuyo período está por vencer.
     */
    List<Subscription> findAllByStatusAndCurrentPeriodEndDateBefore(SubscriptionStatus status, LocalDate date);

}