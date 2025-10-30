package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AdminMetricsNewController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;

/**
 * Controlador de vista para AdminMetricsNew.
 * Maneja la presentación de las métricas en la interfaz.
 */
public class AdminMetricsNewViewController implements Initializable {

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

    private AdminMetricsNewController controller;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controller = new AdminMetricsNewController();
        loadAllMetrics();
    }

    /**
     * Carga todas las métricas
     */
    private void loadAllMetrics() {
        try {
            loadBasicMetrics();
            loadShipmentStatusChart();
            loadShipmentsByMonthChart();
            loadRevenueByMonthChart();
            loadTopDeliverers();
        } catch (Exception e) {
            System.err.println("Error al cargar métricas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga las métricas básicas (labels)
     */
    private void loadBasicMetrics() {
        lbl_totalShipments.setText(String.valueOf(controller.getTotalShipments()));
        lbl_deliveredShipments.setText(String.valueOf(controller.getTotalDeliveredShipments()));
        lbl_inTransitShipments.setText(String.valueOf(controller.getTotalInTransitShipments()));
        lbl_pendingShipments.setText(String.valueOf(controller.getTotalPendingShipments()));
        
        lbl_totalUsers.setText(String.valueOf(controller.getTotalUsers()));
        lbl_totalDeliverers.setText(String.valueOf(controller.getTotalDeliverers()));
        
        double totalRevenue = controller.getTotalRevenue();
        lbl_totalRevenue.setText(String.format("$%.2f", totalRevenue));
        
        double monthRevenue = controller.getCurrentMonthRevenue();
        lbl_currentMonthRevenue.setText(String.format("$%.2f", monthRevenue));
        
        double avgTime = controller.getAverageDeliveryTime();
        lbl_avgDeliveryTime.setText(String.format("%.1f horas", avgTime));
    }

    /**
     * Configura el gráfico de estado de envíos (PieChart)
     */
    private void loadShipmentStatusChart() {
        Map<ShipmentStatus, Integer> statusData = controller.getShipmentsByStatus();
        
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        
        for (Map.Entry<ShipmentStatus, Integer> entry : statusData.entrySet()) {
            String statusName = getStatusDisplayName(entry.getKey());
            chartData.add(new PieChart.Data(statusName, entry.getValue()));
        }
        
        chart_shipmentStatus.setData(chartData);
        
        // Añadir tooltips
        for (final PieChart.Data data : chart_shipmentStatus.getData()) {
            Tooltip tooltip = new Tooltip(String.format("%s: %d envíos", 
                    data.getName(), (int) data.getPieValue()));
            Tooltip.install(data.getNode(), tooltip);
        }
    }

    /**
     * Configura el gráfico de envíos por mes (BarChart)
     */
    private void loadShipmentsByMonthChart() {
        Map<String, Integer> monthData = controller.getShipmentsByMonth();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Envíos");
        
        for (Map.Entry<String, Integer> entry : monthData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        chart_shipmentsByMonth.getData().add(series);
    }

    /**
     * Configura el gráfico de ingresos por mes (LineChart)
     */
    private void loadRevenueByMonthChart() {
        Map<String, Double> revenueData = controller.getRevenueByMonth();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ingresos");
        
        for (Map.Entry<String, Double> entry : revenueData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        chart_revenueByMonth.getData().add(series);
    }

    /**
     * Configura el grid de mejores repartidores
     */
    private void loadTopDeliverers() {
        List<Map.Entry<String, Integer>> topDeliverers = controller.getTopDeliverers(5);
        
        grid_topDeliverers.getChildren().clear();
        
        // Encabezados
        Label nameHeader = new Label("Repartidor");
        nameHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        grid_topDeliverers.add(nameHeader, 0, 0);
        
        Label shipmentsHeader = new Label("Envíos");
        shipmentsHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        grid_topDeliverers.add(shipmentsHeader, 1, 0);
        
        // Datos
        int row = 1;
        for (Map.Entry<String, Integer> entry : topDeliverers) {
            Label nameLabel = new Label(entry.getKey());
            nameLabel.setStyle("-fx-text-fill: #34495e;");
            grid_topDeliverers.add(nameLabel, 0, row);
            
            Label countLabel = new Label(entry.getValue().toString());
            countLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            grid_topDeliverers.add(countLabel, 1, row);
            
            row++;
        }
    }

    /**
     * Convierte el enum de estado a nombre amigable
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
