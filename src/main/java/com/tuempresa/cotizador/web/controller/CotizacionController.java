package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.exception.ResourceNotFoundException;
import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.service.*;
import com.tuempresa.cotizador.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final EmpresaService empresaService;
    private final ProductoService productoService;
    private final PdfGenerationService pdfGenerationService;
    private final ExcelGenerationService excelGenerationService;
    private final UsuarioService usuarioService;

    private void addCommonAttributesToModel(Model model) {
        Empresa miEmpresa = empresaService.findMiEmpresaByUser().orElse(new Empresa());
        model.addAttribute("vendedor", miEmpresa);
        model.addAttribute("clientes", empresaService.findAllClientesByUser());
        model.addAttribute("productos", productoService.findAllByUser()); // Para el dropdown de productos
        model.addAttribute("estatusDisponibles", EstatusCotizacion.values());
    }

    @GetMapping
    public String listarCotizaciones(Model model,
                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaEmision").descending());
        Page<Object> cotizacionesPage = cotizacionService.findAllByUser(pageable);
        model.addAttribute("cotizacionesPage", cotizacionesPage);
        model.addAttribute("cotizaciones", cotizacionesPage.getContent()); // Para la exportación a excel
        return "cotizaciones/lista-cotizaciones";
    }

    @GetMapping("/servicios/nueva")
    public String mostrarFormularioServicios(Model model, RedirectAttributes redirectAttributes) {

        // --- Inicio de la Verificación de Acceso ---
        try {
            User usuarioActual = usuarioService.getUsuarioActual();
            if (!usuarioService.canUserAccessFeature(usuarioActual)) {
                redirectAttributes.addFlashAttribute("error", "Tu plan no te permite crear cotizaciones. Actualiza tu suscripción.");
                return "redirect:/suscripcion"; // Próximamente, crearás esta página
            }
        } catch (IllegalStateException e) {
            // Falla si no hay usuario en sesión, Spring Security debería manejarlo, pero es una salvaguarda.
            return "redirect:/login";
        }
        // --- Fin de la Verificación de Acceso ---

        if (empresaService.findMiEmpresaByUser().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "¡Acción requerida! Debe configurar 'Mi Empresa' antes de poder crear una cotización.");
            return "redirect:/empresas/mi-empresa";
        }

        addCommonAttributesToModel(model);
        model.addAttribute("cotizacion", new CotizacionServiciosCreateDTO());
        return "cotizaciones/form-servicios";
    }

    @PostMapping("/servicios")
    public String crearCotizacionServicios(@ModelAttribute CotizacionServiciosCreateDTO dto) {
        Empresa miEmpresa = empresaService.findMiEmpresaByUser().orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());
        CotizacionServiciosDTO nuevaCotizacion = cotizacionService.crearCotizacionServicios(dto);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/productos/nueva")
    public String mostrarFormularioProductos(Model model, RedirectAttributes redirectAttributes) {
        if (empresaService.findMiEmpresaByUser().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "¡Acción requerida! Debe configurar 'Mi Empresa' antes de poder crear una cotización.");
            return "redirect:/empresas/mi-empresa";
        }
        addCommonAttributesToModel(model);
        model.addAttribute("cotizacion", new CotizacionProductosCreateDTO());
        return "cotizaciones/form-productos";
    }

    @PostMapping("/productos")
    public String crearCotizacionProductos(@ModelAttribute CotizacionProductosCreateDTO dto) {
        Empresa miEmpresa = empresaService.findMiEmpresaByUser().orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());
        CotizacionProductosDTO nuevaCotizacion = cotizacionService.crearCotizacionProductos(dto);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/{id}")
    public String verDetalleCotizacion(@PathVariable Long id, Model model) {
        try {
            Object cotizacionDto = cotizacionService.findById(id);
            model.addAttribute("cotizacion", cotizacionDto);
            return "cotizaciones/detalle";
        } catch (ResourceNotFoundException e) {
            return "redirect:/cotizaciones";
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdfCotizacion(@PathVariable Long id) {
        try {
            byte[] pdfBytes = pdfGenerationService.generarPdfCotizacion(id);
            Object cotizacionDto = cotizacionService.findById(id);
            String folio = "", tipo = "";
            if (cotizacionDto instanceof CotizacionServiciosDTO c) { folio = c.getFolio(); tipo = "Servicios"; }
            else if (cotizacionDto instanceof CotizacionProductosDTO c) { folio = c.getFolio(); tipo = "Productos"; }
            String filename = "Cotizacion-" + tipo + "-" + folio + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", filename);
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        try {
            Object cotizacionDto = cotizacionService.findById(id);
            addCommonAttributesToModel(model);

            if (cotizacionDto instanceof CotizacionServiciosDTO dto) {
                CotizacionServiciosCreateDTO createDto = new CotizacionServiciosCreateDTO();
                createDto.setClienteId(dto.getCliente().getId());
                createDto.setVendedorId(dto.getVendedor().getId());
                createDto.setEstatus(dto.getEstatus());
                createDto.setVigencia(dto.getVigencia());
                createDto.setDescripcionGeneral(dto.getDescripcionGeneral());
                createDto.setFormaPago(dto.getFormaPago());
                createDto.setMetodosAceptados(dto.getMetodosAceptados());
                createDto.setCondicionesEntrega(dto.getCondicionesEntrega());
                createDto.setTiempoRespuesta(dto.getTiempoRespuesta());
                createDto.setAplicarIva(dto.isAplicarIva());
                createDto.setPorcentajeIva(dto.getPorcentajeIva());
                if (dto.getLineas() != null) {
                    createDto.setLineas(dto.getLineas().stream().map(lineaDto -> {
                        LineaCotizacionServicioCreateDTO lineaCreate = new LineaCotizacionServicioCreateDTO();
                        lineaCreate.setConcepto(lineaDto.getConcepto());
                        lineaCreate.setCantidad(lineaDto.getCantidad());
                        lineaCreate.setUnidad(lineaDto.getUnidad());
                        lineaCreate.setPrecioUnitario(lineaDto.getPrecioUnitario());
                        return lineaCreate;
                    }).collect(Collectors.toList()));
                }
                model.addAttribute("cotizacion", createDto);
                model.addAttribute("cotizacionId", id);
                return "cotizaciones/form-servicios";

            } else if (cotizacionDto instanceof CotizacionProductosDTO dto) {
                CotizacionProductosCreateDTO createDto = new CotizacionProductosCreateDTO();
                createDto.setClienteId(dto.getCliente().getId());
                createDto.setVendedorId(dto.getVendedor().getId());
                createDto.setEstatus(dto.getEstatus());
                createDto.setVigencia(dto.getVigencia());
                createDto.setCondicionesEntrega(dto.getCondicionesEntrega());
                createDto.setGarantia(dto.getGarantia());
                createDto.setPoliticaDevoluciones(dto.getPoliticaDevoluciones());
                createDto.setFormasPago(dto.getFormasPago());
                createDto.setAplicarIva(dto.isAplicarIva());
                createDto.setPorcentajeIva(dto.getPorcentajeIva());
                if (dto.getLineas() != null) {
                    createDto.setLineas(dto.getLineas().stream().map(lineaDto -> {
                        LineaCotizacionProductoCreateDTO lineaCreate = new LineaCotizacionProductoCreateDTO();
                        lineaCreate.setProductoId(lineaDto.getProductoId());
                        lineaCreate.setCantidad(lineaDto.getCantidad());
                        lineaCreate.setUnidad(lineaDto.getUnidad());
                        lineaCreate.setPrecioUnitario(lineaDto.getPrecioUnitario());
                        return lineaCreate;
                    }).collect(Collectors.toList()));
                }
                model.addAttribute("cotizacion", createDto);
                model.addAttribute("cotizacionId", id);
                return "cotizaciones/form-productos";
            }
            return "redirect:/cotizaciones";
        } catch (ResourceNotFoundException e) {
            return "redirect:/cotizaciones";
        }
    }

    @PostMapping("/servicios/{id}")
    public String actualizarCotizacionServicios(@PathVariable Long id, @ModelAttribute CotizacionServiciosCreateDTO dto) {
        cotizacionService.actualizarCotizacionServicios(id, dto);
        return "redirect:/cotizaciones/" + id;
    }

    @PostMapping("/productos/{id}")
    public String actualizarCotizacionProductos(@PathVariable Long id, @ModelAttribute CotizacionProductosCreateDTO dto) {
        cotizacionService.actualizarCotizacionProductos(id, dto);
        return "redirect:/cotizaciones/" + id;
    }

    @GetMapping("/exportar/excel")
    public ResponseEntity<InputStreamResource> exportarCotizacionesAExcel() {
        List<Object> cotizaciones = cotizacionService.findAllByUserForExport();
        ByteArrayInputStream in = excelGenerationService.generarExcelCotizaciones(cotizaciones);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=cotizaciones.xlsx");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}