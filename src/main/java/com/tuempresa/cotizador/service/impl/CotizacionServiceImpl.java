package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.exception.ResourceNotFoundException;
import com.tuempresa.cotizador.model.*;
import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import com.tuempresa.cotizador.repository.*;
import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl implements CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final EmpresaRepository empresaRepository;
    private final ProductoRepository productoRepository;

    // =================================================================
    // MÉTODOS DE CREACIÓN
    // =================================================================

    @Override
    @Transactional
    public CotizacionServiciosDTO crearCotizacionServicios(CotizacionServiciosCreateDTO dto) {
        Empresa cliente = empresaRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + dto.getClienteId()));
        Empresa vendedor = empresaRepository.findById(dto.getVendedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor no encontrado con ID: " + dto.getVendedorId()));

        CotizacionServicios cotizacion = new CotizacionServicios();
        cotizacion.setCliente(cliente);
        cotizacion.setVendedor(vendedor);
        cotizacion.setVigencia(dto.getVigencia());
        cotizacion.setDescripcionGeneral(dto.getDescripcionGeneral());
        cotizacion.setFormaPago(dto.getFormaPago());
        cotizacion.setMetodosAceptados(dto.getMetodosAceptados());
        cotizacion.setCondicionesEntrega(dto.getCondicionesEntrega());
        cotizacion.setTiempoRespuesta(dto.getTiempoRespuesta());

        // Valores por defecto y generados
        cotizacion.setFolio(generarFolioUnico());
        cotizacion.setFechaEmision(LocalDate.now());
        cotizacion.setEstatus(EstatusCotizacion.BORRADOR);

        List<LineaCotizacionServicio> lineas = dto.getLineas().stream()
                .map(lineaDto -> {
                    LineaCotizacionServicio linea = new LineaCotizacionServicio();
                    linea.setConcepto(lineaDto.getConcepto());
                    linea.setCantidad(lineaDto.getCantidad());
                    linea.setUnidad(lineaDto.getUnidad());
                    linea.setPrecioUnitario(lineaDto.getPrecioUnitario());
                    linea.setSubtotal(lineaDto.getPrecioUnitario().multiply(BigDecimal.valueOf(lineaDto.getCantidad())));
                    linea.setCotizacionServicios(cotizacion); // Bidireccionalidad
                    return linea;
                }).collect(Collectors.toList());

        cotizacion.setLineas(lineas);

        CotizacionServicios savedCotizacion = cotizacionRepository.save(cotizacion);
        return mapToServiciosDTO(savedCotizacion);
    }

    @Override
    @Transactional
    public CotizacionProductosDTO crearCotizacionProductos(CotizacionProductosCreateDTO dto) {
        Empresa cliente = empresaRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + dto.getClienteId()));
        Empresa vendedor = empresaRepository.findById(dto.getVendedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor no encontrado con ID: " + dto.getVendedorId()));

        CotizacionProductos cotizacion = new CotizacionProductos();
        cotizacion.setCliente(cliente);
        cotizacion.setVendedor(vendedor);
        cotizacion.setVigencia(dto.getVigencia());
        cotizacion.setCondicionesEntrega(dto.getCondicionesEntrega());
        cotizacion.setGarantia(dto.getGarantia());
        cotizacion.setPoliticaDevoluciones(dto.getPoliticaDevoluciones());
        cotizacion.setFormasPago(dto.getFormasPago());

        // Valores por defecto y generados
        cotizacion.setFolio(generarFolioUnico());
        cotizacion.setFechaEmision(LocalDate.now());
        cotizacion.setEstatus(EstatusCotizacion.BORRADOR);

        List<LineaCotizacionProducto> lineas = dto.getLineas().stream()
                .map(lineaDto -> {
                    Producto producto = productoRepository.findById(lineaDto.getProductoId())
                            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + lineaDto.getProductoId()));

                    LineaCotizacionProducto linea = new LineaCotizacionProducto();
                    linea.setProducto(producto);
                    linea.setCantidad(lineaDto.getCantidad());
                    linea.setUnidad(lineaDto.getUnidad());
                    linea.setPrecioUnitario(lineaDto.getPrecioUnitario());
                    linea.setSubtotal(lineaDto.getPrecioUnitario().multiply(BigDecimal.valueOf(lineaDto.getCantidad())));
                    linea.setCotizacionProductos(cotizacion); // Bidireccionalidad
                    return linea;
                }).collect(Collectors.toList());

        cotizacion.setLineas(lineas);

        CotizacionProductos savedCotizacion = cotizacionRepository.save(cotizacion);
        return mapToProductosDTO(savedCotizacion);
    }

    // =================================================================
    // MÉTODOS DE ACTUALIZACIÓN (EDICIÓN)
    // =================================================================

    @Override
    @Transactional
    public CotizacionServiciosDTO actualizarCotizacionServicios(Long id, CotizacionServiciosCreateDTO dto) {
        // 1. Busca la cotización existente o lanza una excepción si no la encuentra.
        CotizacionServicios cotizacion = (CotizacionServicios) cotizacionRepository.findById(id)
                .filter(c -> c instanceof CotizacionServicios) // Aseguramos que es del tipo correcto
                .orElseThrow(() -> new ResourceNotFoundException("Cotización de servicios no encontrada con ID: " + id));

        // 2. Actualiza los campos principales de la cotización
        cotizacion.setVigencia(dto.getVigencia());
        cotizacion.setDescripcionGeneral(dto.getDescripcionGeneral());
        cotizacion.setFormaPago(dto.getFormaPago());
        cotizacion.setMetodosAceptados(dto.getMetodosAceptados());
        cotizacion.setCondicionesEntrega(dto.getCondicionesEntrega());
        cotizacion.setTiempoRespuesta(dto.getTiempoRespuesta());

        // 3. Reemplazar las líneas existentes (el enfoque más simple y robusto)
        cotizacion.getLineas().clear(); // Gracias a orphanRemoval=true, JPA borrará las líneas viejas de la BD.

        List<LineaCotizacionServicio> nuevasLineas = dto.getLineas().stream()
                .map(lineaDto -> {
                    LineaCotizacionServicio linea = new LineaCotizacionServicio();
                    linea.setConcepto(lineaDto.getConcepto());
                    linea.setCantidad(lineaDto.getCantidad());
                    linea.setUnidad(lineaDto.getUnidad());
                    linea.setPrecioUnitario(lineaDto.getPrecioUnitario());
                    linea.setSubtotal(lineaDto.getPrecioUnitario().multiply(BigDecimal.valueOf(lineaDto.getCantidad())));
                    linea.setCotizacionServicios(cotizacion);
                    return linea;
                }).collect(Collectors.toList());

        cotizacion.getLineas().addAll(nuevasLineas); // Añadimos las nuevas líneas a la colección

        CotizacionServicios savedCotizacion = cotizacionRepository.save(cotizacion);
        return mapToServiciosDTO(savedCotizacion);
    }

    @Override
    @Transactional
    public CotizacionProductosDTO actualizarCotizacionProductos(Long id, CotizacionProductosCreateDTO dto) {
        CotizacionProductos cotizacion = (CotizacionProductos) cotizacionRepository.findById(id)
                .filter(c -> c instanceof CotizacionProductos) // Aseguramos que es del tipo correcto
                .orElseThrow(() -> new ResourceNotFoundException("Cotización de productos no encontrada con ID: " + id));

        // Actualiza los campos principales
        cotizacion.setVigencia(dto.getVigencia());
        cotizacion.setCondicionesEntrega(dto.getCondicionesEntrega());
        cotizacion.setGarantia(dto.getGarantia());
        cotizacion.setPoliticaDevoluciones(dto.getPoliticaDevoluciones());
        cotizacion.setFormasPago(dto.getFormasPago());

        // Reemplazar las líneas existentes
        cotizacion.getLineas().clear();

        List<LineaCotizacionProducto> nuevasLineas = dto.getLineas().stream()
                .map(lineaDto -> {
                    Producto producto = productoRepository.findById(lineaDto.getProductoId())
                            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + lineaDto.getProductoId()));

                    LineaCotizacionProducto linea = new LineaCotizacionProducto();
                    linea.setProducto(producto);
                    linea.setCantidad(lineaDto.getCantidad());
                    linea.setUnidad(lineaDto.getUnidad());
                    linea.setPrecioUnitario(lineaDto.getPrecioUnitario());
                    linea.setSubtotal(lineaDto.getPrecioUnitario().multiply(BigDecimal.valueOf(lineaDto.getCantidad())));
                    linea.setCotizacionProductos(cotizacion);
                    return linea;
                }).collect(Collectors.toList());

        cotizacion.getLineas().addAll(nuevasLineas);

        CotizacionProductos savedCotizacion = cotizacionRepository.save(cotizacion);
        return mapToProductosDTO(savedCotizacion);
    }

    // =================================================================
    // MÉTODOS DE BÚSQUEDA Y UTILIDADES
    // =================================================================

    @Override
    @Transactional(readOnly = true)
    public Object findCotizacionById(Long id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con ID: " + id));

        if (cotizacion instanceof CotizacionServicios) {
            return mapToServiciosDTO((CotizacionServicios) cotizacion);
        } else if (cotizacion instanceof CotizacionProductos) {
            return mapToProductosDTO((CotizacionProductos) cotizacion);
        }
        throw new IllegalStateException("Tipo de cotización desconocido: " + cotizacion.getClass().getName());
    }

    private String generarFolioUnico() {
        return "COT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // =================================================================
    // MÉTODOS DE MAPEO (DTOs)
    // =================================================================

    private EmpresaDTO mapToEmpresaDTO(Empresa empresa) {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setId(empresa.getId());
        dto.setNombreEmpresa(empresa.getNombreEmpresa());
        dto.setNombreContacto(empresa.getNombreContacto());
        dto.setCorreo(empresa.getCorreo());
        dto.setDireccion(empresa.getDireccion());
        dto.setTieneLogo(empresa.getLogo() != null && empresa.getLogo().length > 0);
        return dto;
    }

    private CotizacionServiciosDTO mapToServiciosDTO(CotizacionServicios cotizacion) {
        CotizacionServiciosDTO dto = new CotizacionServiciosDTO();
        dto.setId(cotizacion.getId());
        dto.setFolio(cotizacion.getFolio());
        dto.setFechaEmision(cotizacion.getFechaEmision());
        dto.setVigencia(cotizacion.getVigencia());
        dto.setEstatus(cotizacion.getEstatus());
        dto.setCliente(mapToEmpresaDTO(cotizacion.getCliente()));
        dto.setVendedor(mapToEmpresaDTO(cotizacion.getVendedor()));
        dto.setDescripcionGeneral(cotizacion.getDescripcionGeneral());
        dto.setFormaPago(cotizacion.getFormaPago());

        if (cotizacion.getLineas() != null) {
            List<LineaCotizacionServicioDTO> lineasDTO = cotizacion.getLineas().stream().map(linea -> {
                LineaCotizacionServicioDTO lineaDTO = new LineaCotizacionServicioDTO();
                lineaDTO.setId(linea.getId());
                lineaDTO.setConcepto(linea.getConcepto());
                lineaDTO.setCantidad(linea.getCantidad());
                lineaDTO.setUnidad(linea.getUnidad());
                lineaDTO.setPrecioUnitario(linea.getPrecioUnitario());
                lineaDTO.setSubtotal(linea.getSubtotal());
                return lineaDTO;
            }).collect(Collectors.toList());

            dto.setLineas(lineasDTO);
            dto.setTotal(lineasDTO.stream()
                    .map(LineaCotizacionServicioDTO::getSubtotal)
                    .filter(java.util.Objects::nonNull) // Filtra nulos por si acaso
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return dto;
    }

    private CotizacionProductosDTO mapToProductosDTO(CotizacionProductos cotizacion) {
        CotizacionProductosDTO dto = new CotizacionProductosDTO();
        dto.setId(cotizacion.getId());
        dto.setFolio(cotizacion.getFolio());
        dto.setFechaEmision(cotizacion.getFechaEmision());
        dto.setVigencia(cotizacion.getVigencia());
        dto.setEstatus(cotizacion.getEstatus());
        dto.setCliente(mapToEmpresaDTO(cotizacion.getCliente()));
        dto.setVendedor(mapToEmpresaDTO(cotizacion.getVendedor()));
        dto.setFormasPago(cotizacion.getFormasPago());
        dto.setGarantia(cotizacion.getGarantia());

        if (cotizacion.getLineas() != null) {
            List<LineaCotizacionProductoDTO> lineasDTO = cotizacion.getLineas().stream().map(linea -> {
                LineaCotizacionProductoDTO lineaDTO = new LineaCotizacionProductoDTO();
                lineaDTO.setId(linea.getId());
                if (linea.getProducto() != null) {
                    lineaDTO.setProductoId(linea.getProducto().getId());
                    lineaDTO.setProductoNombre(linea.getProducto().getNombre());
                }
                lineaDTO.setCantidad(linea.getCantidad());
                lineaDTO.setUnidad(linea.getUnidad());
                lineaDTO.setPrecioUnitario(linea.getPrecioUnitario());
                lineaDTO.setSubtotal(linea.getSubtotal());
                return lineaDTO;
            }).collect(Collectors.toList());

            dto.setLineas(lineasDTO);
            dto.setTotal(lineasDTO.stream()
                    .map(LineaCotizacionProductoDTO::getSubtotal)
                    .filter(java.util.Objects::nonNull) // Filtra nulos por si acaso
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return dto;
    }
    @Override
    @Transactional(readOnly = true)
    public List<Object> findAllCotizaciones() {
        return cotizacionRepository.findAll().stream()
                .map(cotizacion -> {
                    if (cotizacion instanceof CotizacionServicios) {
                        return mapToServiciosDTO((CotizacionServicios) cotizacion);
                    } else if (cotizacion instanceof CotizacionProductos) {
                        return mapToProductosDTO((CotizacionProductos) cotizacion);
                    }
                    return null; // O manejar un caso de error
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

}