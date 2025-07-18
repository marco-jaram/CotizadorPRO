package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.repository.EmpresaRepository; // IMPORTAR
import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.service.PdfGenerationService;
import com.tuempresa.cotizador.web.dto.CotizacionProductosDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.util.Base64; // IMPORTAR
import java.math.BigDecimal; // IMPORTAR

@Service
@RequiredArgsConstructor
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final CotizacionService cotizacionService;
    private final EmpresaRepository empresaRepository; // AÑADIR
    private final TemplateEngine templateEngine;

    @Override
    public byte[] generarPdfCotizacion(Long cotizacionId) throws Exception {
        Object cotizacionDto = cotizacionService.findCotizacionById(cotizacionId);

        Context context = new Context();
        context.setVariable("cotizacion", cotizacionDto);

        // --- LÓGICA MEJORADA PARA EL LOGO Y TOTALES ---
        String logoBase64 = getMiEmpresaLogoBase64();
        context.setVariable("logoBase64", logoBase64);

        BigDecimal subtotal = BigDecimal.ZERO;
        if (cotizacionDto instanceof CotizacionServiciosDTO) {
            subtotal = ((CotizacionServiciosDTO) cotizacionDto).getTotal();
        } else if (cotizacionDto instanceof CotizacionProductosDTO) {
            subtotal = ((CotizacionProductosDTO) cotizacionDto).getTotal();
        }

        BigDecimal iva = subtotal.multiply(new BigDecimal("0.16")); // Asumiendo 16% de IVA
        BigDecimal totalConIva = subtotal.add(iva);

        context.setVariable("subtotal", subtotal);
        context.setVariable("iva", iva);
        context.setVariable("totalConIva", totalConIva);
        // --- FIN DE LA LÓGICA MEJORADA ---

        String htmlContent = templateEngine.process("pdf/pdf_template_cotizacion", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        String baseUrl = Paths.get("src/main/resources/templates/").toUri().toURL().toString();
        renderer.setDocumentFromString(htmlContent, baseUrl);

        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.close();

        return outputStream.toByteArray();
    }

    private String getMiEmpresaLogoBase64() {
        // Busca "Mi Empresa" y convierte el logo a una cadena Base64 para el HTML
        return empresaRepository.findByEsMiEmpresa(true)
                .map(Empresa::getLogo)
                .map(logoBytes -> "data:image/png;base64," + Base64.getEncoder().encodeToString(logoBytes))
                .orElse(""); // Devuelve una cadena vacía si no hay logo
    }
}