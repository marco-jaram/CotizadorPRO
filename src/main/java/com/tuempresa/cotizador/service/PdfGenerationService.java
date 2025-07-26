package com.tuempresa.cotizador.service;

public interface PdfGenerationService {
    byte[] generarPdfCotizacion(Long cotizacionId) throws Exception;
}