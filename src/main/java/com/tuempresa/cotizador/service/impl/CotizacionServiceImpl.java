package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.exception.ResourceNotFoundException;
import com.tuempresa.cotizador.model.*;
import com.tuempresa.cotizador.repository.CotizacionRepository;
import com.tuempresa.cotizador.repository.EmpresaRepository;
import com.tuempresa.cotizador.repository.ProductoRepository;
import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.service.UsuarioService;
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
    private final UsuarioService usuarioService;

    @Override
    @Transactional
    public CotizacionServiciosDTO crearCotizacionServicios(CotizacionServiciosCreateDTO dto) {
        User usuarioActual = usuarioService.getUsuarioActual();
        Empresa cliente = empresaRepository.findByIdAndUser(dto.getClienteId(), usuarioActual)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        Empresa vendedor = empresaRepository.findByIdAndUser(dto.getVendedorId(), usuarioActual)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor no encontrado"));

        CotizacionServicios cotizacion = new CotizacionServicios();
        cotizacion.setUser(usuarioActual);
        cotizacion.setCliente(cliente);
        cotizacion.setVendedor(vendedor);
        cotizacion.setVigencia(dto.getVigencia());
        cotizacion.setDescripcionGeneral(dto.getDescripcionGeneral());
        cotizacion.setFormaPago(dto.getFormaPago());
        cotizacion.setMetodosAceptados(dto.getMetodosAceptados());
        cotizacion.setCondicionesEntrega(dto.getCondicionesEntrega());
        cotizacion.setTiempoRespuesta(dto.getTiempoRespuesta());
        cotizacion.setFolio(generarFolioUnico());
        cotizacion.setFechaEmision(LocalDate.now());
        cotizacion.setEstatus(dto.getEstatus());
        cotizacion.setAplicarIva(dto.isAplicarIva());
        cotizacion.setPorcentajeIva(dto.getPorcentajeIva());

        List<LineaCotizacionServicio> lineas = dto.getLineas().stream().map(lineaDto -> {
            LineaCotizacionServicio linea = new LineaCotizacionServicio();
            linea.setConcepto(lineaDto.getConcepto());
            linea.setCantidad(lineaDto.getCantidad());
            linea.setUnidad(lineaDto.getUnidad());
            linea.setPrecioUnitario(lineaDto.getPrecioUnitario());
            linea.setSubtotal(lineaDto.getPrecioUnitario().multiply(BigDecimal.valueOf(lineaDto.getCantidad())));
            linea.setCotizacionServicios(cotizacion);
            return linea;
        }).collect(Collectors.toList());

        cotizacion.setLineas(lineas);
        return mapToServiciosDTO(cotizacionRepository.save(cotizacion));
    }

    @Override
    @Transactional
    public CotizacionProductosDTO crearCotizacionProductos(CotizacionProductosCreateDTO dto) {
        User usuarioActual = usuarioService.getUsuarioActual();
        Empresa cliente = empresaRepository.findByIdAndUser(dto.getClienteId(), usuarioActual)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        Empresa vendedor = empresaRepository.findByIdAndUser(dto.getVendedorId(), usuarioActual)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor no encontrado"));

        CotizacionProductos cotizacion = new CotizacionProductos();
        cotizacion.setUser(usuarioActual);
        cotizacion.setCliente(cliente);
        cotizacion.setVendedor(vendedor);
        cotizacion.setVigencia(dto.getVigencia());
        cotizacion.setCondicionesEntrega(dto.getCondicionesEntrega());
        cotizacion.setGarantia(dto.getGarantia());
        cotizacion.setPoliticaDevoluciones(dto.getPoliticaDevoluciones());
        cotizacion.setFormasPago(dto.getFormasPago());
        cotizacion.setFolio(generarFolioUnico());
        cotizacion.setFechaEmision(LocalDate.now());
        cotizacion.setEstatus(dto.getEstatus());
        cotizacion.setAplicarIva(dto.isAplicarIva());
        cotizacion.setPorcentajeIva(dto.getPorcentajeIva());

        List<LineaCotizacionProducto> lineas = dto.getLineas().stream().map(lineaDto -> {
            Producto producto = productoRepository.findByIdAndUser(lineaDto.getProductoId(), usuarioActual)
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

        cotizacion.setLineas(lineas);
        return mapToProductosDTO(cotizacionRepository.save(cotizacion));
    }

    @Override
    @Transactional
    public CotizacionServiciosDTO actualizarCotizacionServicios(Long id, CotizacionServiciosCreateDTO dto) {
        User usuarioActual = usuarioService.getUsuarioActual();
        CotizacionServicios cotizacion = (CotizacionServicios) cotizacionRepository.findByIdAndUser(id, usuarioActual)
                .filter(c -> c instanceof CotizacionServicios)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización de servicios no encontrada"));

        Empresa cliente = empresaRepository.findByIdAndUser(dto.getClienteId(), usuarioActual).orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        cotizacion.setCliente(cliente);
        cotizacion.setEstatus(dto.getEstatus());
        cotizacion.setVigencia(dto.getVigencia());
        cotizacion.setDescripcionGeneral(dto.getDescripcionGeneral());
        cotizacion.setFormaPago(dto.getFormaPago());
        cotizacion.setMetodosAceptados(dto.getMetodosAceptados());
        cotizacion.setCondicionesEntrega(dto.getCondicionesEntrega());
        cotizacion.setTiempoRespuesta(dto.getTiempoRespuesta());
        cotizacion.setAplicarIva(dto.isAplicarIva());
        cotizacion.setPorcentajeIva(dto.getPorcentajeIva());

        cotizacion.getLineas().clear();
        List<LineaCotizacionServicio> nuevasLineas = dto.getLineas().stream().map(lineaDto -> {
            LineaCotizacionServicio linea = new LineaCotizacionServicio();
            linea.setConcepto(lineaDto.getConcepto());
            linea.setCantidad(lineaDto.getCantidad());
            linea.setUnidad(lineaDto.getUnidad());
            linea.setPrecioUnitario(lineaDto.getPrecioUnitario());
            linea.setSubtotal(lineaDto.getPrecioUnitario().multiply(BigDecimal.valueOf(lineaDto.getCantidad())));
            linea.setCotizacionServicios(cotizacion);
            return linea;
        }).collect(Collectors.toList());
        cotizacion.getLineas().addAll(nuevasLineas);

        return mapToServiciosDTO(cotizacionRepository.save(cotizacion));
    }

    @Override
    @Transactional
    public CotizacionProductosDTO actualizarCotizacionProductos(Long id, CotizacionProductosCreateDTO dto) {
        User usuarioActual = usuarioService.getUsuarioActual();
        CotizacionProductos cotizacion = (CotizacionProductos) cotizacionRepository.findByIdAndUser(id, usuarioActual)
                .filter(c -> c instanceof CotizacionProductos)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización de productos no encontrada"));

        Empresa cliente = empresaRepository.findByIdAndUser(dto.getClienteId(), usuarioActual).orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        cotizacion.setCliente(cliente);
        cotizacion.setEstatus(dto.getEstatus());
        cotizacion.setVigencia(dto.getVigencia());
        cotizacion.setCondicionesEntrega(dto.getCondicionesEntrega());
        cotizacion.setGarantia(dto.getGarantia());
        cotizacion.setPoliticaDevoluciones(dto.getPoliticaDevoluciones());
        cotizacion.setFormasPago(dto.getFormasPago());
        cotizacion.setAplicarIva(dto.isAplicarIva());
        cotizacion.setPorcentajeIva(dto.getPorcentajeIva());

        cotizacion.getLineas().clear();
        List<LineaCotizacionProducto> nuevasLineas = dto.getLineas().stream().map(lineaDto -> {
            Producto producto = productoRepository.findByIdAndUser(lineaDto.getProductoId(), usuarioActual)
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

        return mapToProductosDTO(cotizacionRepository.save(cotizacion));
    }

    @Override
    @Transactional(readOnly = true)
    public Object findById(Long id) {
        User usuarioActual = usuarioService.getUsuarioActual();
        Cotizacion cotizacion = cotizacionRepository.findByIdAndUser(id, usuarioActual)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con ID: " + id));
        if (cotizacion instanceof CotizacionServicios) {
            return mapToServiciosDTO((CotizacionServicios) cotizacion);
        } else if (cotizacion instanceof CotizacionProductos) {
            return mapToProductosDTO((CotizacionProductos) cotizacion);
        }
        throw new IllegalStateException("Tipo de cotización desconocido: " + cotizacion.getClass().getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object> findAllByUser() {
        User usuarioActual = usuarioService.getUsuarioActual();
        return cotizacionRepository.findAllByUser(usuarioActual).stream().map(cotizacion -> {
            if (cotizacion instanceof CotizacionServicios) {
                return mapToServiciosDTO((CotizacionServicios) cotizacion);
            } else if (cotizacion instanceof CotizacionProductos) {
                return mapToProductosDTO((CotizacionProductos) cotizacion);
            }
            return null;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    private String generarFolioUnico() {
        return "COT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private EmpresaDTO mapToEmpresaDTO(Empresa empresa) {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setId(empresa.getId());
        dto.setNombreEmpresa(empresa.getNombreEmpresa());
        dto.setNombreContacto(empresa.getNombreContacto());
        dto.setCorreo(empresa.getCorreo());
        dto.setDireccion(empresa.getDireccion());
        dto.setTelefono(empresa.getTelefono());
        dto.setRfc(empresa.getRfc());
        dto.setSitioWeb(empresa.getSitioWeb());
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
        dto.setMetodosAceptados(cotizacion.getMetodosAceptados());
        dto.setCondicionesEntrega(cotizacion.getCondicionesEntrega());
        dto.setTiempoRespuesta(cotizacion.getTiempoRespuesta());
        dto.setAplicarIva(cotizacion.isAplicarIva());
        dto.setPorcentajeIva(cotizacion.getPorcentajeIva());

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

            BigDecimal subtotal = lineasDTO.stream()
                    .map(LineaCotizacionServicioDTO::getSubtotal)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (cotizacion.isAplicarIva()) {
                BigDecimal iva = subtotal.multiply(cotizacion.getPorcentajeIva());
                dto.setTotal(subtotal.add(iva));
            } else {
                dto.setTotal(subtotal);
            }
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
        dto.setCondicionesEntrega(cotizacion.getCondicionesEntrega());
        dto.setPoliticaDevoluciones(cotizacion.getPoliticaDevoluciones());
        dto.setAplicarIva(cotizacion.isAplicarIva());
        dto.setPorcentajeIva(cotizacion.getPorcentajeIva());

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

            BigDecimal subtotal = lineasDTO.stream()
                    .map(LineaCotizacionProductoDTO::getSubtotal)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (cotizacion.isAplicarIva()) {
                BigDecimal iva = subtotal.multiply(cotizacion.getPorcentajeIva());
                dto.setTotal(subtotal.add(iva));
            } else {
                dto.setTotal(subtotal);
            }
        }
        return dto;
    }
}