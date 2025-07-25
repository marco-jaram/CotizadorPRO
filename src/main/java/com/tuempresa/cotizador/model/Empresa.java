package com.tuempresa.cotizador.model;

import com.tuempresa.cotizador.security.model.User;
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
    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "logo", columnDefinition="BLOB")
    private byte[] logo;

    @Column(name = "es_mi_empresa", nullable = false, columnDefinition = "boolean default false")
    private boolean esMiEmpresa = false;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatoBancario> datosBancarios;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}