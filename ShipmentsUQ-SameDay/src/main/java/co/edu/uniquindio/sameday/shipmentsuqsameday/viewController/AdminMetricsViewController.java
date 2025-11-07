package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AdminMetricsController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.application.Platform;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controlador de vista para la pantalla de métricas administrativas
 * Maneja la interacción del usuario y actualización de la interfaz gráfica
 */
public class AdminMetricsViewController implements Initializable {

    // Etiquetas para métricas generales
    @FXML
    private Label lbl_totalShipments;
    
    @FXML
    private Label lbl_monthlyShipments;
    
    @FXML
    private Label lbl_dailyShipments;
    
    @FXML
    private Label lbl_avgDeliveryTime;
    
    @FXML
    private Label lbl_successRate;
    
    @FXML
    private Label lbl_courierEfficiency;
    
    // Etiquetas para detalles de estado
    @FXML
    private Label lbl_pendingShipments;
    
    @FXML
    private Label lbl_inTransitShipments;
    
    @FXML
    private Label lbl_deliveredShipments;
    
    @FXML
    private Label lbl_cancelledShipments;
    
    @FXML
    private Label lbl_lastUpdate;

    // Gráficos
    @FXML
    private PieChart chart_shipmentStatus;
    
    @FXML
    private BarChart<String, Number> chart_monthlyTrend;

    // Botones de reportes
    @FXML
    private Button btn_generateGeneralReport;
    
    @FXML
    private Button btn_generateMonthlyReport;
    
    @FXML
    private Button btn_generateDailyReport;
    
    @FXML
    private Button btn_refreshMetrics;

    // Controlador de lógica de negocio
    private AdminMetricsController metricsController;
    
    // Formateo de números
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Inicializar controlador de métricas
            metricsController = new AdminMetricsController();
            
            // Configurar gráficos
            configureCharts();
            
            // Cargar métricas iniciales
            loadMetrics();
            
