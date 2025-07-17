// src/main/java/com/tuempresa/cotizador/model/Producto.java
package com.tuempresa.cotizador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String sku;
    private String nombre;
    private String especificaciones;
    private BigDecimal precioUnitarioBase;
}