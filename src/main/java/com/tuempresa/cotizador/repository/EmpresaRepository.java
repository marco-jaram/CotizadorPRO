package com.tuempresa.cotizador.repository;

import com.tuempresa.cotizador.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByEsMiEmpresaAndUsuarioId(boolean esMiEmpresa, Long usuarioId);

    List<Empresa> findAllByEsMiEmpresaAndUsuarioId(boolean esMiEmpresa, Long usuarioId);

    List<Empresa> findAllByUsuarioId(Long usuarioId);

    Optional<Empresa> findByIdAndUsuarioId(Long id, Long usuarioId);

}