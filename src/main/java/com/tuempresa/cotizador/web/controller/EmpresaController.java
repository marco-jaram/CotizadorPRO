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
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    // Endpoint para mostrar el logo de una empresa
    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getEmpresaLogo(@PathVariable Long id) {
        Empresa empresa = empresaService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + id));

        if (empresa.getLogo() == null || empresa.getLogo().length == 0) {
            return ResponseEntity.notFound().build();
        }

        // Se asume que los logos son PNG o JPEG. Ajusta si es necesario.
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(empresa.getLogo());
    }

    // (Opcional) Ejemplo de cómo sería el formulario para crear/editar una empresa
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaEmpresa(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "empresas/form-empresa"; // Necesitarías crear esta vista
    }

    @PostMapping
    public String guardarEmpresa(@ModelAttribute Empresa empresa, @RequestParam("logoFile") MultipartFile logoFile) throws IOException {
        empresaService.guardarEmpresa(empresa, logoFile);
        return "redirect:/empresas"; // Redirigir a una lista de empresas, por ejemplo.
    }
    @GetMapping
    public String listarEmpresas(Model model) {
        // Usamos el servicio para obtener todas las empresas
        List<Empresa> listaEmpresas = empresaService.findAll();
        // Las añadimos al modelo para poder usarlas en la vista
        model.addAttribute("empresas", listaEmpresas);
        // Devolvemos el nombre de una nueva vista que vamos a crear
        return "empresas/lista-empresas";
    }
}