package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.model.Producto;
import com.tuempresa.cotizador.service.ProductoService;
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
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public String listarProductos(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(name = "keyword", required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Producto> productosPage = productoService.findAllByUser(pageable);
        if (keyword != null && !keyword.trim().isEmpty()) {
            productosPage = productoService.searchByUser(keyword, pageable);
            model.addAttribute("keyword", keyword); // Pasar el keyword a la vista
        } else {
            productosPage = productoService.findAllByUser(pageable);
        }
        model.addAttribute("productosPage", productosPage);
        return "productos/lista-productos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("pageTitle", "Crear Nuevo Producto");
        return "productos/form-producto";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, RedirectAttributes redirectAttributes) {
        productoService.guardarProducto(producto);
        redirectAttributes.addFlashAttribute("successMessage", "Producto guardado correctamente.");
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productoService.findById(id)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    model.addAttribute("pageTitle", "Editar Producto");
                    return "productos/form-producto";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado o no tienes permiso para editarlo.");
                    return "redirect:/productos";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede eliminar el producto porque está siendo usado en una o más cotizaciones.");
        }
        return "redirect:/productos";
    }
}