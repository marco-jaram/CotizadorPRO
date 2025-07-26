
package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<Producto> findAllByUser();
    Optional<Producto> findById(Long id);
    Producto guardarProducto(Producto producto);
    void eliminarProducto(Long id);
}