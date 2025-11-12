package co.edu.uniquindio.sameday.shipmentsuqsameday.mapping;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Demo/Prueba del sistema de mapas interactivos
 * Muestra el mapa con Leaflet y OpenStreetMap integrado en JavaFX
 */
public class MapDemo extends Application {
    
    private InteractiveMapView mapView;
    private Label lblOriginCoords;
    private Label lblDestinationCoords;
    private Label lblDistance;
    private Label lblEstimatedTime;
    private Label lblEstimatedCost;
    private TextArea txtRouteInfo;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ShipmentsUQ - Sistema de Mapas Interactivos");
        
        // Crear el layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f8fa, #e8f4f8);");
        
        // Panel superior con título e información
        VBox topPanel = createTopPanel();
        root.setTop(topPanel);
        
        // Panel central con el mapa
        VBox centerPanel = createMapPanel();
        root.setCenter(centerPanel);
        
        // Panel derecho con información de ruta
        VBox rightPanel = createInfoPanel();
        root.setRight(rightPanel);
        
        // Panel inferior con botones de acción
        HBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);
        
        // Crear la escena
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createTopPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        
        Label title = new Label("🗺️ Sistema de Mapas Interactivos");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label subtitle = new Label("OpenStreetMap + Leaflet.js integrado en JavaFX");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");
        
        panel.getChildren().addAll(title, subtitle);
        return panel;
    }
    
    private VBox createMapPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        
        Label mapTitle = new Label("📍 Selecciona Origen y Destino en el Mapa");
        mapTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Crear el mapa interactivo
        mapView = new InteractiveMapView();
        
        // Callback cuando cambien las coordenadas
        mapView.setOnCoordinatesSelected((origin, destination) -> {
            updateRouteInfo(origin, destination);
        });
        
        // El WebView debe crecer para llenar el espacio
        VBox.setVgrow(mapView.getWebView(), Priority.ALWAYS);
        
        panel.getChildren().addAll(mapTitle, mapView.getWebView());
        return panel;
    }
    
    private VBox createInfoPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(350);
        panel.setStyle("-fx-background-color: white; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 5);");
        
        Label infoTitle = new Label("📊 Información de Ruta");
        infoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Separator sep1 = new Separator();
        
        // Coordenadas de origen
        Label lblOriginTitle = new Label("Origen:");
        lblOriginTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #64748b;");
        lblOriginCoords = new Label("No seleccionado");
        lblOriginCoords.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b;");
        lblOriginCoords.setWrapText(true);
        
        // Coordenadas de destino
        Label lblDestinationTitle = new Label("Destino:");
        lblDestinationTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #64748b;");
        lblDestinationCoords = new Label("No seleccionado");
        lblDestinationCoords.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b;");
        lblDestinationCoords.setWrapText(true);
        
        Separator sep2 = new Separator();
        
        // Información calculada
        Label lblCalcTitle = new Label("Cálculos:");
        lblCalcTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #64748b;");
        
        lblDistance = new Label("Distancia: -");
        lblDistance.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b;");
        
        lblEstimatedTime = new Label("Tiempo estimado: -");
        lblEstimatedTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b;");
        
        lblEstimatedCost = new Label("Costo estimado: -");
        lblEstimatedCost.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b;");
        
        Separator sep3 = new Separator();
        
        // Área de información detallada
        txtRouteInfo = new TextArea();
        txtRouteInfo.setEditable(false);
        txtRouteInfo.setWrapText(true);
        txtRouteInfo.setPrefRowCount(8);
        txtRouteInfo.setStyle("-fx-font-size: 11px; -fx-font-family: 'Courier New';");
        txtRouteInfo.setText("Selecciona origen y destino en el mapa para ver información detallada de la ruta.");
        
        VBox.setVgrow(txtRouteInfo, Priority.ALWAYS);
        
        panel.getChildren().addAll(
            infoTitle, sep1,
            lblOriginTitle, lblOriginCoords,
            lblDestinationTitle, lblDestinationCoords,
            sep2,
            lblCalcTitle, lblDistance, lblEstimatedTime, lblEstimatedCost,
            sep3,
            txtRouteInfo
        );
        
        return panel;
    }
    
    private HBox createBottomPanel() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(15, 20, 15, 20));
        panel.setAlignment(Pos.CENTER_RIGHT);
        panel.setStyle("-fx-background-color: white; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, -3);");
        
        Button btnClear = new Button("🗑️ Limpiar Mapa");
        btnClear.setStyle(
            "-fx-background-color: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px 20px;" +
            "-fx-cursor: hand;"
        );
        btnClear.setOnAction(e -> clearMap());
        
        Button btnCenterArmenia = new Button("📍 Centrar en Armenia");
        btnCenterArmenia.setStyle(
            "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px 20px;" +
            "-fx-cursor: hand;"
        );
        btnCenterArmenia.setOnAction(e -> centerOnArmenia());
        
        Button btnExport = new Button("💾 Exportar Coordenadas");
        btnExport.setStyle(
            "-fx-background-color: linear-gradient(135deg, #10b981 0%, #059669 100%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px 20px;" +
            "-fx-cursor: hand;"
        );
        btnExport.setOnAction(e -> exportCoordinates());
        
        panel.getChildren().addAll(btnClear, btnCenterArmenia, btnExport);
        return panel;
    }
    
    private void updateRouteInfo(Coordinates origin, Coordinates destination) {
        // Actualizar coordenadas
        lblOriginCoords.setText(String.format("Lat: %.6f, Lng: %.6f", 
            origin.getLatitude(), origin.getLongitude()));
        lblDestinationCoords.setText(String.format("Lat: %.6f, Lng: %.6f", 
            destination.getLatitude(), destination.getLongitude()));
        
        // Calcular métricas
        double distance = origin.distanceTo(destination);
        String estimatedTime = MapCalculator.formatEstimatedTime(origin, destination);
        double cost = MapCalculator.calculateCost(origin, destination);
        
        lblDistance.setText(String.format("Distancia: %.2f km", distance));
        lblEstimatedTime.setText("Tiempo estimado: " + estimatedTime);
        lblEstimatedCost.setText(String.format("Costo estimado: $%,.0f COP", cost));
        
        // Información detallada
        String routeInfo = MapCalculator.getRouteInfo(origin, destination);
        txtRouteInfo.setText(
            "=== INFORMACIÓN DE RUTA ===\n\n" +
            routeInfo + "\n\n" +
            "=== COORDENADAS ===\n" +
            "Origen: " + origin.toString() + "\n" +
            "Destino: " + destination.toString() + "\n\n" +
            "Dentro del área de servicio:\n" +
            "Origen: " + (origin.isInServiceArea() ? "✓ Sí" : "✗ No") + "\n" +
            "Destino: " + (destination.isInServiceArea() ? "✓ Sí" : "✗ No")
        );
    }
    
    private void clearMap() {
        mapView.clearMarkers();
        lblOriginCoords.setText("No seleccionado");
        lblDestinationCoords.setText("No seleccionado");
        lblDistance.setText("Distancia: -");
        lblEstimatedTime.setText("Tiempo estimado: -");
        lblEstimatedCost.setText("Costo estimado: -");
        txtRouteInfo.setText("Mapa limpiado. Selecciona nuevas ubicaciones.");
    }
    
    private void centerOnArmenia() {
        mapView.centerMapOn(4.533889, -75.681111, 13);
    }
    
    private void exportCoordinates() {
        Coordinates origin = mapView.getSelectedOrigin();
        Coordinates destination = mapView.getSelectedDestination();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exportar Coordenadas");
        alert.setHeaderText("Coordenadas seleccionadas");
        alert.setContentText(
            "ORIGEN:\n" +
            "Latitud: " + origin.getLatitude() + "\n" +
            "Longitud: " + origin.getLongitude() + "\n\n" +
            "DESTINO:\n" +
            "Latitud: " + destination.getLatitude() + "\n" +
            "Longitud: " + destination.getLongitude()
        );
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
