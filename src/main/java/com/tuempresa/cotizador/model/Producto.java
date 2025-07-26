// src/main/java/com/tuempresa/cotizador/model/Producto.java
package com.tuempresa.cotizador.model;

import com.tuempresa.cotizador.security.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sku;
    private String nombre;
    private String especificaciones;
    private BigDecimal precioUnitarioBase;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}