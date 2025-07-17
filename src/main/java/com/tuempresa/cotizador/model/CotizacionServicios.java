package com.tuempresa.cotizador.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("SERVICIOS")
public class CotizacionServicios extends Cotizacion {
    private String descripcionGeneral;
    private String formaPago;
    private String metodosAceptados;
    private String condicionesEntrega;
    private String tiempoRespuesta;

    @OneToMany(mappedBy = "cotizacionServicios", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaCotizacionServicio> lineas;
}