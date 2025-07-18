package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.web.dto.CotizacionProductosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionProductosDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosDTO;

import java.util.List;

public interface CotizacionService {

    // --- MÉTODOS DE CREACIÓN ---
    CotizacionServiciosDTO crearCotizacionServicios(CotizacionServiciosCreateDTO dto);
    CotizacionProductosDTO crearCotizacionProductos(CotizacionProductosCreateDTO dto);

    // --- MÉTODOS DE ACTUALIZACIÓN (EDICIÓN) ---
    CotizacionServiciosDTO actualizarCotizacionServicios(Long id, CotizacionServiciosCreateDTO dto);
    CotizacionProductosDTO actualizarCotizacionProductos(Long id, CotizacionProductosCreateDTO dto);

    // --- MÉTODOS DE BÚSQUEDA ---
    Object findCotizacionById(Long id);

    // AÑADIDO: Nuevo método para el listado
    List<Object> findAllCotizaciones();
}