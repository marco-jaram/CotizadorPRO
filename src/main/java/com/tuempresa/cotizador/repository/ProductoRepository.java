package com.tuempresa.cotizador.repository;

import com.tuempresa.cotizador.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findAllByUsuarioId(Long usuarioId);
    Optional<Producto> findByIdAndUsuarioId(Long id, Long usuarioId);
}