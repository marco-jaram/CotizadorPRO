package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.exception.ResourceNotFoundException;
import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.service.EmpresaService;
import com.tuempresa.cotizador.service.impl.EmpresaServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;


@Controller
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    // TODO: Implementar un método real para obtener el ID del usuario.
    // Por ahora, usaremos un valor fijo para que el código compile y funcione.
    private Long getAuthenticatedUserId(Authentication authentication) {
        // En el futuro, esto obtendrá el ID del usuario real que ha iniciado sesión.
        // Ejemplo:
        // CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // return userDetails.getId();
        return 1L; // Usamos 1L como ID de usuario de prueba.
    }

    @GetMapping("/mi-empresa")
    public String mostrarFormularioMiEmpresa(Model model, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        Empresa miEmpresa = empresaService.findMiEmpresaByUsuarioId(usuarioId)
                .orElse(new Empresa());
        model.addAttribute("miEmpresa", miEmpresa);
        return "empresas/form-mi-empresa";
    }

    @PostMapping("/mi-empresa")
    public String guardarMiEmpresa(@ModelAttribute("miEmpresa") Empresa empresa,
                                   @RequestParam("logoFile") MultipartFile logoFile,
                                   RedirectAttributes redirectAttributes, Authentication authentication) throws IOException {
        Long usuarioId = getAuthenticatedUserId(authentication);
        empresaService.guardarMiEmpresa(empresa, logoFile, usuarioId);
        redirectAttributes.addFlashAttribute("successMessage", "Los datos de 'Mi Empresa' se han guardado correctamente.");
        return "redirect:/empresas/mi-empresa";
    }

    @GetMapping("/mi-empresa/eliminar-logo")
    public String eliminarLogoMiEmpresa(RedirectAttributes redirectAttributes, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        empresaService.eliminarLogoMiEmpresa(usuarioId);
        redirectAttributes.addFlashAttribute("successMessage", "El logo ha sido eliminado correctamente.");
        return "redirect:/empresas/mi-empresa";
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getEmpresaLogo(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        Empresa empresa = empresaService.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + id));

        if (empresa.getLogo() == null || empresa.getLogo().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(empresa.getLogo());
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevaEmpresa(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "empresas/form-empresa";
    }

    @PostMapping
    public String guardarEmpresa(@ModelAttribute Empresa empresa, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        empresaService.guardarEmpresa(empresa, usuarioId);
        return "redirect:/empresas";
    }

    @GetMapping
    public String listarEmpresas(Model model, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        List<Empresa> listaDeClientes = empresaService.findClientesByUsuarioId(usuarioId);
        model.addAttribute("empresas", listaDeClientes);
        return "empresas/lista-empresas";
    }
}