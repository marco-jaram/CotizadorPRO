package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.model.Producto;
import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import com.tuempresa.cotizador.repository.ProductoRepository;
import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.service.EmpresaService;
import com.tuempresa.cotizador.service.PdfGenerationService;
import com.tuempresa.cotizador.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final EmpresaService empresaService;
    private final ProductoRepository productoRepository;
    private final PdfGenerationService pdfGenerationService;

    @GetMapping("/servicios/nueva")
    public String mostrarFormularioServicios(Model model, RedirectAttributes redirectAttributes) {
        Optional<Empresa> vendedorOpt = empresaService.findMiEmpresa();
        if (vendedorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "¡Acción requerida! Debe configurar 'Mi Empresa' antes de poder crear una cotización.");
            return "redirect:/empresas/mi-empresa";
        }
        Empresa vendedor = vendedorOpt.get();
        List<Empresa> listaDeClientes = empresaService.findClientes();

        model.addAttribute("cotizacion", new CotizacionServiciosCreateDTO());
        model.addAttribute("clientes", listaDeClientes);
        model.addAttribute("vendedor", vendedor);
        // --- AÑADIDO ---
        model.addAttribute("estatusDisponibles", EstatusCotizacion.values()); // Pasa la lista de estatus a la vista

        return "cotizaciones/form-servicios";
    }

    @PostMapping("/servicios")
    public String crearCotizacionServicios(@ModelAttribute CotizacionServiciosCreateDTO dto) {
        Empresa miEmpresa = empresaService.findMiEmpresa().orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());
        CotizacionServiciosDTO nuevaCotizacion = cotizacionService.crearCotizacionServicios(dto);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/productos/nueva")
    public String mostrarFormularioProductos(Model model, RedirectAttributes redirectAttributes) {
        Optional<Empresa> vendedorOpt = empresaService.findMiEmpresa();
        if (vendedorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "¡Acción requerida! Debe configurar 'Mi Empresa' antes de poder crear una cotización.");
            return "redirect:/empresas/mi-empresa";
        }
        Empresa vendedor = vendedorOpt.get();
        List<Empresa> listaDeClientes = empresaService.findClientes();
        List<Producto> productos = productoRepository.findAll();

        model.addAttribute("cotizacion", new CotizacionProductosCreateDTO());
        model.addAttribute("clientes", listaDeClientes);
        model.addAttribute("vendedor", vendedor);
        model.addAttribute("productos", productos);
        // --- AÑADIDO ---
        model.addAttribute("estatusDisponibles", EstatusCotizacion.values()); // Pasa la lista de estatus a la vista

        return "cotizaciones/form-productos";
    }

    @PostMapping("/productos")
    public String crearCotizacionProductos(@ModelAttribute CotizacionProductosCreateDTO dto) {
        Empresa miEmpresa = empresaService.findMiEmpresa().orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());
        CotizacionProductosDTO nuevaCotizacion = cotizacionService.crearCotizacionProductos(dto);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/{id}")
    public String verDetalleCotizacion(@PathVariable Long id, Model model) {
        Object cotizacionDto = cotizacionService.findCotizacionById(id);
        model.addAttribute("cotizacion", cotizacionDto);
        return "cotizaciones/detalle";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdfCotizacion(@PathVariable Long id) {
        try {
            Object cotizacionDto = cotizacionService.findCotizacionById(id);
            String folio = "";
            if (cotizacionDto instanceof CotizacionServiciosDTO) {
                folio = ((CotizacionServiciosDTO) cotizacionDto).getFolio();
            } else if (cotizacionDto instanceof CotizacionProductosDTO) {
                folio = ((CotizacionProductosDTO) cotizacionDto).getFolio();
            }

            byte[] pdfBytes = pdfGenerationService.generarPdfCotizacion(id);
            String filename = "Cotizacion-" + folio + ".pdf";

            // --- CORRECCIÓN: AÑADIR ENCABEZADOS ANTI-CACHÉ ---
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", filename); // 'inline' para abrir en el navegador

            // Instrucciones para el navegador y proxies para no cachear
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
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
    private void addCommonAttributesToModel(Model model, Empresa vendedor) {
        model.addAttribute("vendedor", vendedor);
        model.addAttribute("clientes", empresaService.findClientes());
        model.addAttribute("estatusDisponibles", EstatusCotizacion.values());
    }
    // ... (dentro de la clase CotizacionController)

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Object cotizacionDto = cotizacionService.findCotizacionById(id);

        if (cotizacionDto instanceof CotizacionServiciosDTO) {
            CotizacionServiciosDTO dto = (CotizacionServiciosDTO) cotizacionDto;
            Long vendedorId = dto.getVendedor().getId();
            Empresa vendedor = empresaService.findById(vendedorId)
                    .orElseThrow(() -> new IllegalStateException("El vendedor de la cotización no fue encontrado."));
            addCommonAttributesToModel(model, vendedor);

            CotizacionServiciosCreateDTO createDto = new CotizacionServiciosCreateDTO();
            createDto.setClienteId(dto.getCliente().getId());
            createDto.setVendedorId(dto.getVendedor().getId());
            createDto.setEstatus(dto.getEstatus());
            createDto.setVigencia(dto.getVigencia());
            createDto.setFormaPago(dto.getFormaPago());
            createDto.setDescripcionGeneral(dto.getDescripcionGeneral());
            createDto.setMetodosAceptados(dto.getMetodosAceptados());
            createDto.setCondicionesEntrega(dto.getCondicionesEntrega());
            createDto.setTiempoRespuesta(dto.getTiempoRespuesta());
            createDto.setAplicarIva(dto.isAplicarIva());
            createDto.setPorcentajeIva(dto.getPorcentajeIva());

            createDto.setLineas(dto.getLineas().stream().map(l -> {
                LineaCotizacionServicioCreateDTO linea = new LineaCotizacionServicioCreateDTO();
                linea.setConcepto(l.getConcepto());
                linea.setCantidad(l.getCantidad());
                linea.setUnidad(l.getUnidad());
                linea.setPrecioUnitario(l.getPrecioUnitario());
                return linea;
            }).collect(Collectors.toList()));

            model.addAttribute("cotizacion", createDto);
            model.addAttribute("cotizacionId", id);

            return "cotizaciones/form-servicios";

        } else if (cotizacionDto instanceof CotizacionProductosDTO) {
            CotizacionProductosDTO dto = (CotizacionProductosDTO) cotizacionDto;
            Long vendedorId = dto.getVendedor().getId();
            Empresa vendedor = empresaService.findById(vendedorId)
                    .orElseThrow(() -> new IllegalStateException("El vendedor de la cotización no fue encontrado."));
            addCommonAttributesToModel(model, vendedor);
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

            createDto.setLineas(dto.getLineas().stream().map(l -> {
                LineaCotizacionProductoCreateDTO linea = new LineaCotizacionProductoCreateDTO();
                linea.setProductoId(l.getProductoId());
                linea.setCantidad(l.getCantidad());
                linea.setUnidad(l.getUnidad());
                linea.setPrecioUnitario(l.getPrecioUnitario());
                return linea;
            }).collect(Collectors.toList()));

            model.addAttribute("cotizacion", createDto);
            model.addAttribute("cotizacionId", id);
            model.addAttribute("productos", productoRepository.findAll());

            return "cotizaciones/form-productos";
        }

        return "redirect:/";
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
}