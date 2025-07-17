package com.tuempresa.cotizador.web.dto;

import lombok.Data;
import java.util.List;

@Data
public class CotizacionProductosCreateDTO {
    private Long clienteId;
    private Long vendedorId;
    private String vigencia;
    private String condicionesEntrega;
    private String garantia;
    private String politicaDevoluciones;
    private String formasPago;
    private List<LineaCotizacionProductoCreateDTO> lineas;
}