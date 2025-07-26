// src/main/java/com/tuempresa/cotizador/service/impl/ExcelGenerationServiceImpl.java
package com.tuempresa.cotizador.service.impl;

import com.tuempresa.cotizador.service.ExcelGenerationService;
import com.tuempresa.cotizador.web.dto.CotizacionProductosDTO;
import com.tuempresa.cotizador.web.dto.CotizacionServiciosDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.FillPatternType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelGenerationServiceImpl implements ExcelGenerationService {

    @Override
    public ByteArrayInputStream generarExcelCotizaciones(List<Object> cotizacionesDtos) {
        // Usamos XSSFWorkbook para el formato .xlsx
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Cotizaciones");

            // Estilo para la cabecera
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));


            // Crear la fila de cabecera
            String[] headers = {"Folio", "Tipo", "Cliente", "Fecha Emisión", "Vigencia", "Estatus", "Subtotal", "IVA", "Total"};
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Object dto : cotizacionesDtos) {
                Row row = sheet.createRow(rowIdx++);

                if (dto instanceof CotizacionServiciosDTO c) {
                    row.createCell(0).setCellValue(c.getFolio());
                    row.createCell(1).setCellValue("Servicios");
                    row.createCell(2).setCellValue(c.getCliente().getNombreEmpresa());
                    row.createCell(3).setCellValue(c.getFechaEmision().format(formatter));
                    row.createCell(4).setCellValue(c.getVigencia());
                    row.createCell(5).setCellValue(c.getEstatus().name());

                    // Calcular subtotal e IVA
                    BigDecimal subtotal = calcularSubtotal(c.getTotal(), c.getPorcentajeIva(), c.isAplicarIva());
                    BigDecimal iva = c.isAplicarIva() ? c.getTotal().subtract(subtotal) : BigDecimal.ZERO;

                    row.createCell(6).setCellValue(subtotal.doubleValue());
                    row.createCell(7).setCellValue(iva.doubleValue());
                    row.createCell(8).setCellValue(c.getTotal().doubleValue());

                } else if (dto instanceof CotizacionProductosDTO c) {
                    row.createCell(0).setCellValue(c.getFolio());
                    row.createCell(1).setCellValue("Productos");
                    row.createCell(2).setCellValue(c.getCliente().getNombreEmpresa());
                    row.createCell(3).setCellValue(c.getFechaEmision().format(formatter));
                    row.createCell(4).setCellValue(c.getVigencia());
                    row.createCell(5).setCellValue(c.getEstatus().name());

                    // Calcular subtotal e IVA
                    BigDecimal subtotal = calcularSubtotal(c.getTotal(), c.getPorcentajeIva(), c.isAplicarIva());
                    BigDecimal iva = c.isAplicarIva() ? c.getTotal().subtract(subtotal) : BigDecimal.ZERO;

                    row.createCell(6).setCellValue(subtotal.doubleValue());
                    row.createCell(7).setCellValue(iva.doubleValue());
                    row.createCell(8).setCellValue(c.getTotal().doubleValue());
                }
            }

            // Autoajustar el tamaño de las columnas
            for(int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Fallo al generar el archivo Excel: " + e.getMessage());
        }
    }

    private BigDecimal calcularSubtotal(BigDecimal total, BigDecimal porcentajeIva, boolean aplicarIva) {
        if (total == null) return BigDecimal.ZERO;
        if (aplicarIva && porcentajeIva != null && porcentajeIva.compareTo(BigDecimal.ZERO) > 0) {
            return total.divide(BigDecimal.ONE.add(porcentajeIva), 2, RoundingMode.HALF_UP);
        }
        return total;
    }
}