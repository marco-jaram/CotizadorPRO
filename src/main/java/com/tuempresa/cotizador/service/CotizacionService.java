package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.web.dto.CotizacionProductosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionProductosDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosCreateDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CotizacionService {

    CotizacionServiciosDTO crearCotizacionServicios(CotizacionServiciosCreateDTO dto);
    CotizacionProductosDTO crearCotizacionProductos(CotizacionProductosCreateDTO dto);
    CotizacionServiciosDTO actualizarCotizacionServicios(Long id, CotizacionServiciosCreateDTO dto);
    CotizacionProductosDTO actualizarCotizacionProductos(Long id, CotizacionProductosCreateDTO dto);
    Object findById(Long id);
    List<Object> findAllByUserForExport();
    Page<Object> findAllByUser(Pageable pageable);
    Page<Object> searchByUser(String keyword, Pageable pageable);
}