package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.model.Producto;
import com.tuempresa.cotizador.repository.EmpresaRepository;
import com.tuempresa.cotizador.repository.ProductoRepository;
import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.service.PdfGenerationService;
import com.tuempresa.cotizador.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final EmpresaRepository empresaRepository;
    private final ProductoRepository productoRepository;
    private final PdfGenerationService pdfGenerationService;



    @GetMapping("/servicios/nueva")
    public String mostrarFormularioServicios(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        model.addAttribute("cotizacion", new CotizacionServiciosCreateDTO());
        model.addAttribute("clientes", empresas);
        model.addAttribute("vendedores", empresas); // Asumiendo que clientes y vendedores son del mismo tipo
        return "cotizaciones/form-servicios"; // Nombre de la vista Thymeleaf
    }

    @PostMapping("/servicios")
    public String crearCotizacionServicios(@ModelAttribute CotizacionServiciosCreateDTO dto) {
        CotizacionServiciosDTO nuevaCotizacion = cotizacionService.crearCotizacionServicios(dto);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/productos/nueva")
    public String mostrarFormularioProductos(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("cotizacion", new CotizacionProductosCreateDTO());
        model.addAttribute("clientes", empresas);
        model.addAttribute("vendedores", empresas);
        model.addAttribute("productos", productos);
        return "cotizaciones/form-productos"; // Nombre de la vista Thymeleaf
    }

    @PostMapping("/productos")
    public String crearCotizacionProductos(@ModelAttribute CotizacionProductosCreateDTO dto) {
        CotizacionProductosDTO nuevaCotizacion = cotizacionService.crearCotizacionProductos(dto);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/{id}")
    public String verDetalleCotizacion(@PathVariable Long id, Model model) {
        Object cotizacionDto = cotizacionService.findCotizacionById(id);

        // El DTO puede ser de tipo Servicios o Productos, lo añadimos al modelo.
        // Thymeleaf puede usar th:if para renderizar la parte correcta de la vista
        // basado en el tipo de objeto. ej: th:if="${cotizacion instanceof T(com.tuempresa...CotizacionServiciosDTO)}"
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
            // Es una buena práctica loguear el error para poder depurar
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}