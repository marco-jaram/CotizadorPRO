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
    private String condicionesEntrega;
    private String garantia;
    private String politicaDevoluciones;
    private String formasPago;

    @OneToMany(mappedBy = "cotizacionProductos", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaCotizacionProducto> lineas;
}