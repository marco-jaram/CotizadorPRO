package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.model.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface EmpresaService {

    Optional<Empresa> findById(Long id);
    Optional<Empresa> findMiEmpresaByUser();
    Empresa guardarMiEmpresa(Empresa empresa, MultipartFile logoFile) throws IOException;
    void eliminarLogoMiEmpresa();
    Empresa guardarCliente(Empresa cliente);
    Page<Empresa> findClientesByUser(Pageable pageable);
    List<Empresa> findAllClientesByUser();

}