package com.tuempresa.cotizador.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("PRODUCTOS")
public class CotizacionProductos extends Cotizacion {
    @Column(columnDefinition = "TEXT")
    private String condicionesEntrega;
    @Column(columnDefinition = "TEXT")
    private String garantia;
    @Column(columnDefinition = "TEXT")
    private String politicaDevoluciones;
    @Column(columnDefinition = "TEXT")
    private String formasPago;

    @OneToMany(mappedBy = "cotizacionProductos", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaCotizacionProducto> lineas;
}