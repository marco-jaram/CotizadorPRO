package com.tuempresa.cotizador.web.dto;

import lombok.Data;

@Data
public class EmpresaDTO {
    private Long id;
    private String nombreEmpresa;
    private String nombreContacto;
    private String correo;
    private String direccion;
    private String telefono;
    private String rfc;
    private String sitioWeb;
    private boolean tieneLogo;
}