package com.tuempresa.cotizador.web.dto;

import lombok.Data;
import java.util.List;

@Data
public class CotizacionServiciosCreateDTO {
    private Long clienteId;
    private Long vendedorId;
    private String vigencia;
    private String descripcionGeneral;
    private String formaPago;
    private String metodosAceptados;
    private String condicionesEntrega;
    private String tiempoRespuesta;
    private List<LineaCotizacionServicioCreateDTO> lineas;
}