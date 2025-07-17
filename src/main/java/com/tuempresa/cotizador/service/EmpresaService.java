package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.model.Empresa;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface EmpresaService {
    Empresa guardarEmpresa(Empresa empresa, MultipartFile logoFile) throws IOException;
    Optional<Empresa> findById(Long id);
    List<Empresa> findAll();
}