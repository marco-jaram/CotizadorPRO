package com.tuempresa.cotizador.web.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LineaCotizacionProductoDTO {
    private Long id;
    private int cantidad;
    private String unidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private Long productoId;
    private String productoNombre;

}