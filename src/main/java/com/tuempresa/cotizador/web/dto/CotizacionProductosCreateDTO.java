package com.tuempresa.cotizador.web.dto;

import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import lombok.Data;
import java.util.List;

@Data
public class CotizacionProductosCreateDTO {
    private Long clienteId;
    private Long vendedorId;
    private EstatusCotizacion estatus;
    private String vigencia;
    private String condicionesEntrega;
    private String garantia;
    private String politicaDevoluciones;
    private String formasPago;
    private List<LineaCotizacionProductoCreateDTO> lineas;
}