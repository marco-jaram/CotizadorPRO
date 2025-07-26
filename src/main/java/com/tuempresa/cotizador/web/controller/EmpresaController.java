package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.exception.ResourceNotFoundException;
import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.service.EmpresaService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public String listarEmpresas(Model model) {
        model.addAttribute("empresas", empresaService.findClientesByUser());
        return "empresas/lista-empresas";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevaEmpresa(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "empresas/form-empresa";
    }

    @PostMapping
    public String guardarEmpresa(@ModelAttribute Empresa empresa) {
        empresaService.guardarEmpresa(empresa);
        return "redirect:/empresas";
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