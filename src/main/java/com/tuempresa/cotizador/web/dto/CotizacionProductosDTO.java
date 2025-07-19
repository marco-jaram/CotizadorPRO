package com.tuempresa.cotizador.web.dto;

import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CotizacionProductosDTO {
    private Long id;
    private String folio;
    private LocalDate fechaEmision;
    private String vigencia;
    private EstatusCotizacion estatus;
    private EmpresaDTO cliente;
    private EmpresaDTO vendedor;
    private String formasPago;
    private String garantia;
    private String condicionesEntrega;
    private String politicaDevoluciones;
    private List<LineaCotizacionProductoDTO> lineas;
    private BigDecimal total;
}