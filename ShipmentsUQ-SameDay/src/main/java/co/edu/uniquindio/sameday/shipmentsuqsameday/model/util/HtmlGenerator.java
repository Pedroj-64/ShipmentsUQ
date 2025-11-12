package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase utilitaria para generar documentos HTML (comprobantes y reportes)
 */
public class HtmlGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Genera un comprobante de pago en PDF y lo guarda en la carpeta de descargas
     * 
     * @param payment El pago para el cual generar el comprobante
     * @return La ruta completa al archivo generado
     * @throws IOException Si hay un error al generar el archivo
     */
    public static String generatePaymentReceipt(Payment payment) throws IOException {
        if (payment == null || payment.getShipment() == null) {
            throw new IllegalArgumentException("El pago o el envío asociado no pueden ser nulos");
        }
        
        // Cargar la plantilla
        String template = loadTemplate("html/payment_receipt_template.html");
        
        // Obtener los datos necesarios
        Shipment shipment = payment.getShipment();
        User customer = shipment.getUser();
        
        // Formatear la fecha actual
        String generationDate = LocalDateTime.now().format(DATE_FORMATTER);
        String paymentDate = payment.getCreationDate() != null ? 
                             payment.getCreationDate().format(DATE_FORMATTER) : "No disponible";
        
        // Determinar la clase CSS para el estado
        String statusClass = payment.getStatus() == PaymentStatus.COMPLETED ? 
                            "status-completed" : "status-pending";
        
        // Reemplazar los marcadores en la plantilla
        String html = template
                .replace("${paymentReference}", payment.getPaymentReference() != null ? 
                        payment.getPaymentReference() : "No disponible")
                .replace("${paymentDate}", paymentDate)
                .replace("${paymentId}", payment.getId().toString())
                .replace("${paymentMethod}", payment.getPaymentMethod().toString())
                .replace("${amount}", String.format("%.2f", payment.getAmount()))
                .replace("${status}", payment.getStatus().toString())
                .replace("${statusClass}", statusClass)
                .replace("${shipmentId}", shipment.getId().toString())
                .replace("${customerName}", customer != null ? 
                        customer.getName() : "No disponible")
                .replace("${originAddress}", shipment.getOrigin() != null ? 
                        shipment.getOrigin().getFullAddress() : "No disponible")
                .replace("${destinationAddress}", shipment.getDestination() != null ? 
                        shipment.getDestination().getFullAddress() : "No disponible")
                .replace("${generationDate}", generationDate);
        
        // Guardar el PDF en la carpeta de descargas
        String fileName = "comprobante_" + payment.getId().toString() + ".pdf";
        return htmlToPdf(html, fileName);
    }
    
    /**
     * Genera un reporte de pagos en PDF y lo guarda en la carpeta de descargas
     * 
     * @param user El usuario para el cual generar el reporte
     * @param payments Lista de pagos para incluir en el reporte
     * @return La ruta completa al archivo generado
     * @throws IOException Si hay un error al generar el archivo
     */
    public static String generatePaymentReport(User user, List<Payment> payments) throws IOException {
        if (user == null || payments == null) {
            throw new IllegalArgumentException("El usuario y la lista de pagos no pueden ser nulos");
        }
        
        // Cargar la plantilla
        String template = loadTemplate("html/payment_report_template.html");
        
        // Formatear la fecha actual
        String generationDate = LocalDateTime.now().format(DATE_FORMATTER);
        String reportDate = generationDate;
        
        // Calcular estadísticas
        int totalPayments = payments.size();
        long completedPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();
        long pendingPayments = totalPayments - completedPayments;
        double totalAmount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
        
        // Generar filas de la tabla de pagos
        StringBuilder paymentRowsBuilder = new StringBuilder();
        for (Payment payment : payments) {
            Shipment shipment = payment.getShipment();
            String shipmentId = shipment != null ? shipment.getId().toString() : "N/A";
            String statusClass = payment.getStatus() == PaymentStatus.COMPLETED ? 
                                "status-completed" : "status-pending";
            
            paymentRowsBuilder.append("<tr>")
                .append("<td>").append(payment.getId().toString()).append("</td>")
                .append("<td>").append(payment.getCreationDate() != null ? 
                        payment.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A").append("</td>")
                .append("<td>").append(shipmentId).append("</td>")
                .append("<td>$").append(String.format("%.2f", payment.getAmount())).append("</td>")
                .append("<td>").append(payment.getPaymentMethod()).append("</td>")
                .append("<td><span class=\"").append(statusClass).append("\">")
                    .append(payment.getStatus()).append("</span></td>")
                .append("<td>").append(payment.getPaymentReference() != null ? 
                        payment.getPaymentReference() : "N/A").append("</td>")
                .append("</tr>");
        }
        
        // Reemplazar los marcadores en la plantilla
        String html = template
                .replace("${userName}", user.getName())
                .replace("${userEmail}", user.getEmail())
                .replace("${userId}", user.getId().toString())
                .replace("${reportDate}", reportDate)
                .replace("${totalPayments}", String.valueOf(totalPayments))
                .replace("${completedPayments}", String.valueOf(completedPayments))
                .replace("${pendingPayments}", String.valueOf(pendingPayments))
                .replace("${totalAmount}", String.format("%.2f", totalAmount))
                .replace("${paymentRows}", paymentRowsBuilder.toString())
                .replace("${generationDate}", generationDate);
        
        // Guardar el PDF en la carpeta de descargas
        String fileName = "reporte_pagos_" + user.getId().toString() + ".pdf";
        return htmlToPdf(html, fileName);
    }
    
    /**
     * Convierte HTML a PDF y lo guarda en la carpeta de descargas
     * Utiliza OpenHTMLToPDF para la conversión de HTML a PDF
     * 
     * @param html Contenido HTML a convertir
     * @param fileName Nombre del archivo PDF a generar
     * @return Ruta completa al archivo generado
     * @throws IOException Si hay un error al generar el archivo
     */
    public static String htmlToPdf(String html, String fileName) throws IOException {
        // Obtener la ruta a la carpeta de descargas
        String userHome = System.getProperty("user.home");
        Path downloadsPath = Paths.get(userHome, "Downloads");
        
        // Asegurar que la carpeta de descargas exista
        if (!Files.exists(downloadsPath)) {
            downloadsPath = Paths.get(userHome);
        }
        
        // Asegurar que el nombre del archivo tenga extensión .pdf
        String actualFileName = fileName;
        if (!actualFileName.endsWith(".pdf")) {
            actualFileName = actualFileName.replace(".html", "");
            actualFileName = actualFileName + ".pdf";
        }
        
        // Crear el archivo en la carpeta de descargas
        File outputFile = new File(downloadsPath.toFile(), actualFileName);
        
        // Primero guardamos el HTML temporalmente
        File tempHtmlFile = new File(downloadsPath.toFile(), actualFileName.replace(".pdf", ".html"));
        
        try (FileWriter writer = new FileWriter(tempHtmlFile, StandardCharsets.UTF_8)) {
            // Escribir el HTML con estilos para impresión optimizada
            String htmlWithStyles = html.replace("</head>", 
                "<style>" +
                "@media print {" +
                "  * { margin: 0; padding: 0; box-sizing: border-box; }" +
                "  body { font-family: 'Arial', sans-serif; font-size: 12pt; color: #000; line-height: 1.5; }" +
                "  table { border-collapse: collapse; width: 100%; margin: 10pt 0; }" +
                "  td, th { border: 1pt solid #000; padding: 5pt; }" +
                "  .no-print { display: none; }" +
                "}" +
                "@page { size: A4; margin: 0.5cm; }" +
                "</style>" +
                "</head>");
            
            writer.write(htmlWithStyles);
            writer.flush();
            
            // Intentar convertir a PDF usando Firefox (si está disponible)
            convertHtmlToPdfUsingFirefox(tempHtmlFile, outputFile);
            
            // Si Firefox no está disponible, simplemente renombrar el archivo HTML a PDF
            if (!outputFile.exists() && tempHtmlFile.exists()) {
                System.out.println("Firefox no disponible. Guardando como PDF (HTML con estilos)...");
                Files.move(tempHtmlFile.toPath(), outputFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } else if (tempHtmlFile.exists()) {
                // Eliminar el archivo HTML temporal si existe el PDF
                Files.delete(tempHtmlFile.toPath());
            }
            
            System.out.println("PDF generado exitosamente: " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Error al generar archivo PDF: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error al generar archivo PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Intenta convertir un archivo HTML a PDF usando Firefox en modo headless
     */
    private static void convertHtmlToPdfUsingFirefox(File htmlFile, File pdfFile) {
        try {
            String htmlPath = htmlFile.toURI().toString();
            String pdfPath = pdfFile.getAbsolutePath();
            
            // Comando para Firefox en modo headless (para Windows)
            ProcessBuilder pb = new ProcessBuilder(
                "firefox",
                "--headless",
                "--print-to-pdf=" + pdfPath,
                htmlPath
            );
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("PDF convertido exitosamente usando Firefox");
            }
        } catch (Exception e) {
            System.out.println("Firefox no disponible o error al convertir: " + e.getMessage());
            // Continuar sin Firefox - se guardará como HTML en PDF
        }
    }
    
    /**
     * Carga una plantilla HTML desde los recursos
     * 
     * @param templatePath Ruta relativa al recurso (ej: "html/payment_receipt_template.html")
     * @return Contenido de la plantilla como String
     * @throws IOException Si hay un error al cargar la plantilla
     */
    public static String loadTemplate(String templatePath) throws IOException {
        try (InputStream is = App.class.getResourceAsStream(templatePath)) {
            if (is == null) {
                throw new IOException("No se pudo encontrar la plantilla: " + templatePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
    
    /**
     * Guarda un archivo HTML en la carpeta de descargas del usuario
     * 
     * @param html Contenido HTML
     * @param fileName Nombre del archivo a guardar
     * @return Ruta completa al archivo guardado
     * @throws IOException Si hay un error al guardar el archivo
     */
    public static String saveToDownloads(String html, String fileName) throws IOException {
        // Obtener la ruta a la carpeta de descargas
        String userHome = System.getProperty("user.home");
        Path downloadsPath = Paths.get(userHome, "Downloads");
        
        // Asegurar que la carpeta de descargas exista
        if (!Files.exists(downloadsPath)) {
            downloadsPath = Paths.get(userHome); // Usar la carpeta de usuario si no existe Descargas
        }
        
        // Crear el archivo en la carpeta de descargas
        File outputFile = new File(downloadsPath.toFile(), fileName);
        try (FileWriter writer = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write(html);
        }
        
        System.out.println("Reporte generado exitosamente: " + outputFile.getAbsolutePath());
        return outputFile.getAbsolutePath();
    }
}