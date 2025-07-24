// Archivo: src/main/java/com/tuempresa/cotizador/service/impl/ProductoServiceImpl.java
package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.model.Producto;
import com.tuempresa.cotizador.repository.ProductoRepository;
import com.tuempresa.cotizador.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    public List<Producto> findAllByUsuarioId(Long usuarioId) {
        return productoRepository.findAllByUsuarioId(usuarioId);
    }

    @Override
    public Optional<Producto> findByIdAndUsuarioId(Long id, Long usuarioId) {
        return productoRepository.findByIdAndUsuarioId(id, usuarioId);
    }

    @Override
    public Producto guardarProducto(Producto producto, Long usuarioId) {
        producto.setUsuarioId(usuarioId);
        // Aquí podrías añadir validaciones, como verificar si el SKU ya existe para ese usuario
        return productoRepository.save(producto);
    }

    @Override
    public void eliminarProducto(Long id, Long usuarioId) {
        // Verificamos que el producto a eliminar realmente pertenezca al usuario
        productoRepository.findByIdAndUsuarioId(id, usuarioId).ifPresent(producto -> {
            productoRepository.deleteById(id);
        });
        // Si no pertenece al usuario, simplemente no hace nada (más seguro que lanzar excepción)
    }
}