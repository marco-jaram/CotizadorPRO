package com.tuempresa.cotizador.repository;

import com.tuempresa.cotizador.model.Cotizacion;
import com.tuempresa.cotizador.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    List<Cotizacion> findAllByUser(User user);
    Optional<Cotizacion> findByIdAndUser(Long id, User user);

}