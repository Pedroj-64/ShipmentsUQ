package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AdminMetricsController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/**
 * Controlador de vista para la pantalla de métricas administrativas.
 */
public class AdminMetricsViewController implements Initializable {

    @FXML private Label lbl_totalShipments;
    @FXML private Label lbl_deliveredShipments;
    @FXML private Label lbl_inTransitShipments;
    @FXML private Label lbl_pendingShipments;
    @FXML private Label lbl_totalUsers;
    @FXML private Label lbl_totalDeliverers;
    @FXML private Label lbl_totalRevenue;
    @FXML private Label lbl_currentMonthRevenue;
    @FXML private Label lbl_avgDeliveryTime;

    @FXML private PieChart chart_shipmentStatus;
    @FXML private BarChart<String, Number> chart_shipmentsByMonth;
    @FXML private LineChart<String, Number> chart_revenueByMonth;
    @FXML private GridPane grid_topDeliverers;

    // Controlador de negocio
    private AdminMetricsController controller;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar el controlador
        controller = new AdminMetricsController();
        
        // Cargar datos
        loadMetricsData();
        setupShipmentStatusChart();
        setupShipmentsByMonthChart();
        setupRevenueByMonthChart();
        setupTopDeliverersGrid();
    }

    /**
     * Carga los datos de las métricas principales
     */
    private void loadMetricsData() {
        try {
            // Cargar datos de envíos
            lbl_totalShipments.setText(String.valueOf(controller.getTotalShipments()));
            lbl_deliveredShipments.setText(String.valueOf(controller.getTotalDeliveredShipments()));
            lbl_inTransitShipments.setText(String.valueOf(controller.getTotalInTransitShipments()));
            lbl_pendingShipments.setText(String.valueOf(controller.getTotalPendingShipments()));
            
            // Cargar datos de usuarios y repartidores
            lbl_totalUsers.setText(String.valueOf(controller.getTotalUsers()));
            lbl_totalDeliverers.setText(String.valueOf(controller.getTotalDeliverers()));
            
            // Cargar datos de ingresos
            double totalRevenue = controller.getTotalRevenue();
            lbl_totalRevenue.setText(String.format("$%.2f", totalRevenue));
            
            double monthRevenue = controller.getCurrentMonthRevenue();
            lbl_currentMonthRevenue.setText(String.format("$%.2f", monthRevenue));
            
            // Cargar tiempo promedio de entrega
            double avgTime = controller.getAverageDeliveryTime();
            lbl_avgDeliveryTime.setText(String.format("%.1f horas", avgTime));
        } catch (Exception e) {
            System.err.println("Error al cargar métricas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configura el gráfico de estado de envíos
     */
    private void setupShipmentStatusChart() {
        try {
            // Obtener datos
            Map<ShipmentStatus, Integer> statusData = controller.getShipmentsByStatus();
            
            // Convertir a datos para el gráfico
            ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
            
            for (Map.Entry<ShipmentStatus, Integer> entry : statusData.entrySet()) {
                if (entry.getValue() > 0) {
                    String statusName = getStatusDisplayName(entry.getKey());
                    chartData.add(new PieChart.Data(statusName, entry.getValue()));
                }
            }
            
            // Asignar datos al gráfico
            chart_shipmentStatus.setData(chartData);
            chart_shipmentStatus.setTitle("Envíos por Estado");
            
            // Añadir tooltips a los sectores del gráfico
            for (final PieChart.Data data : chart_shipmentStatus.getData()) {
                Tooltip tooltip = new Tooltip(String.format("%s: %d envíos", 
                        data.getName(), (int) data.getPieValue()));
                
                Tooltip.install(data.getNode(), tooltip);
                
                // Resaltar al pasar el mouse
                data.getNode().setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        data.getNode().setStyle("-fx-opacity: 0.8;");
                    }
                });
                
                data.getNode().setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        data.getNode().setStyle("-fx-opacity: 1.0;");
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error al configurar gráfico de estado de envíos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configura el gráfico de envíos por mes
     */
    private void setupShipmentsByMonthChart() {
        try {
            // Obtener datos
            Map<String, Integer> monthData = controller.getShipmentsByMonth();
            
            // Crear serie de datos
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Envíos por Mes");
            
            // Añadir datos a la serie
            for (Map.Entry<String, Integer> entry : monthData.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            
            // Configurar gráfico
            chart_shipmentsByMonth.getData().add(series);
            chart_shipmentsByMonth.setTitle("Envíos por Mes (últimos 6 meses)");
            
            // Añadir tooltips
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(String.format("%s: %d envíos", 
                        data.getXValue(), data.getYValue().intValue()));
                
                Tooltip.install(data.getNode(), tooltip);
            }
        } catch (Exception e) {
            System.err.println("Error al configurar gráfico de envíos por mes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configura el gráfico de ingresos por mes
     */
    private void setupRevenueByMonthChart() {
        try {
            // Obtener datos
            Map<String, Double> revenueData = controller.getRevenueByMonth();
            
            // Crear serie de datos
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ingresos por Mes");
            
            // Añadir datos a la serie
            for (Map.Entry<String, Double> entry : revenueData.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            
            // Configurar gráfico
            chart_revenueByMonth.getData().add(series);
            chart_revenueByMonth.setTitle("Ingresos por Mes (últimos 6 meses)");
            
            // Añadir tooltips
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(String.format("%s: $%.2f", 
                        data.getXValue(), data.getYValue().doubleValue()));
                
                Tooltip.install(data.getNode(), tooltip);
            }
        } catch (Exception e) {
            System.err.println("Error al configurar gráfico de ingresos por mes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configura el grid de los mejores repartidores
     */
    private void setupTopDeliverersGrid() {
        try {
            // Obtener los top 5 repartidores
            List<Map.Entry<String, Integer>> topDeliverers = controller.getTopDeliverers(5);
            
            // Limpiar el grid
            grid_topDeliverers.getChildren().clear();
            
            // Añadir títulos de columnas
            Label nameHeader = new Label("Nombre");
            nameHeader.setStyle("-fx-font-weight: bold;");
            grid_topDeliverers.add(nameHeader, 0, 0);
            
            Label shipmentsHeader = new Label("Envíos");
            shipmentsHeader.setStyle("-fx-font-weight: bold;");
            grid_topDeliverers.add(shipmentsHeader, 1, 0);
            
            // Añadir datos de repartidores
            int row = 1;
            for (Map.Entry<String, Integer> entry : topDeliverers) {
                grid_topDeliverers.add(new Label(entry.getKey()), 0, row);
                grid_topDeliverers.add(new Label(entry.getValue().toString()), 1, row);
                row++;
            }
        } catch (Exception e) {
            System.err.println("Error al configurar grid de mejores repartidores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convierte el enum de estado de envío a un nombre más amigable para el usuario
     * @param status Estado del envío
     * @return Nombre amigable para el usuario
     */
    private String getStatusDisplayName(ShipmentStatus status) {
        switch (status) {
            case PENDING: return "Pendientes";
            case ASSIGNED: return "En Ruta";
            case DELIVERED: return "Entregados";
            case CANCELLED: return "Cancelados";
            default: return status.toString();
        }
    }
}