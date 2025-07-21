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
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping("/mi-empresa")
    public String mostrarFormularioMiEmpresa(Model model) {
        // Busca si "Mi Empresa" ya existe.
        Empresa miEmpresa = empresaService.findMiEmpresa()
                .orElse(new Empresa()); // Si no existe, crea un objeto nuevo.

        model.addAttribute("miEmpresa", miEmpresa);
        return "empresas/form-mi-empresa"; // Apunta a la nueva vista que crearemos.
    }

    // --- NUEVO ENDPOINT (POST) PARA GUARDAR LOS DATOS ---
    @PostMapping("/mi-empresa")
    public String guardarMiEmpresa(@ModelAttribute("miEmpresa") Empresa empresa,
                                   @RequestParam("logoFile") MultipartFile logoFile,
                                   Model model) throws IOException {

        empresaService.guardarMiEmpresa(empresa, logoFile);

        // Añadimos un mensaje de éxito para mostrar en la vista.
        model.addAttribute("successMessage", "Los datos de 'Mi Empresa' se han guardado correctamente.");

        // Volvemos a cargar los datos por si acaso y redirigimos al mismo formulario.
        return mostrarFormularioMiEmpresa(model);
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getEmpresaLogo(@PathVariable Long id) {
        Empresa empresa = empresaService.findById(id)
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
    // MODIFICADO: Ya no recibe MultipartFile ni lanza IOException
    public String guardarEmpresa(@ModelAttribute Empresa empresa) {
        empresaService.guardarEmpresa(empresa);
        return "redirect:/empresas"; // Redirige a la lista de clientes después de guardar
    }
    @GetMapping
    public String listarEmpresas(Model model) {
        List<Empresa> listaDeClientes = empresaService.findClientes();
        model.addAttribute("empresas", listaDeClientes);
        return "empresas/lista-empresas";
    }
}