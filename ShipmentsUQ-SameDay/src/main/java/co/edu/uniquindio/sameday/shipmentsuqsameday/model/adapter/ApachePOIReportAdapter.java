package co.edu.uniquindio.sameday.shipmentsuqsameday.model.adapter;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IReport;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adaptador que integra la librería Apache POI con la interfaz IReport del sistema
 * 
 * Patrón de Diseño: ADAPTER
 * Problema que resuelve: Apache POI tiene una API compleja para manejar archivos Excel/CSV.
 * Este adaptador la convierte a la interfaz simple IReport que usa el sistema.
 * 
 * Beneficio: Permite cambiar la librería de Excel sin afectar el resto del código.
 * También facilita exportar a CSV de manera consistente con PDF.
 */
public class ApachePOIReportAdapter implements IReport {
    
    private String reportTitle;
    private List<String[]> reportData; // Cada array es una fila de datos
    private String[] headers;
    
    public ApachePOIReportAdapter(String reportTitle, String[] headers, List<String[]> reportData) {
        this.reportTitle = reportTitle;
        this.headers = headers;
        this.reportData = reportData;
    }
    
    /**
     * Genera un reporte en formato PDF (usando POI para crear Excel primero)
     * En una implementación completa, se convertiría el Excel a PDF
     */
    @Override
    public File generatePDF(LocalDateTime startDate, LocalDateTime endDate) {
        // Apache POI es principalmente para Excel, para PDF use PDFBoxReportAdapter
        System.out.println("[ADAPTER] Apache POI no genera PDF directamente, use PDFBoxReportAdapter");
        return null;
    }
    
    /**
     * Genera un reporte en formato CSV usando Apache POI
     * Adapta la compleja API de POI a nuestra interfaz simple
     */
    @Override
    public File generateCSV(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // Crear workbook y sheet usando Apache POI
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(reportTitle);
            
            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // Título del reporte
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportTitle);
            titleCell.setCellStyle(headerStyle);
            
            // Periodo
            Row periodRow = sheet.createRow(rowNum++);
            Cell periodCell = periodRow.createCell(0);
            periodCell.setCellValue("Periodo: " + formatDate(startDate) + " - " + formatDate(endDate));
            periodCell.setCellStyle(dataStyle);
            
            rowNum++; // Línea en blanco
            
            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            if (reportData != null) {
                for (String[] rowData : reportData) {
                    Row row = sheet.createRow(rowNum++);
                    for (int i = 0; i < rowData.length && i < headers.length; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(rowData[i]);
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Guardar archivo Excel (que puede abrirse como CSV)
            String fileName = generateFileName("xlsx");
            File file = new File(getDownloadsPath(), fileName);
            
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
            
            workbook.close();
            
            System.out.println("[ADAPTER] Excel/CSV generado exitosamente usando Apache POI: " + file.getAbsolutePath());
            return file;
            
        } catch (IOException e) {
            System.err.println("[ADAPTER] Error al generar CSV con Apache POI: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Crea estilo para headers
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Crea estilo para datos
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Formatea una fecha para el reporte
     */
    private String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    /**
     * Genera un nombre de archivo único
     */
    private String generateFileName(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s.%s", 
                reportTitle.replaceAll("\\s+", "_"), 
                timestamp, 
                extension);
    }
    
    /**
     * Obtiene la ruta de la carpeta de descargas del usuario
     */
    private String getDownloadsPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "Downloads").toString();
    }
}
