package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.exception.ResourceNotFoundException;
import com.tuempresa.cotizador.model.Subscription;
import com.tuempresa.cotizador.model.enums.SubscriptionStatus;
import com.tuempresa.cotizador.repository.SubscriptionRepository;
import com.tuempresa.cotizador.security.model.Role;
import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.security.repository.UserRepository;
import com.tuempresa.cotizador.service.AdminService;
import com.tuempresa.cotizador.web.dto.admin.SubscriptionUpdateDTO;
import com.tuempresa.cotizador.web.dto.admin.UserSubscriptionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserSubscriptionDTO> getAllUsersWithSubscription(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(this::mapUserToSubscriptionDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSubscriptionDTO getUserWithSubscriptionDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        return mapUserToSubscriptionDTO(user);
    }



    /**
     * Método privado de utilidad para convertir un objeto User a UserSubscriptionDTO.
     * Es consciente de los roles y trata a los ADMIN como un caso especial.
     */
    private UserSubscriptionDTO mapUserToSubscriptionDTO(User user) {
        UserSubscriptionDTO dto = new UserSubscriptionDTO();

        // 1. Mapear datos del usuario (común para todos los roles)
        dto.setUserId(user.getId());
        dto.setNombre(user.getNombre());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        // 2. Lógica de mapeo de suscripción basada en el rol
        if (user.getRole() == Role.ADMIN) {
            // Si el usuario es ADMIN, se le asigna un estado especial y permanente.
            // No se consulta la tabla de suscripción para él.
            dto.setSubscriptionStatus(SubscriptionStatus.PERMANENT);  // Para visualización, siempre está activo.
            dto.setPlanType("Administrador");
            // Las fechas de expiración se dejan en null, ya que no aplican.

        } else {
            // Si es un usuario normal (USER), se busca su suscripción en la base de datos.
            subscriptionRepository.findByUser(user)
                    .ifPresentOrElse(
                            // Si la suscripción existe, se mapean sus datos
                            subscription -> {
                                dto.setSubscriptionStatus(subscription.getStatus());
                                dto.setStartDate(subscription.getStartDate());
                                dto.setTrialEndDate(subscription.getTrialEndDate());
                                dto.setCurrentPeriodEndDate(subscription.getCurrentPeriodEndDate());
                                dto.setPlanType(subscription.getPlanType());
                            },
                            // Si por alguna razón un USER no tiene suscripción, se establece un estado por defecto.
                            () -> {
                                dto.setSubscriptionStatus(SubscriptionStatus.CANCELED); // O el estado que consideres apropiado
                                dto.setPlanType("N/A");
                            }
                    );
        }

        return dto;
    }

    @Override
    @Transactional
    public void updateSubscription(Long userId, SubscriptionUpdateDTO updateDTO) {
        // 1. Buscar el usuario en la base de datos.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        // 2. Regla de negocio: No se puede modificar la suscripción de un admin.
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        // 3. Buscar la suscripción asociada a ese usuario.
        Subscription subscription = subscriptionRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada para el usuario con ID: " + userId));

        // 4. Actualizar los campos con los datos que vienen del formulario (DTO).
        subscription.setPlanType(updateDTO.getPlanType());
        subscription.setStatus(updateDTO.getNewStatus());

        // 5. Lógica inteligente para la fecha de expiración.
        if (updateDTO.getNewEndDate() != null) {
            // Si el admin especificó una fecha manualmente en el formulario, se usa esa.
            subscription.setCurrentPeriodEndDate(updateDTO.getNewEndDate());
        } else if (updateDTO.getNewStatus() == SubscriptionStatus.ACTIVE) {
            // Si el admin cambió el estado a ACTIVE pero no puso fecha, la calculamos automáticamente.
            LocalDate newEndDate = LocalDate.now();
            if ("Mensual".equalsIgnoreCase(updateDTO.getPlanType())) {
                newEndDate = newEndDate.plusMonths(1);
            } else if ("Anual".equalsIgnoreCase(updateDTO.getPlanType())) {
                newEndDate = newEndDate.plusYears(1);
            }
            subscription.setCurrentPeriodEndDate(newEndDate);
        }

        // 6. Guardar la suscripción actualizada en la base de datos.
        subscriptionRepository.save(subscription);
    }
}