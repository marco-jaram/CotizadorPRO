package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final CotizacionService cotizacionService;
    private final TemplateEngine templateEngine; // Spring Boot inyecta el motor de Thymeleaf automáticamente

    @Override
    public byte[] generarPdfCotizacion(Long cotizacionId) throws Exception {
        // 1. Obtener los datos completos de la cotización como un DTO
        Object cotizacionDto = cotizacionService.findCotizacionById(cotizacionId);

        // 2. Preparar el contexto de Thymeleaf con los datos que usará la plantilla
        Context context = new Context();
        context.setVariable("cotizacion", cotizacionDto);
        // Aquí podrías añadir más variables, como la fecha actual, etc.
        // context.setVariable("fechaImpresion", LocalDate.now());

        // 3. Procesar la plantilla HTML (ubicada en templates/pdf/) a un String en memoria
        String htmlContent = templateEngine.process("pdf/pdf_template_cotizacion", context);

        // 4. Usar Flying Saucer para convertir el String HTML a un PDF en un arreglo de bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        // 5. Establecer la URL base para que Flying Saucer pueda encontrar recursos como CSS o imágenes
        // Esto le dice que busque los recursos relativos a la carpeta /resources/templates/
        String baseUrl = Paths.get("src/main/resources/templates/").toUri().toURL().toString();
        renderer.setDocumentFromString(htmlContent, baseUrl);

        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.close();

        return outputStream.toByteArray();
    }
}