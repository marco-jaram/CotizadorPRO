package com.tuempresa.cotizador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class LineaCotizacionProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int cantidad;
    private String unidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;


    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_productos_id")
    private CotizacionProductos cotizacionProductos;
}