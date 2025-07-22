package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.model.Producto;
import com.tuempresa.cotizador.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoRepository productoRepository;

    // 1. Mostrar la lista de productos
    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "productos/lista-productos";
    }

    // 2. Mostrar formulario para crear un nuevo producto
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("pageTitle", "Crear Nuevo Producto");
        return "productos/form-producto";
    }

    // 3. Guardar un nuevo producto
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, RedirectAttributes redirectAttributes) {
        productoRepository.save(producto);
        redirectAttributes.addFlashAttribute("successMessage", "Producto guardado correctamente.");
        return "redirect:/productos";
    }

    // 4. Mostrar formulario para editar un producto existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productoRepository.findById(id)
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

    // 5. Eliminar un producto
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede eliminar el producto porque está siendo usado en una o más cotizaciones.");
        }
        return "redirect:/productos";
    }
}