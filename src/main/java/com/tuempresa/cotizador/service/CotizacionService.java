package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.web.dto.CotizacionProductosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionProductosDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosDTO;

public interface CotizacionService {

    CotizacionServiciosDTO crearCotizacionServicios(CotizacionServiciosCreateDTO dto);

    CotizacionProductosDTO crearCotizacionProductos(CotizacionProductosCreateDTO dto);
    /**
     * Busca una cotización por su ID y la devuelve como DTO.
     * El objeto devuelto puede ser un CotizacionServiciosDTO o un CotizacionProductosDTO.
     * @param id El ID de la cotización a buscar.
     * @return Un DTO de la cotización encontrada.
     */
    Object findCotizacionById(Long id);

    // Aquí podrían ir otros métodos como:
    // void cambiarEstatus(Long id, EstatusCotizacion nuevoEstatus);
}