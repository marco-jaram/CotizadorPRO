package com.tuempresa.cotizador.service;

public interface PdfGenerationService {
    byte[] generarPdfCotizacion(Long cotizacionId, Long usuarioId) throws Exception;
}