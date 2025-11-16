package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.DelivererService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;

import java.time.LocalDateTime;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Controlador para las métricas y reportes del sistema
 * Se encarga de la lógica de negocio relacionada con el cálculo
 * y generación de métricas y reportes administrativos
 */
public class AdminMetricsController {
    
    // Servicios para acceso a datos
    private ShipmentService shipmentService;
    private DelivererService delivererService;
    
    /**
     * Constructor del controlador
     */
    public AdminMetricsController() {
        this.shipmentService = ShipmentService.getInstance();
        this.delivererService = DelivererService.getInstance();
    }
    
    /**
     * Obtiene el número total de envíos en el sistema
     * @return cantidad total de envíos
     */
    public int getTotalShipments() {
        try {
            List<Shipment> allShipments = shipmentService.findAll();
            return allShipments != null ? allShipments.size() : 0;
        } catch (Exception e) {
            System.err.println("Error al obtener total de envíos: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Obtiene el número de envíos del mes actual
     * @return cantidad de envíos del mes actual
     */
    public int getMonthlyShipments() {
        try {
            List<Shipment> allShipments = shipmentService.findAll();
            if (allShipments == null) return 0;
            
            LocalDate now = LocalDate.now();
            return (int) allShipments.stream()
                    .filter(shipment -> shipment.getCreationDate() != null)
                    .filter(shipment -> {
                        LocalDate shipmentDate = shipment.getCreationDate().toLocalDate();
                        return shipmentDate.getYear() == now.getYear() && 
                               shipmentDate.getMonth() == now.getMonth();
                    })
                    .count();
        } catch (Exception e) {
            System.err.println("Error al obtener envíos del mes: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Obtiene el número de envíos del día actual
     * @return cantidad de envíos del día actual
     */
    public int getDailyShipments() {
        try {
            List<Shipment> allShipments = shipmentService.findAll();
            if (allShipments == null) return 0;
            
            LocalDate today = LocalDate.now();
            return (int) allShipments.stream()
                    .filter(shipment -> shipment.getCreationDate() != null)
                    .filter(shipment -> shipment.getCreationDate().toLocalDate().equals(today))
                    .count();
        } catch (Exception e) {
            System.err.println("Error al obtener envíos del día: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Obtiene los envíos agrupados por estado
     * @return mapa con estado como clave y cantidad como valor
     */
    public Map<ShipmentStatus, Integer> getShipmentsByStatus() {
        Map<ShipmentStatus, Integer> statusCount = new HashMap<>();
        
        try {
            List<Shipment> allShipments = shipmentService.findAll();
            if (allShipments == null) return statusCount;
            
            for (ShipmentStatus status : ShipmentStatus.values()) {
                statusCount.put(status, 0);
            }
            
            for (Shipment shipment : allShipments) {
                ShipmentStatus status = shipment.getStatus();
                statusCount.put(status, statusCount.get(status) + 1);
            }
            
        } catch (Exception e) {
            System.err.println("Error al obtener envíos por estado: " + e.getMessage());
        }
        
        return statusCount;
    }
    
    /**
     * Calcula el tiempo promedio de entrega en minutos
     * @return tiempo promedio de entrega
     */
    public double getAverageDeliveryTime() {
        try {
            List<Shipment> deliveredShipments = shipmentService.findAll()
                    .stream()
                    .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                    .filter(s -> s.getCreationDate() != null && s.getDeliveryDate() != null)
                    .collect(Collectors.toList());
            
            if (deliveredShipments.isEmpty()) return 0.0;
            
            double totalMinutes = deliveredShipments.stream()
                    .mapToLong(s -> java.time.Duration.between(
                            s.getCreationDate(), s.getDeliveryDate()).toMinutes())
                    .average()
                    .orElse(0.0);
            
            return Math.round(totalMinutes * 100.0) / 100.0;
            
        } catch (Exception e) {
            System.err.println("Error al calcular tiempo promedio de entrega: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Calcula la tasa de entrega exitosa como porcentaje
     * @return porcentaje de entregas exitosas
     */
    public double getSuccessRate() {
        try {
            List<Shipment> allShipments = shipmentService.findAll();
            if (allShipments == null || allShipments.isEmpty()) return 0.0;
            
            long deliveredCount = allShipments.stream()
                    .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                    .count();
            
            double rate = (deliveredCount * 100.0) / allShipments.size();
            return Math.round(rate * 100.0) / 100.0;
            
        } catch (Exception e) {
            System.err.println("Error al calcular tasa de éxito: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Calcula la eficiencia promedio de los repartidores
     * @return porcentaje de eficiencia promedio
     */
    public double getCourierEfficiency() {
        try {
            List<Deliverer> allDeliverers = delivererService.findAll();
            if (allDeliverers == null || allDeliverers.isEmpty()) return 0.0;
            
            double totalEfficiency = 0.0;
            int activeDeliverers = 0;
            
            for (Deliverer deliverer : allDeliverers) {
                List<Shipment> delivererShipments = shipmentService.findAll()
                        .stream()
                        .filter(s -> deliverer.equals(s.getDeliverer()))
                        .collect(Collectors.toList());
                
                if (!delivererShipments.isEmpty()) {
                    long delivered = delivererShipments.stream()
                            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                            .count();
                    
                    double efficiency = (delivered * 100.0) / delivererShipments.size();
                    totalEfficiency += efficiency;
                    activeDeliverers++;
                }
            }
            
            if (activeDeliverers == 0) return 0.0;
            
            double average = totalEfficiency / activeDeliverers;
            return Math.round(average * 100.0) / 100.0;
            
        } catch (Exception e) {
            System.err.println("Error al calcular eficiencia de repartidores: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Obtiene datos para el gráfico de envíos por mes
     * @return mapa con mes como clave y cantidad como valor
     */
    public Map<String, Integer> getMonthlyShipmentsData() {
        Map<String, Integer> monthlyData = new HashMap<>();
        
        try {
            List<Shipment> allShipments = shipmentService.findAll();
            if (allShipments == null) return monthlyData;
            
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
            
            Map<String, Integer> shipmentsByMonth = allShipments.stream()
                    .filter(s -> s.getCreationDate() != null)
                    .collect(Collectors.groupingBy(
                            s -> s.getCreationDate().format(monthFormatter),
                            Collectors.summingInt(s -> 1)
                    ));
            
            monthlyData.putAll(shipmentsByMonth);
            
        } catch (Exception e) {
            System.err.println("Error al obtener datos mensuales: " + e.getMessage());
        }
        
        return monthlyData;
    }
    
    /**
     * Genera un reporte general del sistema y lo guarda como HTML
     * @return ruta del archivo generado o null si hay error
     */
    public String generateGeneralReport() {
        try {
            // Crear datos del reporte
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("totalShipments", getTotalShipments());
            reportData.put("monthlyShipments", getMonthlyShipments());
            reportData.put("dailyShipments", getDailyShipments());
            reportData.put("shipmentsByStatus", getShipmentsByStatus());
            reportData.put("averageDeliveryTime", getAverageDeliveryTime());
            reportData.put("successRate", getSuccessRate());
            reportData.put("courierEfficiency", getCourierEfficiency());
            reportData.put("monthlyData", getMonthlyShipmentsData());
            
            return generateReportHtml("general", reportData);
            
        } catch (Exception e) {
            System.err.println("Error al generar reporte general: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera un reporte mensual y lo guarda como HTML
     * @return ruta del archivo generado o null si hay error
     */
    public String generateMonthlyReport() {
        try {
            Map<String, Object> reportData = new HashMap<>();
            
            // Formatear mes en español
            LocalDate now = LocalDate.now();
            String monthName = now.format(DateTimeFormatter.ofPattern("MMMM yyyy", 
                    java.util.Locale.forLanguageTag("es-ES")));
            monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
            
            reportData.put("month", monthName);
            reportData.put("monthlyShipments", getMonthlyShipments());
            reportData.put("shipmentsByStatus", getShipmentsByStatus());
            reportData.put("averageDeliveryTime", getAverageDeliveryTime());
            reportData.put("successRate", getSuccessRate());
            
            return generateReportHtml("monthly", reportData);
            
        } catch (Exception e) {
            System.err.println("Error al generar reporte mensual: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera un reporte diario y lo guarda como HTML
     * @return ruta del archivo generado o null si hay error
     */
    public String generateDailyReport() {
        try {
            Map<String, Object> reportData = new HashMap<>();
            
            // Formatear fecha en español
            LocalDate today = LocalDate.now();
            String formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", 
                    java.util.Locale.forLanguageTag("es-ES")));
            formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            
            reportData.put("date", formattedDate);
            reportData.put("dailyShipments", getDailyShipments());
            
            // Obtener envíos del día con detalles
            List<Shipment> todayShipments = new ArrayList<>();
            try {
                List<Shipment> allShipments = shipmentService.findAll();
                if (allShipments != null) {
                    todayShipments = allShipments.stream()
                            .filter(s -> s.getCreationDate() != null)
                            .filter(s -> s.getCreationDate().toLocalDate().equals(today))
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                System.err.println("Error al obtener envíos del día: " + e.getMessage());
            }
            
            reportData.put("todayShipments", todayShipments);
            
            return generateReportHtml("daily", reportData);
            
        } catch (Exception e) {
            System.err.println("Error al generar reporte diario: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera un archivo HTML para el reporte especificado
     * @param reportType tipo de reporte (general, monthly, daily)
     * @param data datos del reporte
     * @return ruta del archivo generado
     */
    private String generateReportHtml(String reportType, Map<String, Object> data) {
        try {
            String template = loadReportTemplate(reportType);
            String html = processTemplate(template, data);
            
            String fileName = String.format("reporte_%s_%s.html", 
                    reportType, 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            return saveToDownloads(html, fileName);
            
        } catch (Exception e) {
            System.err.println("Error al generar HTML del reporte: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Carga la plantilla HTML para el tipo de reporte
     * @param reportType tipo de reporte
     * @return contenido de la plantilla
     */
    private String loadReportTemplate(String reportType) {
        try {
            String templatePath = "html/" + reportType + "_report_template.html";
            InputStream is = App.class.getResourceAsStream(templatePath);
            
            if (is == null) {
                System.err.println("No se pudo encontrar la plantilla: " + templatePath);
                System.err.println("Intentando con ruta completa...");
                String fullPath = "/co/edu/uniquindio/sameday/shipmentsuqsameday/html/" + reportType + "_report_template.html";
                is = getClass().getResourceAsStream(fullPath);
            }
            
            if (is == null) {
                System.err.println("No se pudo cargar la plantilla, usando plantilla por defecto");
                return getDefaultTemplate(reportType);
            }
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar plantilla: " + e.getMessage());
            e.printStackTrace();
            return getDefaultTemplate(reportType);
        }
    }
    
    /**
     * Obtiene una plantilla por defecto si no se puede cargar la específica
     * @param reportType tipo de reporte
     * @return HTML de plantilla básica
     */
    private String getDefaultTemplate(String reportType) {
        return "<!DOCTYPE html><html><head><title>Reporte " + reportType + "</title>" +
               "<style>body{font-family:Arial,sans-serif;margin:20px;}</style></head>" +
               "<body><h1>Reporte " + reportType.toUpperCase() + "</h1>" +
               "<p>Generado el: ${generationDate}</p>${content}</body></html>";
    }
    
    /**
     * Procesa la plantilla reemplazando los marcadores con datos
     * @param template plantilla HTML
     * @param data datos para reemplazar
     * @return HTML procesado
     */
    private String processTemplate(String template, Map<String, Object> data) {
        String html = template;
        
        // Reemplazar fecha de generación
        html = html.replace("${generationDate}", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        // Procesar según el tipo de plantilla
        if (template.contains("${totalShipments}")) {
            // Plantilla general
            html = processGeneralTemplate(html, data);
        } else if (template.contains("${month}")) {
            // Plantilla mensual
            html = processMonthlyTemplate(html, data);
        } else if (template.contains("${date}")) {
            // Plantilla diaria
            html = processDailyTemplate(html, data);
        } else {
            // Plantilla por defecto
            html = processDefaultTemplate(html, data);
        }
        
        return html;
    }
    
    /**
     * Procesa la plantilla del reporte general
     */
    @SuppressWarnings("unchecked")
    private String processGeneralTemplate(String template, Map<String, Object> data) {
        String html = template;
        
        // Reemplazar métricas básicas
        html = html.replace("${totalShipments}", String.valueOf(data.get("totalShipments")));
        html = html.replace("${monthlyShipments}", String.valueOf(data.get("monthlyShipments")));
        html = html.replace("${dailyShipments}", String.valueOf(data.get("dailyShipments")));
        html = html.replace("${averageDeliveryTime}", formatTime((Double) data.get("averageDeliveryTime")));
        html = html.replace("${successRate}", String.format("%.2f", data.get("successRate")));
        html = html.replace("${courierEfficiency}", String.format("%.2f", data.get("courierEfficiency")));
        
        // Generar tabla de estados
        Map<ShipmentStatus, Integer> statusData = (Map<ShipmentStatus, Integer>) data.get("shipmentsByStatus");
        html = html.replace("${shipmentStatusRows}", generateStatusTableRows(statusData));
        
        // Generar datos mensuales
        Map<String, Integer> monthlyData = (Map<String, Integer>) data.get("monthlyData");
        html = html.replace("${monthlyDataRows}", generateMonthlyDataRows(monthlyData));
        
        return html;
    }
    
    /**
     * Procesa la plantilla del reporte mensual
     */
    @SuppressWarnings("unchecked")
    private String processMonthlyTemplate(String template, Map<String, Object> data) {
        String html = template;
        
        html = html.replace("${month}", (String) data.get("month"));
        html = html.replace("${monthlyShipments}", String.valueOf(data.get("monthlyShipments")));
        html = html.replace("${averageDeliveryTime}", formatTime((Double) data.get("averageDeliveryTime")));
        html = html.replace("${successRate}", String.format("%.2f", data.get("successRate")));
        
        // Generar distribución de estados
        Map<ShipmentStatus, Integer> statusData = (Map<ShipmentStatus, Integer>) data.get("shipmentsByStatus");
        html = html.replace("${statusDistributionRows}", generateStatusDistributionRows(statusData));
        
        // Generar insights
        html = html.replace("${performanceInsight}", generatePerformanceInsight((Double) data.get("successRate")));
        html = html.replace("${trendAnalysis}", generateTrendAnalysis((Integer) data.get("monthlyShipments")));
        html = html.replace("${recommendation}", generateRecommendation((Double) data.get("successRate")));
        
        return html;
    }
    
    /**
     * Procesa la plantilla del reporte diario
     */
    @SuppressWarnings("unchecked")
    private String processDailyTemplate(String template, Map<String, Object> data) {
        String html = template;
        
        html = html.replace("${date}", (String) data.get("date"));
        html = html.replace("${dailyShipments}", String.valueOf(data.get("dailyShipments")));
        
        int dailyShipments = (Integer) data.get("dailyShipments");
        html = html.replace("${dailyMessage}", generateDailyMessage(dailyShipments));
        html = html.replace("${pendingShipments}", "0"); // Por implementar
        html = html.replace("${deliveredToday}", "0"); // Por implementar
        
        // Generar contenido de la tabla de envíos
        List<Shipment> todayShipments = (List<Shipment>) data.get("todayShipments");
        html = html.replace("${shipmentsTableContent}", generateShipmentsTableContent(todayShipments));
        html = html.replace("${timeSlotAnalysis}", generateTimeSlotAnalysis(todayShipments));
        
        return html;
    }
    
    /**
     * Procesa plantilla por defecto
     */
    private String processDefaultTemplate(String template, Map<String, Object> data) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            content.append("<p><strong>").append(entry.getKey()).append(":</strong> ")
                   .append(entry.getValue().toString()).append("</p>");
        }
        return template.replace("${content}", content.toString());
    }
    
    /**
     * Formatea el tiempo de entrega para mostrar
     */
    private String formatTime(Double minutes) {
        if (minutes == null || minutes <= 0) return "N/A";
        
        if (minutes < 60) {
            return String.format("%.1f min", minutes);
        } else {
            double hours = minutes / 60;
            return String.format("%.1f hrs", hours);
        }
    }
    
    /**
     * Genera las filas de la tabla de estados para el reporte general
     */
    private String generateStatusTableRows(Map<ShipmentStatus, Integer> statusData) {
        StringBuilder rows = new StringBuilder();
        int total = statusData.values().stream().mapToInt(Integer::intValue).sum();
        
        for (Map.Entry<ShipmentStatus, Integer> entry : statusData.entrySet()) {
            if (entry.getValue() > 0) {
                double percentage = total > 0 ? (entry.getValue() * 100.0) / total : 0;
                String statusClass = getStatusClass(entry.getKey());
                String statusName = translateStatusToSpanish(entry.getKey());
                
                rows.append("<tr>")
                    .append("<td>").append(statusName).append("</td>")
                    .append("<td>").append(entry.getValue()).append("</td>")
                    .append("<td>").append(String.format("%.1f%%", percentage)).append("</td>")
                    .append("<td><span class=\"status-badge ").append(statusClass).append("\">")
                    .append(entry.getKey()).append("</span></td>")
                    .append("</tr>");
            }
        }
        
        return rows.toString();
    }
    
    /**
     * Genera las filas de datos mensuales
     */
    private String generateMonthlyDataRows(Map<String, Integer> monthlyData) {
        StringBuilder rows = new StringBuilder();
        
        for (Map.Entry<String, Integer> entry : monthlyData.entrySet()) {
            rows.append("<div class=\"monthly-item\">")
                .append("<span class=\"month\">").append(entry.getKey()).append("</span>")
                .append("<span class=\"count\">").append(entry.getValue()).append("</span>")
                .append("</div>");
        }
        
        return rows.toString();
    }
    
    /**
     * Genera las filas de distribución de estados para reporte mensual
     */
    private String generateStatusDistributionRows(Map<ShipmentStatus, Integer> statusData) {
        StringBuilder rows = new StringBuilder();
        
        for (Map.Entry<ShipmentStatus, Integer> entry : statusData.entrySet()) {
            if (entry.getValue() > 0) {
                String statusClass = getStatusClass(entry.getKey());
                String statusName = translateStatusToSpanish(entry.getKey());
                
                rows.append("<div class=\"status-item ").append(statusClass).append("\">")
                    .append("<div class=\"status-name\">")
                    .append("<div class=\"status-indicator\"></div>")
                    .append(statusName)
                    .append("</div>")
                    .append("<div class=\"status-count\">").append(entry.getValue()).append("</div>")
                    .append("</div>");
            }
        }
        
        return rows.toString();
    }
    
    /**
     * Genera mensaje diario basado en la cantidad de envíos
     */
    private String generateDailyMessage(int dailyShipments) {
        if (dailyShipments == 0) {
            return "No se registraron envíos en el día de hoy.";
        } else if (dailyShipments == 1) {
            return "Se registró 1 envío en el día de hoy.";
        } else if (dailyShipments < 5) {
            return "Día de actividad baja con " + dailyShipments + " envíos registrados.";
        } else if (dailyShipments < 15) {
            return "Día de actividad moderada con " + dailyShipments + " envíos registrados.";
        } else {
            return "Día de alta actividad con " + dailyShipments + " envíos registrados.";
        }
    }
    
    /**
     * Genera contenido de tabla de envíos para reporte diario
     */
    private String generateShipmentsTableContent(List<Shipment> shipments) {
        if (shipments == null || shipments.isEmpty()) {
            return "<div class=\"no-shipments\">" +
                   "<div class=\"icon\">📦</div>" +
                   "<h4>No hay envíos registrados</h4>" +
                   "<p>No se crearon envíos en este día.</p>" +
                   "</div>";
        }
        
        StringBuilder table = new StringBuilder();
        table.append("<table class=\"shipments-table\">")
             .append("<thead>")
             .append("<tr>")
             .append("<th>ID Envío</th>")
             .append("<th>Hora Creación</th>")
             .append("<th>Origen</th>")
             .append("<th>Destino</th>")
             .append("<th>Estado</th>")
             .append("<th>Cliente</th>")
             .append("</tr>")
             .append("</thead>")
             .append("<tbody>");
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Shipment shipment : shipments) {
            String statusClass = getStatusClass(shipment.getStatus());
            String creationTime = shipment.getCreationDate() != null ? 
                                 shipment.getCreationDate().format(timeFormatter) : "N/A";
            String origin = shipment.getOrigin() != null ? 
                           shipment.getOrigin().getCity() : "N/A";
            String destination = shipment.getDestination() != null ? 
                                shipment.getDestination().getCity() : "N/A";
            String customerName = shipment.getUser() != null ? 
                                 shipment.getUser().getName() : "N/A";
            
            table.append("<tr>")
                 .append("<td>").append(shipment.getId()).append("</td>")
                 .append("<td>").append(creationTime).append("</td>")
                 .append("<td>").append(origin).append("</td>")
                 .append("<td>").append(destination).append("</td>")
                 .append("<td><span class=\"status-badge ").append(statusClass).append("\">")
                 .append(translateStatusToSpanish(shipment.getStatus())).append("</span></td>")
                 .append("<td>").append(customerName).append("</td>")
                 .append("</tr>");
        }
        
        table.append("</tbody></table>");
        return table.toString();
    }
    
    /**
     * Genera análisis por franjas horarias
     */
    private String generateTimeSlotAnalysis(List<Shipment> shipments) {
        if (shipments == null || shipments.isEmpty()) {
            return "<div class=\"time-slot\"><div class=\"slot-time\">Sin datos</div><div class=\"slot-count\">0</div></div>";
        }
        
        // Contar envíos por franja horaria
        Map<String, Integer> timeSlots = new HashMap<>();
        timeSlots.put("06:00-12:00", 0);
        timeSlots.put("12:00-18:00", 0);
        timeSlots.put("18:00-00:00", 0);
        timeSlots.put("00:00-06:00", 0);
        
        for (Shipment shipment : shipments) {
            if (shipment.getCreationDate() != null) {
                int hour = shipment.getCreationDate().getHour();
                
                if (hour >= 6 && hour < 12) {
                    timeSlots.put("06:00-12:00", timeSlots.get("06:00-12:00") + 1);
                } else if (hour >= 12 && hour < 18) {
                    timeSlots.put("12:00-18:00", timeSlots.get("12:00-18:00") + 1);
                } else if (hour >= 18) {
                    timeSlots.put("18:00-00:00", timeSlots.get("18:00-00:00") + 1);
                } else {
                    timeSlots.put("00:00-06:00", timeSlots.get("00:00-06:00") + 1);
                }
            }
        }
        
        StringBuilder slots = new StringBuilder();
        for (Map.Entry<String, Integer> entry : timeSlots.entrySet()) {
            slots.append("<div class=\"time-slot\">")
                 .append("<div class=\"slot-time\">").append(entry.getKey()).append("</div>")
                 .append("<div class=\"slot-count\">").append(entry.getValue()).append("</div>")
                 .append("</div>");
        }
        
        return slots.toString();
    }
    
    /**
     * Genera insight de rendimiento para reporte mensual
     */
    private String generatePerformanceInsight(Double successRate) {
        if (successRate >= 95) {
            return "Excelente rendimiento del sistema con una tasa de éxito superior al 95%.";
        } else if (successRate >= 85) {
            return "Buen rendimiento del sistema con una tasa de éxito satisfactoria.";
        } else if (successRate >= 70) {
            return "Rendimiento moderado del sistema. Se recomienda revisar procesos.";
        } else {
            return "Rendimiento por debajo del estándar. Requiere atención inmediata.";
        }
    }
    
    /**
     * Genera análisis de tendencia
     */
    private String generateTrendAnalysis(Integer monthlyShipments) {
        if (monthlyShipments > 100) {
            return "alta";
        } else if (monthlyShipments > 50) {
            return "moderada";
        } else {
            return "baja";
        }
    }
    
    /**
     * Genera recomendación basada en métricas
     */
    private String generateRecommendation(Double successRate) {
        if (successRate >= 95) {
            return "Mantener los procesos actuales y buscar oportunidades de expansión.";
        } else if (successRate >= 85) {
            return "Optimizar rutas de entrega para mejorar la eficiencia.";
        } else {
            return "Revisar y mejorar los procesos de gestión de envíos.";
        }
    }
    
    /**
     * Obtiene la clase CSS para un estado de envío
     */
    private String getStatusClass(ShipmentStatus status) {
        switch (status) {
            case PENDING: return "status-pending";
            case ASSIGNED: return "status-assigned";
            case IN_TRANSIT: return "status-in-transit";
            case DELIVERED: return "status-delivered";
            case CANCELLED: return "status-cancelled";
            case INCIDENT: return "status-incident";
            case PENDING_REASSIGNMENT: return "status-pending";
            default: return "status-pending";
        }
    }
    
    /**
     * Traduce estado de envío al español
     */
    private String translateStatusToSpanish(ShipmentStatus status) {
        switch (status) {
            case PENDING: return "Pendiente";
            case ASSIGNED: return "Asignado";
            case IN_TRANSIT: return "En Tránsito";
            case DELIVERED: return "Entregado";
            case CANCELLED: return "Cancelado";
            case INCIDENT: return "Con Incidente";
            case PENDING_REASSIGNMENT: return "Pendiente de Reasignación";
            default: return status.toString();
        }
    }
    
    /**
     * Guarda el HTML en la carpeta de descargas
     * @param html contenido HTML
     * @param fileName nombre del archivo
     * @return ruta completa del archivo
     */
    private String saveToDownloads(String html, String fileName) {
        try {
            String userHome = System.getProperty("user.home");
            java.nio.file.Path downloadsPath = java.nio.file.Paths.get(userHome, "Downloads");
            
            if (!java.nio.file.Files.exists(downloadsPath)) {
                downloadsPath = java.nio.file.Paths.get(userHome);
            }
            
            java.io.File outputFile = new java.io.File(downloadsPath.toFile(), fileName);
            try (java.io.FileWriter writer = new java.io.FileWriter(outputFile, java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(html);
            }
            
            return outputFile.getAbsolutePath();
            
        } catch (Exception e) {
            System.err.println("Error al guardar archivo: " + e.getMessage());
            return null;
        }
    }
}