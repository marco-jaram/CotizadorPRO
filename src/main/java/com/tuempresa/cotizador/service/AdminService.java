package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.model.enums.SubscriptionStatus;
import com.tuempresa.cotizador.web.dto.admin.UserSubscriptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    /**
     * Obtiene una lista paginada de todos los usuarios con los detalles de su suscripción.
     * @param pageable Configuración de paginación.
     * @return Una página de DTOs con información combinada de usuario y suscripción.
     */
    Page<UserSubscriptionDTO> getAllUsersWithSubscription(Pageable pageable);

    /**
     * Obtiene los detalles de un usuario y su suscripción por ID.
     * @param userId El ID del usuario a buscar.
     * @return Un DTO con la información detallada.
     */
    UserSubscriptionDTO getUserWithSubscriptionDetails(Long userId);

    /**
     * Actualiza el estado de la suscripción de un usuario.
     * Este método solo debería afectar a usuarios con rol USER.
     * @param userId El ID del usuario cuya suscripción se actualizará.
     * @param newStatus El nuevo estado para la suscripción.
     */
    void updateUserSubscriptionStatus(Long userId, SubscriptionStatus newStatus);
}