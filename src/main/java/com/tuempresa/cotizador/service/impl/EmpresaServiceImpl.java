
package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.repository.EmpresaRepository;
import com.tuempresa.cotizador.service.EmpresaService;

import jakarta.transaction.Transactional;
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
    public Empresa guardarEmpresa(Empresa empresa, Long usuarioId) {
        empresa.setUsuarioId(usuarioId);
        return empresaRepository.save(empresa);
    }

    @Override
    public Optional<Empresa> findByIdAndUsuarioId(Long id, Long usuarioId) {
        return empresaRepository.findByIdAndUsuarioId(id, usuarioId);
    }

    @Override
    public List<Empresa> findAllByUsuarioId(Long usuarioId) {
        return empresaRepository.findAllByUsuarioId(usuarioId);
    }

    @Override
    public Optional<Empresa> findMiEmpresaByUsuarioId(Long usuarioId) {
        return empresaRepository.findByEsMiEmpresaAndUsuarioId(true, usuarioId);
    }

    @Override
    public List<Empresa> findClientesByUsuarioId(Long usuarioId) {
        return empresaRepository.findAllByEsMiEmpresaAndUsuarioId(false, usuarioId);
    }

    @Override
    @Transactional
    public Empresa guardarMiEmpresa(Empresa empresaDataFromForm, MultipartFile logoFile, Long usuarioId) throws IOException {
        Empresa miEmpresaToSave = empresaRepository.findByEsMiEmpresaAndUsuarioId(true, usuarioId)
                .orElse(new Empresa());

        miEmpresaToSave.setUsuarioId(usuarioId);
        miEmpresaToSave.setNombreEmpresa(empresaDataFromForm.getNombreEmpresa());
        miEmpresaToSave.setNombreContacto(empresaDataFromForm.getNombreContacto());
        miEmpresaToSave.setCorreo(empresaDataFromForm.getCorreo());
        miEmpresaToSave.setTelefono(empresaDataFromForm.getTelefono());
        miEmpresaToSave.setRfc(empresaDataFromForm.getRfc());
        miEmpresaToSave.setSitioWeb(empresaDataFromForm.getSitioWeb());
        miEmpresaToSave.setDireccion(empresaDataFromForm.getDireccion());
        miEmpresaToSave.setEsMiEmpresa(true);

        if (logoFile != null && !logoFile.isEmpty()) {
            if (logoFile.getSize() > MAX_LOGO_SIZE) {
                throw new IOException("El archivo del logo no debe exceder los 3MB.");
            }
            miEmpresaToSave.setLogo(logoFile.getBytes());
        }

        return empresaRepository.save(miEmpresaToSave);
    }

    @Override
    @Transactional
    public void eliminarLogoMiEmpresa(Long usuarioId) {
        empresaRepository.findByEsMiEmpresaAndUsuarioId(true, usuarioId).ifPresent(miEmpresa -> {
            miEmpresa.setLogo(null);
            empresaRepository.save(miEmpresa);
        });
    }
}