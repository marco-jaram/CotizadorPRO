package com.tuempresa.cotizador.repository;

import com.tuempresa.cotizador.model.Producto;
import com.tuempresa.cotizador.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findAllByUser(User user);
    Optional<Producto> findByIdAndUser(Long id, User user);
}