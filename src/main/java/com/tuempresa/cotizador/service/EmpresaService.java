package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.model.Empresa;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface EmpresaService {

    Empresa guardarEmpresa(Empresa empresa, Long usuarioId);

    Optional<Empresa> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Empresa> findAllByUsuarioId(Long usuarioId);

    Optional<Empresa> findMiEmpresaByUsuarioId(Long usuarioId);

    List<Empresa> findClientesByUsuarioId(Long usuarioId);

    Empresa guardarMiEmpresa(Empresa empresa, MultipartFile logoFile, Long usuarioId) throws IOException;

    void eliminarLogoMiEmpresa(Long usuarioId);

}