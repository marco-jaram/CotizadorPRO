package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.service.EmpresaService;
import com.tuempresa.cotizador.service.PdfGenerationService;
import com.tuempresa.cotizador.service.ProductoService;
import com.tuempresa.cotizador.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final EmpresaService empresaService;
    private final ProductoService productoService;
    private final PdfGenerationService pdfGenerationService;

    // TODO: Implementar un método real para obtener el ID del usuario.
    // Este es un stub temporal para que el código compile y la lógica funcione.
    private Long getAuthenticatedUserId(Authentication authentication) {
        // En el futuro, esto obtendrá el ID del usuario real que ha iniciado sesión.
        return 1L;
    }

    private void addCommonAttributesToModel(Model model, Long usuarioId) {
        Empresa miEmpresa = empresaService.findMiEmpresaByUsuarioId(usuarioId)
                .orElse(new Empresa()); // Devolver una empresa vacía si no está configurada
        model.addAttribute("vendedor", miEmpresa);
        model.addAttribute("clientes", empresaService.findClientesByUsuarioId(usuarioId));
        model.addAttribute("estatusDisponibles", EstatusCotizacion.values());
    }

    @GetMapping("/servicios/nueva")
    public String mostrarFormularioServicios(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        if (empresaService.findMiEmpresaByUsuarioId(usuarioId).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "¡Acción requerida! Debe configurar 'Mi Empresa' antes de poder crear una cotización.");
            return "redirect:/empresas/mi-empresa";
        }
        addCommonAttributesToModel(model, usuarioId);
        model.addAttribute("cotizacion", new CotizacionServiciosCreateDTO());
        return "cotizaciones/form-servicios";
    }

    @PostMapping("/servicios")
    public String crearCotizacionServicios(@ModelAttribute CotizacionServiciosCreateDTO dto, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        Empresa miEmpresa = empresaService.findMiEmpresaByUsuarioId(usuarioId).orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());
        CotizacionServiciosDTO nuevaCotizacion = cotizacionService.crearCotizacionServicios(dto, usuarioId);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/productos/nueva")
    public String mostrarFormularioProductos(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        if (empresaService.findMiEmpresaByUsuarioId(usuarioId).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "¡Acción requerida! Debe configurar 'Mi Empresa' antes de poder crear una cotización.");
            return "redirect:/empresas/mi-empresa";
        }
        addCommonAttributesToModel(model, usuarioId);
        model.addAttribute("productos", productoService.findAllByUsuarioId(usuarioId));
        model.addAttribute("cotizacion", new CotizacionProductosCreateDTO());
        return "cotizaciones/form-productos";
    }

    @PostMapping("/productos")
    public String crearCotizacionProductos(@ModelAttribute CotizacionProductosCreateDTO dto, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        Empresa miEmpresa = empresaService.findMiEmpresaByUsuarioId(usuarioId).orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());
        CotizacionProductosDTO nuevaCotizacion = cotizacionService.crearCotizacionProductos(dto, usuarioId);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/{id}")
    public String verDetalleCotizacion(@PathVariable Long id, Model model, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        Object cotizacionDto = cotizacionService.findCotizacionByIdAndUsuarioId(id, usuarioId);
        model.addAttribute("cotizacion", cotizacionDto);
        return "cotizaciones/detalle";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdfCotizacion(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        try {
            Object cotizacionDto = cotizacionService.findCotizacionByIdAndUsuarioId(id, usuarioId);
            String folio = "";
            String tipo = "";
            if (cotizacionDto instanceof CotizacionServiciosDTO) {
                folio = ((CotizacionServiciosDTO) cotizacionDto).getFolio();
                tipo = "Servicios";
            } else if (cotizacionDto instanceof CotizacionProductosDTO) {
                folio = ((CotizacionProductosDTO) cotizacionDto).getFolio();
                tipo = "Productos";
            }

            byte[] pdfBytes = pdfGenerationService.generarPdfCotizacion(id, usuarioId);
            String filename = "Cotizacion-" + tipo + "-" + folio + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public String listarCotizaciones(Model model, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        List<Object> cotizaciones = cotizacionService.findAllCotizacionesByUsuarioId(usuarioId);
        model.addAttribute("cotizaciones", cotizaciones);
        return "cotizaciones/lista-cotizaciones";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        Object cotizacionDto = cotizacionService.findCotizacionByIdAndUsuarioId(id, usuarioId);

        if (cotizacionDto instanceof CotizacionServiciosDTO) {
            CotizacionServiciosDTO dto = (CotizacionServiciosDTO) cotizacionDto;
            addCommonAttributesToModel(model, usuarioId);

            CotizacionServiciosCreateDTO createDto = new CotizacionServiciosCreateDTO();
            // ... (copia de campos del DTO, esto no cambia)
            createDto.setClienteId(dto.getCliente().getId());
            createDto.setVendedorId(dto.getVendedor().getId());
            createDto.setEstatus(dto.getEstatus());
            // ... etc ...

            model.addAttribute("cotizacion", createDto);
            model.addAttribute("cotizacionId", id);
            return "cotizaciones/form-servicios";

        } else if (cotizacionDto instanceof CotizacionProductosDTO) {
            CotizacionProductosDTO dto = (CotizacionProductosDTO) cotizacionDto;
            addCommonAttributesToModel(model, usuarioId);

            CotizacionProductosCreateDTO createDto = new CotizacionProductosCreateDTO();
            // ... (copia de campos del DTO, esto no cambia)
            createDto.setClienteId(dto.getCliente().getId());
            createDto.setVendedorId(dto.getVendedor().getId());
            createDto.setEstatus(dto.getEstatus());
            // ... etc ...

            model.addAttribute("cotizacion", createDto);
            model.addAttribute("cotizacionId", id);
            model.addAttribute("productos", productoService.findAllByUsuarioId(usuarioId));
            return "cotizaciones/form-productos";
        }

        return "redirect:/";
    }

    @PostMapping("/servicios/{id}")
    public String actualizarCotizacionServicios(@PathVariable Long id, @ModelAttribute CotizacionServiciosCreateDTO dto, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        cotizacionService.actualizarCotizacionServicios(id, dto, usuarioId);
        return "redirect:/cotizaciones/" + id;
    }

    @PostMapping("/productos/{id}")
    public String actualizarCotizacionProductos(@PathVariable Long id, @ModelAttribute CotizacionProductosCreateDTO dto, Authentication authentication) {
        Long usuarioId = getAuthenticatedUserId(authentication);
        cotizacionService.actualizarCotizacionProductos(id, dto, usuarioId);
        return "redirect:/cotizaciones/" + id;
    }
}