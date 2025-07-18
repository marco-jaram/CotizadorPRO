package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.model.Producto;
import com.tuempresa.cotizador.repository.EmpresaRepository;
import com.tuempresa.cotizador.repository.ProductoRepository;
import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.service.EmpresaService;
import com.tuempresa.cotizador.service.PdfGenerationService;
import com.tuempresa.cotizador.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final EmpresaService empresaService;
    private final ProductoRepository productoRepository;
    private final PdfGenerationService pdfGenerationService;


    @GetMapping("/servicios/nueva")
    public String mostrarFormularioServicios(Model model, RedirectAttributes redirectAttributes) { // Añadir RedirectAttributes
        // --- CORRECCIÓN: LÓGICA DE SELECCIÓN PARA COTIZACIÓN ---

        // 1. Validar que "Mi Empresa" (el vendedor) exista.
        Optional<Empresa> vendedorOpt = empresaService.findMiEmpresa();
        if (vendedorOpt.isEmpty()) {
            // Si no existe, redirigimos al formulario de configuración con un mensaje.
            redirectAttributes.addFlashAttribute("error", "¡Acción requerida! Debe configurar 'Mi Empresa' antes de poder crear una cotización.");
            return "redirect:/empresas/mi-empresa";
        }
        Empresa vendedor = vendedorOpt.get();


        // 2. Los CLIENTES son todas las demás empresas.
        List<Empresa> listaDeClientes = empresaService.findClientes();

        model.addAttribute("cotizacion", new CotizacionServiciosCreateDTO());
        model.addAttribute("clientes", listaDeClientes);
        model.addAttribute("vendedor", vendedor); // Ahora es un solo objeto, no una lista

        return "cotizaciones/form-servicios";
    }


    @PostMapping("/servicios")
    public String crearCotizacionServicios(@ModelAttribute CotizacionServiciosCreateDTO dto) {
        // Asignar el ID del vendedor automáticamente
        Empresa miEmpresa = empresaService.findMiEmpresa().orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());

        CotizacionServiciosDTO nuevaCotizacion = cotizacionService.crearCotizacionServicios(dto);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/productos/nueva")
    public String mostrarFormularioProductos(Model model, RedirectAttributes redirectAttributes) { // Añadir RedirectAttributes
        // --- CORRECCIÓN: LÓGICA DE SELECCIÓN (IDÉNTICA A LA DE SERVICIOS) ---

        // 1. Validar que "Mi Empresa" (el vendedor) exista.
        Optional<Empresa> vendedorOpt = empresaService.findMiEmpresa();
        if (vendedorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "¡Acción requerida! Debe configurar 'Mi Empresa' antes de poder crear una cotización.");
            return "redirect:/empresas/mi-empresa";
        }
        Empresa vendedor = vendedorOpt.get();


        // 2. Los CLIENTES son todas las demás empresas.
        List<Empresa> listaDeClientes = empresaService.findClientes();

        // 3. Obtenemos todos los productos disponibles.
        List<Producto> productos = productoRepository.findAll();

        model.addAttribute("cotizacion", new CotizacionProductosCreateDTO());
        model.addAttribute("clientes", listaDeClientes);
        model.addAttribute("vendedor", vendedor); // Ahora es un solo objeto, no una lista
        model.addAttribute("productos", productos);

        return "cotizaciones/form-productos";
    }

    @PostMapping("/productos")
    public String crearCotizacionProductos(@ModelAttribute CotizacionProductosCreateDTO dto) {
        // Asignar el ID del vendedor automáticamente
        Empresa miEmpresa = empresaService.findMiEmpresa().orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());

        CotizacionProductosDTO nuevaCotizacion = cotizacionService.crearCotizacionProductos(dto);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/{id}")
    public String verDetalleCotizacion(@PathVariable Long id, Model model) {
        Object cotizacionDto = cotizacionService.findCotizacionById(id);

        model.addAttribute("cotizacion", cotizacionDto);

        return "cotizaciones/detalle"; // Vista genérica de detalle
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdfCotizacion(@PathVariable Long id) {
        try {
            byte[] pdfBytes = pdfGenerationService.generarPdfCotizacion(id);
            String filename = "cotizacion-" + id + ".pdf";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public String listarCotizaciones(Model model) {
        List<Object> cotizaciones = cotizacionService.findAllCotizaciones();
        model.addAttribute("cotizaciones", cotizaciones);
        return "cotizaciones/lista-cotizaciones";
    }
}