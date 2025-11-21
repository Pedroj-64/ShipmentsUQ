package co.edu.uniquindio.sameday.shipmentsuqsameday.model.adapter;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IReport;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Adaptador que integra la librería Apache PDFBox con la interfaz IReport del sistema
 * 
 * Patrón de Diseño: ADAPTER
 * Problema que resuelve: La librería PDFBox tiene su propia API compleja para generar PDFs.
 * Este adaptador la convierte a la interfaz simple IReport que usa el sistema.
 * 
 * Beneficio: Permite cambiar la librería de PDFs sin afectar el resto del código.
 */
public class PDFBoxReportAdapter implements IReport {
    
    private String reportTitle;
    private Object reportData;
    
    public PDFBoxReportAdapter(String reportTitle, Object reportData) {
        this.reportTitle = reportTitle;
        this.reportData = reportData;
    }
    
    /**
     * Genera un reporte en formato PDF usando Apache PDFBox
     * Adapta la compleja API de PDFBox a nuestra interfaz simple
     */
    @Override
    public File generatePDF(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // Crear documento PDF usando PDFBox
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            
            // Crear contenido
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Configurar fuente
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(reportTitle);
            contentStream.endText();
            
            // Agregar fechas
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 720);
            contentStream.showText("Periodo: " + formatDate(startDate) + " - " + formatDate(endDate));
            contentStream.endText();
            
            // Agregar contenido del reporte
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 680);
            contentStream.showText("Datos del reporte:");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 650);
            contentStream.showText(reportData != null ? reportData.toString() : "Sin datos");
            contentStream.endText();
            
            contentStream.close();
            
            // Guardar archivo
            String fileName = generateFileName("pdf");
            File file = new File(getDownloadsPath(), fileName);
            document.save(file);
            document.close();
            
            System.out.println("[ADAPTER] PDF generado exitosamente usando PDFBox: " + file.getAbsolutePath());
            return file;
            
        } catch (IOException e) {
            System.err.println("[ADAPTER] Error al generar PDF con PDFBox: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Por ahora, este método redirige a CSV nativo ya que PDFBox no maneja CSV
     * En una implementación real, se usaría otro adaptador para CSV
     */
    @Override
    public File generateCSV(LocalDateTime startDate, LocalDateTime endDate) {
        // PDFBox no genera CSV, delegamos a ApachePOIReportAdapter
        System.out.println("[ADAPTER] PDFBox no soporta CSV, use ApachePOIReportAdapter");
        return null;
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
