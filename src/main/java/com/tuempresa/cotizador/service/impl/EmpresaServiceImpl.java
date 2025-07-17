// src/main/java/com/tuempresa/cotizador/service/impl/EmpresaServiceImpl.java
package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.repository.EmpresaRepository;
import com.tuempresa.cotizador.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private static final long MAX_LOGO_SIZE = 3 * 1024 * 1024; // 3 MB

    @Override
    public Empresa guardarEmpresa(Empresa empresa, MultipartFile logoFile) throws IOException {
        if (logoFile != null && !logoFile.isEmpty()) {
            if (logoFile.getSize() > MAX_LOGO_SIZE) {
                throw new IOException("El archivo del logo no debe exceder los 3MB.");
            }
            empresa.setLogo(logoFile.getBytes());
        }
        return empresaRepository.save(empresa);
    }

    @Override
    public Optional<Empresa> findById(Long id) {
        return empresaRepository.findById(id);
    }

    @Override
    public List<Empresa> findAll() {
        return empresaRepository.findAll();
    }
}