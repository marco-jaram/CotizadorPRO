package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.model.Producto;

import com.tuempresa.cotizador.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    // --- CAMBIO: Inyecta el servicio en lugar del repositorio ---
    private final ProductoService productoService;

    // TODO: Usar el método real una vez implementada la seguridad
    private Long getAuthenticatedUserId(Authentication authentication) {
        return 1L; // Stub temporal
    }

    @GetMapping
    public String listarProductos(Model model, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        model.addAttribute("productos", productoService.findAllByUsuarioId(usuarioId));
        return "productos/lista-productos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("pageTitle", "Crear Nuevo Producto");
        return "productos/form-producto";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        productoService.guardarProducto(producto, usuarioId);
        redirectAttributes.addFlashAttribute("successMessage", "Producto guardado correctamente.");
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        return productoService.findByIdAndUsuarioId(id, usuarioId)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    model.addAttribute("pageTitle", "Editar Producto");
                    return "productos/form-producto";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
                    return "redirect:/productos";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        try {
            productoService.eliminarProducto(id, usuarioId);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede eliminar el producto porque está siendo usado en una o más cotizaciones.");
        }
        return "redirect:/productos";
    }
}