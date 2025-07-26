package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.model.Producto;
import com.tuempresa.cotizador.repository.ProductoRepository;
import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.service.ProductoService;
import com.tuempresa.cotizador.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService; // <-- Inyección clave

    @Override
    public List<Producto> findAllByUser() {
        User usuarioActual = usuarioService.getUsuarioActual();
        return productoRepository.findAllByUser(usuarioActual);
    }

    @Override
    public Optional<Producto> findById(Long id) {
        User usuarioActual = usuarioService.getUsuarioActual();
        return productoRepository.findByIdAndUser(id, usuarioActual);
    }

    @Override
    public Producto guardarProducto(Producto producto) {
        User usuarioActual = usuarioService.getUsuarioActual();
        producto.setUser(usuarioActual); // Asigna el dueño
        return productoRepository.save(producto);
    }

    @Override
    public void eliminarProducto(Long id) {
        User usuarioActual = usuarioService.getUsuarioActual();
        // Verificamos que el producto a eliminar realmente pertenezca al usuario
        productoRepository.findByIdAndUser(id, usuarioActual).ifPresent(producto -> {
            productoRepository.deleteById(id);
        });
    }
}