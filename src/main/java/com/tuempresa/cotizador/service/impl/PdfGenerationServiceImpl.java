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
    private final EmpresaRepository empresaRepository;
    private final TemplateEngine templateEngine;

    @Override
    public byte[] generarPdfCotizacion(Long cotizacionId, Long usuarioId) throws Exception {
        // --- CAMBIO CLAVE: Llama al nuevo método del servicio que verifica al usuario ---
        Object cotizacionDto = cotizacionService.findCotizacionByIdAndUsuarioId(cotizacionId, usuarioId);

        Context context = new Context();
        context.setVariable("cotizacion", cotizacionDto);

        // --- CAMBIO CLAVE: El logo ahora debe ser el del usuario específico ---
        String logoBase64 = getMiEmpresaLogoBase64(usuarioId);
        context.setVariable("logoBase64", logoBase64);

        // El resto de la lógica para calcular totales no cambia
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal porcentajeIva = BigDecimal.ZERO;
        boolean aplicarIva = false;

        if (cotizacionDto instanceof CotizacionServiciosDTO dto) {
            subtotal = dto.getLineas().stream()
                    .map(LineaCotizacionServicioDTO::getSubtotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            porcentajeIva = dto.getPorcentajeIva();
            aplicarIva = dto.isAplicarIva();
        } else if (cotizacionDto instanceof CotizacionProductosDTO dto) {
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

    private String getMiEmpresaLogoBase64(Long usuarioId) {
        // --- CAMBIO CLAVE: Busca "Mi Empresa" del usuario que solicita el PDF ---
        return empresaRepository.findByEsMiEmpresaAndUsuarioId(true, usuarioId)
                .map(Empresa::getLogo)
                .filter(logoBytes -> logoBytes != null && logoBytes.length > 0)
                .map(logoBytes -> "data:image/png;base64," + Base64.getEncoder().encodeToString(logoBytes))
                .orElse("");
    }
}