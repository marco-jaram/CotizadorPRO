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


    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "15") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) com.tuempresa.cotizador.security.model.Role role,
                                @RequestParam(required = false) SubscriptionStatus status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<UserSubscriptionDTO> usersPage = adminService.getAllUsersWithSubscription(pageable, keyword, role, status);

        // Añadimos los filtros al modelo para mantener sus valores en el formulario
        model.addAttribute("usersPage", usersPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);

        // Pasamos los enums a la vista para rellenar los dropdowns
        model.addAttribute("roles", com.tuempresa.cotizador.security.model.Role.values());
        model.addAttribute("statuses", SubscriptionStatus.values());

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
