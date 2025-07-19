// src/main/java/com/tuempresa/cotizador/model/Cotizacion.java
package com.tuempresa.cotizador.model;

import com.tuempresa.cotizador.model.enums.EstatusCotizacion;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_cotizacion", discriminatorType = DiscriminatorType.STRING)
public abstract class Cotizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String folio;
    private LocalDate fechaEmision;
    private String vigencia;
    private String moneda = "MXN";

    @Enumerated(EnumType.STRING)
    private EstatusCotizacion estatus;

    @Column(name = "aplicar_iva")
    private boolean aplicarIva = true;

    @Column(name = "porcentaje_iva")
    private BigDecimal porcentajeIva = new BigDecimal("0.16");


    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Empresa cliente;

    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Empresa vendedor;
}