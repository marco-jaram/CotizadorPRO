package com.tuempresa.cotizador.repository;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByEsMiEmpresaAndUser(boolean esMiEmpresa, User user);
    List<Empresa> findAllByEsMiEmpresaAndUser(boolean esMiEmpresa, User user);
    List<Empresa> findAllByUser(User user);
    Optional<Empresa> findByIdAndUser(Long id, User user);

}