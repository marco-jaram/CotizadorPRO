package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.model.enums.SubscriptionStatus;
import com.tuempresa.cotizador.service.AdminService;
import com.tuempresa.cotizador.web.dto.admin.SubscriptionUpdateDTO;
import com.tuempresa.cotizador.web.dto.admin.UserSubscriptionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    // Inyectamos el servicio que contendrá la lógica de negocio del administrador.
    // Este servicio es el que debes implementar a continuación.
    private final AdminService adminService;

    /**
     * Muestra el dashboard principal del administrador con una lista paginada de todos los usuarios.
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "15") int size) {

        // Preparamos la paginación, ordenando por el ID del usuario.
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        // Llamamos al servicio para obtener los datos.
        Page<UserSubscriptionDTO> usersPage = adminService.getAllUsersWithSubscription(pageable);

        // Añadimos la página de datos al modelo para que la vista pueda usarla.
        model.addAttribute("usersPage", usersPage);

        // Devolvemos el nombre de la plantilla de Thymeleaf a renderizar.
        // Esta plantilla la tendrás que crear en: resources/templates/admin/dashboard.html
        return "admin/dashboard";
    }

    @GetMapping("/users/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model) {
        UserSubscriptionDTO userDto = adminService.getUserWithSubscriptionDetails(id);
        model.addAttribute("user", userDto);
        return "admin/user-details";
    }

    @PostMapping("/users/{id}/update-subscription")
    public String updateSubscription(@PathVariable Long id,
                                     @ModelAttribute SubscriptionUpdateDTO updateDTO,
                                     RedirectAttributes redirectAttributes) {

        // Ahora llamamos al nuevo método del servicio, que es más completo.
        adminService.updateSubscription(id, updateDTO);

        redirectAttributes.addFlashAttribute("successMessage", "La suscripción del usuario ha sido actualizada correctamente.");
        return "redirect:/admin/dashboard";
    }
}
