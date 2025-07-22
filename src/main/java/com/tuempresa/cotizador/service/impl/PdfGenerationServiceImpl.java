package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.repository.EmpresaRepository;
import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.service.PdfGenerationService;
import com.tuempresa.cotizador.web.dto.CotizacionProductosDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosDTO;
import com.tuempresa.cotizador.web.dto.LineaCotizacionProductoDTO;
import com.tuempresa.cotizador.web.dto.LineaCotizacionServicioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final CotizacionService cotizacionService;
    private final EmpresaRepository empresaRepository; // Asegúrate de que esta inyección esté
    private final TemplateEngine templateEngine;

    @Override
    public byte[] generarPdfCotizacion(Long cotizacionId) throws Exception {
        Object cotizacionDto = cotizacionService.findCotizacionById(cotizacionId);

        Context context = new Context();
        context.setVariable("cotizacion", cotizacionDto);

        // 1. Obtener y codificar el logo de "Mi Empresa" en Base64
        String logoBase64 = getMiEmpresaLogoBase64();
        context.setVariable("logoBase64", logoBase64);

        // 2. Calcular totales para la plantilla
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal porcentajeIva = BigDecimal.ZERO;
        boolean aplicarIva = false;

        if (cotizacionDto instanceof CotizacionServiciosDTO) {
            CotizacionServiciosDTO dto = (CotizacionServiciosDTO) cotizacionDto;
            subtotal = dto.getLineas().stream()
                    .map(LineaCotizacionServicioDTO::getSubtotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            porcentajeIva = dto.getPorcentajeIva();
            aplicarIva = dto.isAplicarIva();

    } else if (cotizacionDto instanceof CotizacionProductosDTO) {
        CotizacionProductosDTO dto = (CotizacionProductosDTO) cotizacionDto;
        subtotal = dto.getLineas().stream()
                .map(LineaCotizacionProductoDTO::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        porcentajeIva = dto.getPorcentajeIva();
        aplicarIva = dto.isAplicarIva();
    }

        BigDecimal iva = aplicarIva ? subtotal.multiply(porcentajeIva) : BigDecimal.ZERO;
        BigDecimal totalConIva = subtotal.add(iva);

        context.setVariable("subtotal", subtotal);
        context.setVariable("iva", iva);
        context.setVariable("totalConIva", totalConIva);


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
        return empresaRepository.findByEsMiEmpresa(true)
                .map(Empresa::getLogo)
                .filter(logoBytes -> logoBytes != null && logoBytes.length > 0)
                .map(logoBytes -> "data:image/png;base64," + Base64.getEncoder().encodeToString(logoBytes))
                .orElse(""); // Devuelve una cadena vacía si no hay logo
    }
}