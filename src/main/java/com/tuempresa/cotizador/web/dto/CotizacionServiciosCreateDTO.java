package com.tuempresa.cotizador.web.dto;

import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CotizacionServiciosCreateDTO {
    private Long clienteId;
    private Long vendedorId;
    private EstatusCotizacion estatus;
    private String vigencia;
    private String descripcionGeneral;
    private String formaPago;
    private String metodosAceptados;
    private String condicionesEntrega;
    private String tiempoRespuesta;
    private boolean aplicarIva;
    private BigDecimal porcentajeIva;
    private List<LineaCotizacionServicioCreateDTO> lineas;

}