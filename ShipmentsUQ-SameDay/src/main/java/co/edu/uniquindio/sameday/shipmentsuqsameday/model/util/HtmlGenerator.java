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
     * Genera un comprobante de pago en HTML y lo guarda en la carpeta de descargas
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
        
        // Guardar el HTML en la carpeta de descargas
        String fileName = "comprobante_" + payment.getId().toString() + ".html";
        return saveToDownloads(html, fileName);
    }
    
    /**
     * Genera un reporte de pagos en HTML y lo guarda en la carpeta de descargas
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
        
        // Guardar el HTML en la carpeta de descargas
        String fileName = "reporte_pagos_" + user.getId().toString() + ".html";
        return saveToDownloads(html, fileName);
    }
    
    /**
     * Carga una plantilla HTML desde los recursos
     * 
     * @param templatePath Ruta relativa al recurso
     * @return Contenido de la plantilla como String
     * @throws IOException Si hay un error al cargar la plantilla
     */
    private static String loadTemplate(String templatePath) throws IOException {
        try (InputStream is = App.class.getResourceAsStream(templatePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
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
    private static String saveToDownloads(String html, String fileName) throws IOException {
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
        
        return outputFile.getAbsolutePath();
    }
}