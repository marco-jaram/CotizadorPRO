package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.exception.ResourceNotFoundException;
import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    // MÉTODO LISTAR, AHORA CON PAGINACIÓN
    @GetMapping
    public String listarEmpresas(Model model,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size,
                                 @RequestParam(name = "keyword", required = false) String keyword) {
        // Creamos el objeto Pageable para la consulta, ordenando por nombre de empresa
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombreEmpresa").ascending());
        Page<Empresa> empresasPage = empresaService.findClientesByUser(pageable);
        if (keyword != null && !keyword.trim().isEmpty()) {
            empresasPage = empresaService.searchClientesByUser(keyword, pageable);
            model.addAttribute("keyword", keyword); // Pasar el keyword a la vista para mantenerlo en el input
        } else {
            empresasPage = empresaService.findClientesByUser(pageable);
        }
        model.addAttribute("empresasPage", empresasPage);
        return "empresas/lista-empresas";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevaEmpresa(Model model) {
        model.addAttribute("empresa", new Empresa());
        // Añado un título para diferenciar de la edición
        model.addAttribute("pageTitle", "Crear Nuevo Cliente");
        return "empresas/form-empresa";
    }

    // MÉTODO GUARDAR, CON LA LLAMADA CORREGIDA
    @PostMapping
    public String guardarEmpresa(@ModelAttribute Empresa empresa, RedirectAttributes redirectAttributes) {
        // V--- ESTA ES LA LÍNEA CORREGIDA ---V
        empresaService.guardarCliente(empresa);
        redirectAttributes.addFlashAttribute("successMessage", "Cliente guardado correctamente.");
        return "redirect:/empresas";
    }

    // MÉTODO PARA MOSTRAR FORMULARIO DE EDICIÓN (NUEVO Y RECOMENDADO)
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return empresaService.findById(id)
                .map(empresa -> {
                    model.addAttribute("empresa", empresa);
                    model.addAttribute("pageTitle", "Editar Cliente");
                    return "empresas/form-empresa";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado.");
                    return "redirect:/empresas";
                });
    }



    @GetMapping("/mi-empresa")
    public String mostrarFormularioMiEmpresa(Model model) {
        Empresa miEmpresa = empresaService.findMiEmpresaByUser().orElse(new Empresa());
        model.addAttribute("miEmpresa", miEmpresa);
        return "empresas/form-mi-empresa";
    }

    @PostMapping("/mi-empresa")
    public String guardarMiEmpresa(@ModelAttribute("miEmpresa") Empresa empresa,
                                   @RequestParam("logoFile") MultipartFile logoFile,
                                   RedirectAttributes redirectAttributes) throws IOException {
        empresaService.guardarMiEmpresa(empresa, logoFile);
        redirectAttributes.addFlashAttribute("successMessage", "Los datos de 'Mi Empresa' se han guardado correctamente.");
        return "redirect:/empresas/mi-empresa";
    }

    @GetMapping("/mi-empresa/eliminar-logo")
    public String eliminarLogoMiEmpresa(RedirectAttributes redirectAttributes) {
        empresaService.eliminarLogoMiEmpresa();
        redirectAttributes.addFlashAttribute("successMessage", "El logo ha sido eliminado correctamente.");
        return "redirect:/empresas/mi-empresa";
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getEmpresaLogo(@PathVariable Long id) {
        Empresa empresa = empresaService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada o no tienes permiso para verla."));

        if (empresa.getLogo() == null || empresa.getLogo().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(empresa.getLogo());
    }
}