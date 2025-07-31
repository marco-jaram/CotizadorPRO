package com.tuempresa.cotizador.repository;

import com.tuempresa.cotizador.model.Cotizacion;
import com.tuempresa.cotizador.security.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    Page<Cotizacion> findAllByUser(User user, Pageable pageable);
    List<Cotizacion> findAllByUser(User user);
    Optional<Cotizacion> findByIdAndUser(Long id, User user);
    @Query("SELECT c FROM Cotizacion c WHERE c.user = :user AND (" +
            "LOWER(c.folio) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.cliente.nombreEmpresa) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.cliente.correo) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    Page<Cotizacion> searchByUser(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

}