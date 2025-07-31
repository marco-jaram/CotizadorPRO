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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserSubscriptionDTO> getAllUsersWithSubscription(Pageable pageable, String keyword, Role role, SubscriptionStatus status) {
        // 1. Filtrar en la BD por keyword y rol, que son campos de la tabla User.
        Page<User> usersPage = userRepository.searchUsers(keyword, role, pageable);

        // 2. Mapear los resultados a DTO.
        List<UserSubscriptionDTO> dtoList = usersPage.getContent().stream()
                .map(this::mapUserToSubscriptionDTO)
                .collect(Collectors.toList());

        // 3. Si se especifica un filtro de estado de suscripción, filtramos la lista de DTOs en memoria.
        List<UserSubscriptionDTO> finalList;
        if (status != null) {
            finalList = dtoList.stream()
                    .filter(dto -> dto.getSubscriptionStatus() == status)
                    .collect(Collectors.toList());
        } else {
            finalList = dtoList;
        }

        // Creamos una nueva página con la lista filtrada (o no) y la información de paginación original
        return new PageImpl<>(finalList, pageable, usersPage.getTotalElements());
    }


    @Override
    @Transactional(readOnly = true)
    public UserSubscriptionDTO getUserWithSubscriptionDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        return mapUserToSubscriptionDTO(user);
    }

    private UserSubscriptionDTO mapUserToSubscriptionDTO(User user) {
        UserSubscriptionDTO dto = new UserSubscriptionDTO();
        dto.setUserId(user.getId());
        dto.setNombre(user.getNombre());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        if (user.getRole() == Role.ADMIN) {
            dto.setSubscriptionStatus(SubscriptionStatus.PERMANENT);
            dto.setPlanType("Administrador");
        } else {
            subscriptionRepository.findByUser(user)
                    .ifPresentOrElse(
                            subscription -> {
                                dto.setSubscriptionStatus(subscription.getStatus());
                                dto.setStartDate(subscription.getStartDate());
                                dto.setTrialEndDate(subscription.getTrialEndDate());
                                dto.setCurrentPeriodEndDate(subscription.getCurrentPeriodEndDate());
                                dto.setPlanType(subscription.getPlanType());
                            },
                            () -> {
                                dto.setSubscriptionStatus(SubscriptionStatus.CANCELED);
                                dto.setPlanType("N/A");
                            }
                    );
        }
        return dto;
    }

    @Override
    @Transactional
    public void updateSubscription(Long userId, SubscriptionUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        Subscription subscription = subscriptionRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada para el usuario con ID: " + userId));

        subscription.setPlanType(updateDTO.getPlanType());
        subscription.setStatus(updateDTO.getNewStatus());

        if (updateDTO.getNewEndDate() != null) {
            subscription.setCurrentPeriodEndDate(updateDTO.getNewEndDate());
        } else if (updateDTO.getNewStatus() == SubscriptionStatus.ACTIVE) {
            LocalDate newEndDate = LocalDate.now();
            if ("Mensual".equalsIgnoreCase(updateDTO.getPlanType())) {
                newEndDate = newEndDate.plusMonths(1);
            } else if ("Anual".equalsIgnoreCase(updateDTO.getPlanType())) {
                newEndDate = newEndDate.plusYears(1);
            }
            subscription.setCurrentPeriodEndDate(newEndDate);
        }

        subscriptionRepository.save(subscription);
    }
}