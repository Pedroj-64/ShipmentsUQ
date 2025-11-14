package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.GridMapViewController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.RealMapService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.ShipmentDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.GridMap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la interfaz de seguimiento de envíos que muestra la ubicación del repartidor en un mapa
 */
public class ShipmentTrackingViewController implements Initializable {
    
    @FXML
    private Label lbl_shipmentId;
    @FXML
    private Label lbl_shipmentStatus;
    @FXML
    private Label lbl_delivererName;
    @FXML
    private Pane mapContainer;
    @FXML
    private Label lbl_coordinates;
    @FXML
    private Label lbl_distanceToDestination;
    @FXML
    private Button btn_refresh;
    @FXML
    private Button btn_close;
    @FXML
    private Button btn_openRealTimeMap;
    
    private ShipmentDTO shipmentDTO;
    private ShipmentService shipmentService;
    private GridMapViewController mapController;
    private RealMapService realMapService;
    @SuppressWarnings("unused")
    private GridMap gridMap;  // Reservado para funcionalidad futura
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    
    private boolean usingRealCoordinates = false;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurar el mapa con un tamaño adecuado para la visualización
        double mapWidth = 560;   // Ancho fijo
        double mapHeight = 280;  // Alto fijo
        double cellSize = 20;    // Tamaño de cada celda en píxeles
        
        // Inicializar el controlador del mapa (NO clickeable para tracking)
        mapController = new GridMapViewController(mapWidth, mapHeight, cellSize, false);
        gridMap = new GridMap((int)(mapWidth/cellSize), (int)(mapHeight/cellSize));
        
        // Inicializar el mapa en el contenedor
        mapController.initialize(mapContainer);
        
        // Inicializar servicio de coordenadas GPS
        realMapService = new RealMapService();
        
        // Configurar botones
        btn_refresh.setOnAction(event -> refreshTracking());
        btn_close.setOnAction(event -> ((Stage) btn_close.getScene().getWindow()).close());
        