            // Configurar listeners de botones
            setupButtonListeners();
            
        } catch (Exception e) {
            System.err.println("Error al inicializar AdminMetricsViewController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Configura los gráficos iniciales
     */
    private void configureCharts() {
        try {
            // Configurar gráfico de pastel para estados de envíos
            chart_shipmentStatus.setTitle("Distribución de Envíos por Estado");
            chart_shipmentStatus.setLegendVisible(true);
            chart_shipmentStatus.setLabelsVisible(true);
            
            // Configurar gráfico de barras para tendencia mensual
            chart_monthlyTrend.setTitle("Envíos por Mes");
            chart_monthlyTrend.getXAxis().setLabel("Mes");
            chart_monthlyTrend.getYAxis().setLabel("Cantidad de Envíos");
            chart_monthlyTrend.setLegendVisible(false);
            
        } catch (Exception e) {
            System.err.println("Error al configurar gráficos: " + e.getMessage());
        }
    }
    
    /**
     * Carga todas las métricas del sistema
     */
    private void loadMetrics() {
        try {
            // Actualizar métricas generales
            updateGeneralMetrics();
            
            // Actualizar gráficos
            updateCharts();
            
        } catch (Exception e) {
            System.err.println("Error al cargar métricas: " + e.getMessage());
            showAlert("Error", "Error al cargar las métricas del sistema", Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Actualiza las métricas generales en las etiquetas
     */
    private void updateGeneralMetrics() {
        try {
            // Obtener datos del controlador
            int totalShipments = metricsController.getTotalShipments();
            int monthlyShipments = metricsController.getMonthlyShipments();
            int dailyShipments = metricsController.getDailyShipments();
            double averageDelivery = metricsController.getAverageDeliveryTime();
            double successRate = metricsController.getSuccessRate();
            double courierEfficiency = metricsController.getCourierEfficiency();
            
            // Obtener detalles de estado
            Map<ShipmentStatus, Integer> statusData = metricsController.getShipmentsByStatus();
            
            // Actualizar etiquetas en el hilo de JavaFX
            Platform.runLater(() -> {
                lbl_totalShipments.setText(String.valueOf(totalShipments));
                lbl_monthlyShipments.setText(String.valueOf(monthlyShipments));
                lbl_dailyShipments.setText(String.valueOf(dailyShipments));
                
                // Formatear tiempo de entrega
                if (averageDelivery > 0) {
                    if (averageDelivery < 60) {
                        lbl_avgDeliveryTime.setText(decimalFormat.format(averageDelivery) + " min");
                    } else {
                        double hours = averageDelivery / 60;
                        lbl_avgDeliveryTime.setText(decimalFormat.format(hours) + " hrs");
                    }
                } else {
                    lbl_avgDeliveryTime.setText("N/A");
                }
                
                lbl_successRate.setText(decimalFormat.format(successRate) + "%");
                lbl_courierEfficiency.setText(decimalFormat.format(courierEfficiency) + "%");
                
                // Actualizar detalles de estado
                lbl_pendingShipments.setText(String.valueOf(statusData.getOrDefault(ShipmentStatus.PENDING, 0)));
                lbl_inTransitShipments.setText(String.valueOf(statusData.getOrDefault(ShipmentStatus.IN_TRANSIT, 0)));
                lbl_deliveredShipments.setText(String.valueOf(statusData.getOrDefault(ShipmentStatus.DELIVERED, 0)));
                lbl_cancelledShipments.setText(String.valueOf(statusData.getOrDefault(ShipmentStatus.CANCELLED, 0)));
                
                // Actualizar timestamp
                lbl_lastUpdate.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            });
            
        } catch (Exception e) {
            System.err.println("Error al actualizar métricas generales: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza los gráficos con datos actuales
     */
    private void updateCharts() {
        try {
            // Actualizar gráfico de estados de envíos
            updateShipmentStatusChart();
            
            // Actualizar gráfico de tendencia mensual
            updateMonthlyTrendChart();
            
        } catch (Exception e) {
            System.err.println("Error al actualizar gráficos: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza el gráfico de pastel con los estados de envíos
     */
    private void updateShipmentStatusChart() {
        try {
            Map<ShipmentStatus, Integer> statusData = metricsController.getShipmentsByStatus();
            
            Platform.runLater(() -> {
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                
                for (Map.Entry<ShipmentStatus, Integer> entry : statusData.entrySet()) {
                    if (entry.getValue() > 0) { // Solo mostrar estados con envíos
                        String statusName = translateStatus(entry.getKey());
                        pieChartData.add(new PieChart.Data(statusName, entry.getValue()));
                    }
                }
                
                chart_shipmentStatus.setData(pieChartData);
            });
            
        } catch (Exception e) {
            System.err.println("Error al actualizar gráfico de estados: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza el gráfico de barras con la tendencia mensual
     */
    private void updateMonthlyTrendChart() {
        try {
            Map<String, Integer> monthlyData = metricsController.getMonthlyShipmentsData();
            
            Platform.runLater(() -> {
                chart_monthlyTrend.getData().clear();
                
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Envíos por Mes");
                
                for (Map.Entry<String, Integer> entry : monthlyData.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
                
                chart_monthlyTrend.getData().add(series);
            });
            
        } catch (Exception e) {
            System.err.println("Error al actualizar gráfico mensual: " + e.getMessage());
        }
    }
    
    /**
     * Configura los listeners de los botones
     */
    private void setupButtonListeners() {
        try {
            btn_generateGeneralReport.setOnAction(e -> handleGenerateGeneralReport());
            btn_generateMonthlyReport.setOnAction(e -> handleGenerateMonthlyReport());
            btn_generateDailyReport.setOnAction(e -> handleGenerateDailyReport());
            // btn_refreshMetrics ya está configurado en FXML con onAction
            
        } catch (Exception e) {
            System.err.println("Error al configurar listeners de botones: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la acción de refrescar métricas
     */
    @FXML
    private void handleRefreshMetrics() {
        try {
            // Ejecutar actualización en hilo separado
            new Thread(() -> {
                try {
                    loadMetrics();
                    
                    Platform.runLater(() -> {
                        showAlert("Éxito", "Métricas actualizadas correctamente", Alert.AlertType.INFORMATION);
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("Error", "Error al actualizar métricas: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                }
            }).start();
            
        } catch (Exception e) {
            showAlert("Error", "Error al refrescar métricas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Maneja la generación del reporte general
     */
    @FXML
    private void handleGenerateGeneralReport() {
        generateReportAsync("general", () -> metricsController.generateGeneralReport());
    }
    
    /**
     * Maneja la generación del reporte mensual
     */
    @FXML
    private void handleGenerateMonthlyReport() {
        generateReportAsync("mensual", () -> metricsController.generateMonthlyReport());
    }
    
    /**
     * Maneja la generación del reporte diario
     */
    @FXML
    private void handleGenerateDailyReport() {
        generateReportAsync("diario", () -> metricsController.generateDailyReport());
    }
    
    /**
     * Genera un reporte de forma asíncrona
     * @param reportType tipo de reporte
     * @param reportGenerator función que genera el reporte
     */
    private void generateReportAsync(String reportType, ReportGenerator reportGenerator) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    disableReportButtons(true);
                });
                
                String filePath = reportGenerator.generate();
                
                Platform.runLater(() -> {
                    disableReportButtons(false);
                    
                    if (filePath != null) {
                        showAlert("Reporte Generado", 
                                "Reporte " + reportType + " generado exitosamente.\n" +
                                "Archivo guardado en: " + filePath + "\n\n" +
                                "¿Desea abrir el archivo?", 
                                Alert.AlertType.INFORMATION);
                        
                        // Intentar abrir el archivo
                        try {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(new File(filePath));
                            }
                        } catch (Exception e) {
                            System.err.println("No se pudo abrir el archivo automáticamente: " + e.getMessage());
                        }
                        
                    } else {
                        showAlert("Error", "Error al generar el reporte " + reportType, Alert.AlertType.ERROR);
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    disableReportButtons(false);
                    showAlert("Error", "Error al generar reporte: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }
    
    /**
     * Habilita/deshabilita los botones de reportes
     * @param disable true para deshabilitar, false para habilitar
     */
    private void disableReportButtons(boolean disable) {
        btn_generateGeneralReport.setDisable(disable);
        btn_generateMonthlyReport.setDisable(disable);
        btn_generateDailyReport.setDisable(disable);
        
        if (disable) {
            btn_generateGeneralReport.setText("Generando...");
            btn_generateMonthlyReport.setText("Generando...");
            btn_generateDailyReport.setText("Generando...");
        } else {
            btn_generateGeneralReport.setText("Reporte General");
            btn_generateMonthlyReport.setText("Reporte Mensual");
            btn_generateDailyReport.setText("Reporte Diario");
        }
    }
    
    /**
     * Traduce el estado del envío al español
     * @param status estado del envío
     * @return nombre en español
     */
    private String translateStatus(ShipmentStatus status) {
        switch (status) {
            case PENDING:
                return "Pendiente";
            case IN_TRANSIT:
                return "En Tránsito";
            case DELIVERED:
                return "Entregado";
            case CANCELLED:
                return "Cancelado";
            default:
                return status.toString();
        }
    }
    
    /**
     * Muestra una alerta al usuario
     * @param title título de la alerta
     * @param message mensaje de la alerta
     * @param type tipo de alerta
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error al mostrar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Interfaz funcional para generadores de reportes
     */
    @FunctionalInterface
    private interface ReportGenerator {
        String generate();
    }
}