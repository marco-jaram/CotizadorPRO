package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.web.dto.CotizacionProductosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionProductosDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosDTO;
import java.util.List;

public interface CotizacionService {

    CotizacionServiciosDTO crearCotizacionServicios(CotizacionServiciosCreateDTO dto);
    CotizacionProductosDTO crearCotizacionProductos(CotizacionProductosCreateDTO dto);
    CotizacionServiciosDTO actualizarCotizacionServicios(Long id, CotizacionServiciosCreateDTO dto);
    CotizacionProductosDTO actualizarCotizacionProductos(Long id, CotizacionProductosCreateDTO dto);
    Object findById(Long id);
    List<Object> findAllByUser();
}