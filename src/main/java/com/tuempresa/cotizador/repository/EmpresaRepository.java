package com.tuempresa.cotizador.repository;

import com.tuempresa.cotizador.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    /**
     * Busca la única empresa marcada como "Mi Empresa".
     * @param esMiEmpresa Debería ser siempre 'true' para esta consulta.
     * @return Un Optional que contiene la empresa vendedora si existe.
     */
    Optional<Empresa> findByEsMiEmpresa(boolean esMiEmpresa);

    /**
     * Busca todas las empresas que NO están marcadas como "Mi Empresa" (es decir, los clientes).
     * @param esMiEmpresa Debería ser siempre 'false' para esta consulta.
     * @return Una lista de empresas cliente.
     */
    List<Empresa> findAllByEsMiEmpresa(boolean esMiEmpresa);
}