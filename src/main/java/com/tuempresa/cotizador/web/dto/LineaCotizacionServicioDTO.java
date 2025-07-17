package com.tuempresa.cotizador.web.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LineaCotizacionServicioDTO {
    private Long id;
    private String concepto;
    private double cantidad;
    private String unidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}