package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.model.Empresa;
import com.tuempresa.cotizador.repository.EmpresaRepository;
import com.tuempresa.cotizador.security.model.User;
import com.tuempresa.cotizador.service.CotizacionService;
import com.tuempresa.cotizador.service.PdfGenerationService;
import com.tuempresa.cotizador.service.UsuarioService;
import com.tuempresa.cotizador.web.dto.CotizacionProductosDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.util.Base64;


@Service
@RequiredArgsConstructor
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final CotizacionService cotizacionService;
    private final UsuarioService usuarioService;
    private final EmpresaRepository empresaRepository; // Inyectar EmpresaRepository
    private final TemplateEngine templateEngine;

    @Override
    public byte[] generarPdfCotizacion(Long cotizacionId) throws Exception {
        // El servicio de cotización ya está securizado, así que findById es seguro.
        Object cotizacionDto = cotizacionService.findById(cotizacionId);
        User usuarioActual = usuarioService.getUsuarioActual();

        Context context = new Context();
        context.setVariable("cotizacion", cotizacionDto);
        context.setVariable("logoBase64", getMiEmpresaLogoBase64(usuarioActual));

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal porcentajeIva = BigDecimal.ZERO;
        boolean aplicarIva = false;
        BigDecimal total = BigDecimal.ZERO;

        if (cotizacionDto instanceof CotizacionServiciosDTO dto) {
            total = dto.getTotal();
            aplicarIva = dto.isAplicarIva();
            porcentajeIva = dto.getPorcentajeIva();
        } else if (cotizacionDto instanceof CotizacionProductosDTO dto) {
            total = dto.getTotal();
            aplicarIva = dto.isAplicarIva();
            porcentajeIva = dto.getPorcentajeIva();
        }

        if (aplicarIva && porcentajeIva.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = total.divide(BigDecimal.ONE.add(porcentajeIva), 2, RoundingMode.HALF_UP);
        } else {
            subtotal = total;
        }

        BigDecimal iva = total.subtract(subtotal);

        context.setVariable("subtotal", subtotal);
        context.setVariable("iva", iva);
        context.setVariable("totalConIva", total);

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

    private String getMiEmpresaLogoBase64(User user) {
        return empresaRepository.findByEsMiEmpresaAndUser(true, user)
                .map(Empresa::getLogo)
                .filter(logoBytes -> logoBytes != null && logoBytes.length > 0)
                .map(logoBytes -> "data:image/png;base64," + Base64.getEncoder().encodeToString(logoBytes))
                .orElse("");
    }
}