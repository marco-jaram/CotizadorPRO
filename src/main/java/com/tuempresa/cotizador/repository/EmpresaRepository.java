package com.tuempresa.cotizador.repository;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.security.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByEsMiEmpresaAndUser(boolean esMiEmpresa, User user);
    Page<Empresa> findAllByEsMiEmpresaAndUser(boolean esMiEmpresa, User user, Pageable pageable);
    List<Empresa> findAllByEsMiEmpresaAndUser(boolean esMiEmpresa, User user);
    Optional<Empresa> findByIdAndUser(Long id, User user);
    @Query("SELECT e FROM Empresa e WHERE e.user = :user AND e.esMiEmpresa = false AND (" +
            "LOWER(e.nombreEmpresa) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.nombreContacto) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.correo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.telefono) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    Page<Empresa> searchClientesByUser(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

}