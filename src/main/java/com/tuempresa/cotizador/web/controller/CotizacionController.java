package com.tuempresa.cotizador.web.controller;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.service.*;
import com.tuempresa.cotizador.service.UsuarioService;
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
    private final UsuarioService usuarioService; // <-- CAMBIO: Inyectar UsuarioService


    private Long getUsuarioId(Authentication authentication) {
        String userEmail = authentication.getName();
        return usuarioService.findByEmail(userEmail)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado para el email: " + userEmail));
    }

    private void addCommonAttributesToModel(Model model, Long usuarioId) {
        Empresa miEmpresa = empresaService.findMiEmpresaByUsuarioId(usuarioId)
                .orElse(new Empresa());
        model.addAttribute("vendedor", miEmpresa);
        model.addAttribute("clientes", empresaService.findClientesByUsuarioId(usuarioId));
        model.addAttribute("estatusDisponibles", EstatusCotizacion.values());
    }

    @GetMapping("/servicios/nueva")
    public String mostrarFormularioServicios(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {

        Long usuarioId = getUsuarioId(authentication);
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

        Long usuarioId = getUsuarioId(authentication);
        Empresa miEmpresa = empresaService.findMiEmpresaByUsuarioId(usuarioId).orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());
        CotizacionServiciosDTO nuevaCotizacion = cotizacionService.crearCotizacionServicios(dto, usuarioId);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/productos/nueva")
    public String mostrarFormularioProductos(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {

        Long usuarioId = getUsuarioId(authentication);
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

        Long usuarioId = getUsuarioId(authentication);
        Empresa miEmpresa = empresaService.findMiEmpresaByUsuarioId(usuarioId).orElseThrow(() -> new IllegalStateException("No se encontró 'Mi Empresa' al guardar."));
        dto.setVendedorId(miEmpresa.getId());
        CotizacionProductosDTO nuevaCotizacion = cotizacionService.crearCotizacionProductos(dto, usuarioId);
        return "redirect:/cotizaciones/" + nuevaCotizacion.getId();
    }

    @GetMapping("/{id}")
    public String verDetalleCotizacion(@PathVariable Long id, Model model, Authentication authentication) {

        Long usuarioId = getUsuarioId(authentication);
        Object cotizacionDto = cotizacionService.findCotizacionByIdAndUsuarioId(id, usuarioId);
        model.addAttribute("cotizacion", cotizacionDto);
        return "cotizaciones/detalle";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdfCotizacion(@PathVariable Long id, Authentication authentication) {

        Long usuarioId = getUsuarioId(authentication);
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

        Long usuarioId = getUsuarioId(authentication);
        List<Object> cotizaciones = cotizacionService.findAllCotizacionesByUsuarioId(usuarioId);
        model.addAttribute("cotizaciones", cotizaciones);
        return "cotizaciones/lista-cotizaciones";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
        Long usuarioId = getUsuarioId(authentication);
        Object cotizacionDto = cotizacionService.findCotizacionByIdAndUsuarioId(id, usuarioId);

        // CASO 1: LA COTIZACIÓN ES DE SERVICIOS
        if (cotizacionDto instanceof CotizacionServiciosDTO dto) {
            // Obtenemos los atributos comunes para el formulario
            addCommonAttributesToModel(model, usuarioId);

            // Creamos el objeto que espera el formulario (CreateDTO)
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
                List<LineaCotizacionServicioCreateDTO> lineasCreateDto = dto.getLineas().stream().map(lineaDto -> {
                    LineaCotizacionServicioCreateDTO lineaCreate = new LineaCotizacionServicioCreateDTO();
                    lineaCreate.setConcepto(lineaDto.getConcepto());
                    lineaCreate.setCantidad(lineaDto.getCantidad());
                    lineaCreate.setUnidad(lineaDto.getUnidad());
                    lineaCreate.setPrecioUnitario(lineaDto.getPrecioUnitario());
                    return lineaCreate;
                }).collect(java.util.stream.Collectors.toList());
                createDto.setLineas(lineasCreateDto);
            }


            // Pasamos los datos al modelo para que Thymeleaf los renderice
            model.addAttribute("cotizacion", createDto);
            model.addAttribute("cotizacionId", id);
            return "cotizaciones/form-servicios";

            // CASO 2: LA COTIZACIÓN ES DE PRODUCTOS
        } else if (cotizacionDto instanceof CotizacionProductosDTO dto) {
            // Obtenemos los atributos comunes para el formulario
            addCommonAttributesToModel(model, usuarioId);

            // Creamos el objeto que espera el formulario (CreateDTO)
            CotizacionProductosCreateDTO createDto = new CotizacionProductosCreateDTO();


            // Copiamos todos los datos del DTO de vista (dto) al DTO de creación/edición (createDto)
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
                List<LineaCotizacionProductoCreateDTO> lineasCreateDto = dto.getLineas().stream().map(lineaDto -> {
                    LineaCotizacionProductoCreateDTO lineaCreate = new LineaCotizacionProductoCreateDTO();
                    lineaCreate.setProductoId(lineaDto.getProductoId());
                    lineaCreate.setCantidad(lineaDto.getCantidad());
                    lineaCreate.setUnidad(lineaDto.getUnidad());
                    lineaCreate.setPrecioUnitario(lineaDto.getPrecioUnitario());
                    return lineaCreate;
                }).collect(java.util.stream.Collectors.toList());
                createDto.setLineas(lineasCreateDto);
            }


            // Pasamos los datos al modelo para que Thymeleaf los renderice
            model.addAttribute("cotizacion", createDto);
            model.addAttribute("cotizacionId", id); // Importante para que el form sepa que es una edición
            model.addAttribute("productos", productoService.findAllByUsuarioId(usuarioId)); // El formulario de productos necesita la lista de productos
            return "cotizaciones/form-productos";
        }

        // Si por alguna razón la cotización no es de ninguno de los tipos conocidos, redirigimos
        return "redirect:/cotizaciones";
    }

    @PostMapping("/servicios/{id}")
    public String actualizarCotizacionServicios(@PathVariable Long id, @ModelAttribute CotizacionServiciosCreateDTO dto, Authentication authentication) {

        Long usuarioId = getUsuarioId(authentication);
        cotizacionService.actualizarCotizacionServicios(id, dto, usuarioId);
        return "redirect:/cotizaciones/" + id;
    }

    @PostMapping("/productos/{id}")
    public String actualizarCotizacionProductos(@PathVariable Long id, @ModelAttribute CotizacionProductosCreateDTO dto, Authentication authentication) {

        Long usuarioId = getUsuarioId(authentication);
        cotizacionService.actualizarCotizacionProductos(id, dto, usuarioId);
        return "redirect:/cotizaciones/" + id;
    }
}