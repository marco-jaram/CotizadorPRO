
package com.tuempresa.cotizador.service;

import com.tuempresa.cotizador.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<Producto> findAllByUsuarioId(Long usuarioId);
    Optional<Producto> findByIdAndUsuarioId(Long id, Long usuarioId);
    Producto guardarProducto(Producto producto, Long usuarioId);
    void eliminarProducto(Long id, Long usuarioId);
}