        // NOTE: Botón para abrir mapa de tracking en tiempo real
        if (btn_openRealTimeMap != null) {
            btn_openRealTimeMap.setOnAction(event -> openRealTimeTrackingMap());
        }
    }
    
    /**
     * Inicializa el controlador con los datos del envío y servicio
     * @param shipmentDTO envío a rastrear
     * @param shipmentService servicio para obtener datos actualizados
     */
    public void initData(ShipmentDTO shipmentDTO, ShipmentService shipmentService) {
        this.shipmentDTO = shipmentDTO;
        this.shipmentService = shipmentService;
        
        // Cargar los datos iniciales
        refreshTracking();
    }
    
    /**
     * Actualiza los datos de seguimiento del envío
     */
    private void refreshTracking() {
        if (shipmentDTO != null && shipmentService != null) {
            // Obtener datos actualizados del envío
            Optional<Shipment> shipmentOptional = shipmentService.findById(shipmentDTO.getId());
            
            if (shipmentOptional.isPresent()) {
                Shipment shipment = shipmentOptional.get();
                Deliverer deliverer = shipment.getDeliverer();
                Address origin = shipment.getOrigin();
                Address destination = shipment.getDestination();
                
                // Actualizar la información en la UI
                lbl_shipmentId.setText(shipment.getId().toString());
                lbl_shipmentStatus.setText(shipment.getStatus().toString());
                
                if (deliverer != null) {
                    lbl_delivererName.setText(deliverer.getName());
                    
                    // Verificar si el repartidor tiene coordenadas GPS reales
                    if (deliverer.hasRealCoordinates()) {
                        // Usar coordenadas GPS
                        usingRealCoordinates = true;
                        double lat = deliverer.getRealLatitude();
                        double lng = deliverer.getRealLongitude();
                        
                        lbl_coordinates.setText(String.format("GPS: %.6f, %.6f", lat, lng));
                        
                        // Convertir a Grid para mostrar en el mapa
                        double[] gridCoords = realMapService.convertRealToGrid(lat, lng);
                        mapController.setSelectedCoordinates(gridCoords[0], gridCoords[1]);
                        
                        // Calcular distancia GPS al destino si existe
                        if (destination != null) {
                            Coordinates delivererCoords = new Coordinates(lat, lng);
                            
                            // Verificar si el destino también tiene GPS
                            if (destination.getGpsLatitude() != null && destination.getGpsLongitude() != null) {
                                Coordinates destCoords = new Coordinates(
                                    destination.getGpsLatitude(), 
                                    destination.getGpsLongitude()
                                );
                                double distanceKm = delivererCoords.distanceTo(destCoords);
                                lbl_distanceToDestination.setText(
                                    decimalFormat.format(distanceKm) + " km (GPS)"
                                );
                            } else {
                                // Destino solo tiene Grid, estimar desde GPS
                                double[] destGridCoords = {destination.getCoordX(), destination.getCoordY()};
                                double[] destGPS = realMapService.convertGridToReal(
                                    destGridCoords[0], destGridCoords[1]
                                );
                                Coordinates destCoords = new Coordinates(destGPS[0], destGPS[1]);
                                double distanceKm = delivererCoords.distanceTo(destCoords);
                                lbl_distanceToDestination.setText(
                                    decimalFormat.format(distanceKm) + " km (estimado)"
                                );
                            }
                        } else {
                            lbl_distanceToDestination.setText("N/A");
                        }
                        
                    } else {
                        // Usar coordenadas Grid tradicionales
                        usingRealCoordinates = false;
                        double delivererX = deliverer.getCurrentX();
                        double delivererY = deliverer.getCurrentY();
                        lbl_coordinates.setText(String.format("Grid: (%.0f, %.0f)", delivererX, delivererY));
                        
                        // Establecer las coordenadas en el mapa
                        mapController.setSelectedCoordinates(delivererX, delivererY);
                        
                        // Si hay dirección de destino, calcular la distancia Grid
                        if (destination != null) {
                            double destX = destination.getCoordX();
                            double destY = destination.getCoordY();
                            
                            double distance = calculateDistance(delivererX, delivererY, destX, destY);
                            lbl_distanceToDestination.setText(
                                decimalFormat.format(distance) + " unidades (Grid)"
                            );
                        } else {
                            lbl_distanceToDestination.setText("N/A");
                        }
                    }
                } else {
                    lbl_delivererName.setText("No asignado");
                    lbl_coordinates.setText("N/A");
                    lbl_distanceToDestination.setText("N/A");
                }
                
                // Autorellenar información geográfica
                if (origin != null) {
                    String originInfo = buildLocationInfo("Origen", origin);
                    System.out.println(originInfo);
                }
                
                if (destination != null) {
                    String destInfo = buildLocationInfo("Destino", destination);
                    System.out.println(destInfo);
                }
            }
        }
    }
    
    /**
     * Construye información legible de una ubicación
     */
    private String buildLocationInfo(String label, Address address) {
        StringBuilder info = new StringBuilder();
        info.append(label).append(": ");
        info.append(address.getStreet()).append(", ");
        info.append(address.getCity());
        
        if (address.getGpsLatitude() != null && address.getGpsLongitude() != null) {
            info.append(String.format(" [GPS: %.6f, %.6f]", 
                address.getGpsLatitude(), address.getGpsLongitude()));
        } else {
            info.append(String.format(" [Grid: %.0f, %.0f]", 
                address.getCoordX(), address.getCoordY()));
        }
        
        return info.toString();
    }
    
    /**
     * Calcula la distancia euclidiana entre dos puntos
     * @param x1 coordenada x del punto 1
     * @param y1 coordenada y del punto 1
     * @param x2 coordenada x del punto 2
     * @param y2 coordenada y del punto 2
     * @return distancia entre los puntos
     */
    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    /**
     * NOTE: Abre el mapa de tracking en tiempo real en el navegador
     * Muestra los 3 puntos: origen, repartidor, destino con animación
     */
    private void openRealTimeTrackingMap() {
        try {
            if (shipmentDTO == null) {
                showAlert("Error", "No hay información de envío disponible", Alert.AlertType.ERROR);
                return;
            }
            
            // Verificar que el envío tenga repartidor asignado
            Optional<Shipment> shipmentOpt = shipmentService.findById(shipmentDTO.getId());
            if (shipmentOpt.isEmpty() || shipmentOpt.get().getDeliverer() == null) {
                showAlert("Información", "Este envío aún no tiene repartidor asignado", Alert.AlertType.INFORMATION);
                return;
            }
            
            // Iniciar el servidor del mapa si no está activo
            if (realMapService.startMapServer()) {
                // Abrir el mapa de tracking especializado
                realMapService.openMapInBrowser(RealMapService.MapType.TRACKING);
                
                showAlert("Mapa Abierto", 
                    "Mapa de seguimiento en tiempo real abierto en el navegador.\\n" +
                    "El mapa se actualizará automáticamente cada 30 segundos.",
                    Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "No se pudo iniciar el servidor del mapa", Alert.AlertType.ERROR);
            }
            
        } catch (Exception e) {
            System.err.println("Error al abrir mapa de tracking: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Error al abrir el mapa: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Muestra un diálogo de alerta
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}