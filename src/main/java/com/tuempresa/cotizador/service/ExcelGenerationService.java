package com.tuempresa.cotizador.service;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExcelGenerationService {

    ByteArrayInputStream generarExcelCotizaciones(List<Object> cotizacionesDtos);
}