
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
    public Empresa guardarEmpresa(Empresa empresa) {
        // MODIFICADO: Toda la lógica del logo ha sido eliminada.
        // La propiedad 'esMiEmpresa' por defecto es 'false', así que se guarda como cliente correctamente.
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

    @Override
    public Optional<Empresa> findMiEmpresa() {
        return empresaRepository.findByEsMiEmpresa(true);
    }

    @Override
    public List<Empresa> findClientes() {
        return empresaRepository.findAllByEsMiEmpresa(false);
    }

    @Override
    @Transactional
    public Empresa guardarMiEmpresa(Empresa empresaDataFromForm, MultipartFile logoFile) throws IOException {
        Empresa miEmpresaToSave = empresaRepository.findByEsMiEmpresa(true)
                .orElse(new Empresa());

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
    public void eliminarLogoMiEmpresa() {
        // Busca "Mi Empresa"
        empresaRepository.findByEsMiEmpresa(true).ifPresent(miEmpresa -> {
            // Si la encuentra, pone el logo a null y guarda los cambios
            miEmpresa.setLogo(null);
            empresaRepository.save(miEmpresa);
        });
    }
}