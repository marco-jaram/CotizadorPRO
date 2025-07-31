package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.model.enums.SubscriptionStatus;
import com.tuempresa.cotizador.web.dto.admin.SubscriptionUpdateDTO;
import com.tuempresa.cotizador.web.dto.admin.UserSubscriptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    /**
     * Obtiene una lista paginada de todos los usuarios con los detalles de su suscripción,
     * aplicando filtros de búsqueda opcionales.
     * @param pageable Configuración de paginación.
     * @param keyword Término de búsqueda para nombre o email.
     * @param role Rol del usuario a filtrar.
     * @param status Estado de la suscripción a filtrar.
     * @return Una página de DTOs con información combinada de usuario y suscripción.
     */
    Page<UserSubscriptionDTO> getAllUsersWithSubscription(Pageable pageable, String keyword, com.tuempresa.cotizador.security.model.Role role, SubscriptionStatus status);

    /**
     * Obtiene los detalles de un usuario y su suscripción por ID.
     * @param userId El ID del usuario a buscar.
     * @return Un DTO con la información detallada.
     */
    UserSubscriptionDTO getUserWithSubscriptionDetails(Long userId);

    /**
     * Actualiza la suscripción de un usuario.
     * @param userId El ID del usuario.
     * @param updateDTO DTO con la información a actualizar.
     */
    void updateSubscription(Long userId, SubscriptionUpdateDTO updateDTO);
}