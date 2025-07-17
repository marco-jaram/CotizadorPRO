package com.tuempresa.cotizador.web.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LineaCotizacionProductoCreateDTO {
    private Long productoId;
    private int cantidad;
    private String unidad;
    private BigDecimal precioUnitario;


}