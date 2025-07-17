package com.tuempresa.cotizador.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DatoBancario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String banco;
    private String numeroCuenta;
    private String clabe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}