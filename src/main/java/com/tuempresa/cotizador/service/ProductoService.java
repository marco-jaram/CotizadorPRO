
package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductoService {
    Page<Producto> findAllByUser(Pageable pageable);
    List<Producto> findAllByUser();
    Optional<Producto> findById(Long id);
    Producto guardarProducto(Producto producto);
    void eliminarProducto(Long id);
}