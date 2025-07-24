package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.web.dto.CotizacionProductosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionProductosDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosDTO;

import java.util.List;

public interface CotizacionService {

    CotizacionServiciosDTO crearCotizacionServicios(CotizacionServiciosCreateDTO dto, Long usuarioId);

    CotizacionProductosDTO crearCotizacionProductos(CotizacionProductosCreateDTO dto, Long usuarioId);

    CotizacionServiciosDTO actualizarCotizacionServicios(Long id, CotizacionServiciosCreateDTO dto, Long usuarioId);

    CotizacionProductosDTO actualizarCotizacionProductos(Long id, CotizacionProductosCreateDTO dto, Long usuarioId);

    Object findCotizacionByIdAndUsuarioId(Long id, Long usuarioId);

    List<Object> findAllCotizacionesByUsuarioId(Long usuarioId);
}