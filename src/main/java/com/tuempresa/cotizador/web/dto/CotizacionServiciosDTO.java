package com.tuempresa.cotizador.web.dto;

import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CotizacionServiciosDTO {
    private Long id;
    private String folio;
    private LocalDate fechaEmision;
    private String vigencia;
    private EstatusCotizacion estatus;
    private EmpresaDTO cliente;
    private EmpresaDTO vendedor;
    private String descripcionGeneral;
    private String formaPago;
    private List<LineaCotizacionServicioDTO> lineas;
    private BigDecimal total;
}