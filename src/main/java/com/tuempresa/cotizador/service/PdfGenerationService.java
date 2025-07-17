package com.tuempresa.cotizador.service;

public interface PdfGenerationService {
    /**
     * Genera un archivo PDF para una cotización específica.
     * @param cotizacionId El ID de la cotización a convertir en PDF.
     * @return Un arreglo de bytes que representa el archivo PDF.
     * @throws Exception Si ocurre un error durante la generación.
     */
    byte[] generarPdfCotizacion(Long cotizacionId) throws Exception;
}