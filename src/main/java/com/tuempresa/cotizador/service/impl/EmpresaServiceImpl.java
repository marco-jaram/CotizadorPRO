package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.repository.EmpresaRepository;
import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.service.EmpresaService;
import com.tuempresa.cotizador.service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;
    private static final long MAX_LOGO_SIZE = 3 * 1024 * 1024;

    @Override
    public Optional<Empresa> findById(Long id) {
        User usuarioActual = usuarioService.getUsuarioActual();
        return empresaRepository.findByIdAndUser(id, usuarioActual);
    }

    // --- Operaciones con "Mi Empresa" (Vendedor) ---

    @Override
    public Optional<Empresa> findMiEmpresaByUser() {
        User usuarioActual = usuarioService.getUsuarioActual();
        return empresaRepository.findByEsMiEmpresaAndUser(true, usuarioActual);
    }

    @Override
    @Transactional
    public Empresa guardarMiEmpresa(Empresa empresaDataFromForm, MultipartFile logoFile) throws IOException {
        User usuarioActual = usuarioService.getUsuarioActual();

        Empresa miEmpresaToSave = empresaRepository.findByEsMiEmpresaAndUser(true, usuarioActual)
                .orElse(new Empresa());

        miEmpresaToSave.setUser(usuarioActual);
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
        User usuarioActual = usuarioService.getUsuarioActual();
        empresaRepository.findByEsMiEmpresaAndUser(true, usuarioActual).ifPresent(miEmpresa -> {
            miEmpresa.setLogo(null);
            empresaRepository.save(miEmpresa);
        });
    }

    // --- Operaciones con Clientes ---

    @Override
    public Empresa guardarCliente(Empresa cliente) {
        User usuarioActual = usuarioService.getUsuarioActual();
        cliente.setUser(usuarioActual);
        cliente.setEsMiEmpresa(false); // Aseguramos que siempre se guarde como un cliente
        return empresaRepository.save(cliente);
    }

    @Override
    public Page<Empresa> findClientesByUser(Pageable pageable) {
        User usuarioActual = usuarioService.getUsuarioActual();
        // Llama al método del repositorio que acepta Pageable
        return empresaRepository.findAllByEsMiEmpresaAndUser(false, usuarioActual, pageable);
    }

    @Override
    public List<Empresa> findAllClientesByUser() {
        User usuarioActual = usuarioService.getUsuarioActual();
        // Llama al método del repositorio que devuelve la lista completa
        return empresaRepository.findAllByEsMiEmpresaAndUser(false, usuarioActual);
    }
}