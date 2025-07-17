package com.tuempresa.cotizador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class LineaCotizacionServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String concepto;
    private double cantidad;
    private String unidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_servicios_id")
    private CotizacionServicios cotizacionServicios;
}