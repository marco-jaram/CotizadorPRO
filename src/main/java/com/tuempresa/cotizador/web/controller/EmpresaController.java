package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.exception.ResourceNotFoundException;
import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.service.EmpresaService;
import com.tuempresa.cotizador.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final UsuarioService usuarioService;


    private Long getUsuarioId(Authentication authentication) {
        String userEmail = authentication.getName();
        return usuarioService.findByEmail(userEmail)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado para el email: " + userEmail));
    }

    @GetMapping("/mi-empresa")
    public String mostrarFormularioMiEmpresa(Model model, Authentication authentication) {

        Long usuarioId = getUsuarioId(authentication);
        Empresa miEmpresa = empresaService.findMiEmpresaByUsuarioId(usuarioId)
                .orElse(new Empresa());
        model.addAttribute("miEmpresa", miEmpresa);
        return "empresas/form-mi-empresa";
    }

    @PostMapping("/mi-empresa")
    public String guardarMiEmpresa(@ModelAttribute("miEmpresa") Empresa empresa,
                                   @RequestParam("logoFile") MultipartFile logoFile,
                                   RedirectAttributes redirectAttributes, Authentication authentication) throws IOException {

        Long usuarioId = getUsuarioId(authentication);
        empresaService.guardarMiEmpresa(empresa, logoFile, usuarioId);
        redirectAttributes.addFlashAttribute("successMessage", "Los datos de 'Mi Empresa' se han guardado correctamente.");
        return "redirect:/empresas/mi-empresa";
    }

    @GetMapping("/mi-empresa/eliminar-logo")
    public String eliminarLogoMiEmpresa(RedirectAttributes redirectAttributes, Authentication authentication) {

        Long usuarioId = getUsuarioId(authentication);
        empresaService.eliminarLogoMiEmpresa(usuarioId);
        redirectAttributes.addFlashAttribute("successMessage", "El logo ha sido eliminado correctamente.");
        return "redirect:/empresas/mi-empresa";
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getEmpresaLogo(@PathVariable Long id, Authentication authentication) {

        Long usuarioId = getUsuarioId(authentication);
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

        Long usuarioId = getUsuarioId(authentication);
        empresaService.guardarEmpresa(empresa, usuarioId);
        return "redirect:/empresas";
    }

    @GetMapping
    public String listarEmpresas(Model model, Authentication authentication) {

        Long usuarioId = getUsuarioId(authentication);
        List<Empresa> listaDeClientes = empresaService.findClientesByUsuarioId(usuarioId);
        model.addAttribute("empresas", listaDeClientes);
        return "empresas/lista-empresas";
    }
}