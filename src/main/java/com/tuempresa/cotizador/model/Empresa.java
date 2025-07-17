package com.tuempresa.cotizador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombreEmpresa;
    private String nombreContacto;
    private String correo;
    private String telefono;
    private String rfc;
    private String sitioWeb;
    private String direccion;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "logo", columnDefinition="BLOB")
    private byte[] logo;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatoBancario> datosBancarios;
}