package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.GridMapViewController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.ShipmentDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.GridMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

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
    
    private ShipmentDTO shipmentDTO;
    private ShipmentService shipmentService;
    private GridMapViewController mapController;
    private GridMap gridMap;
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurar el mapa con un tamaño adecuado para la visualización
        double mapWidth = 550;
        double mapHeight = 300;
        double cellSize = 20; // Tamaño de cada celda en píxeles
        
        // Inicializar el controlador del mapa y el mapa
        mapController = new GridMapViewController(mapWidth, mapHeight, cellSize);
        gridMap = new GridMap((int)(mapWidth/cellSize), (int)(mapHeight/cellSize));
        
        // Configurar el listener para clicks en el mapa (opcional)
        mapController.setCoordinateListener((x, y) -> {
            // No hacemos nada cuando el usuario hace clic, es sólo para visualización
        });
        
        // Inicializar el mapa en el contenedor
        mapController.initialize(mapContainer);
        
        // Configurar botones
        btn_refresh.setOnAction(event -> refreshTracking());
        btn_close.setOnAction(event -> ((Stage) btn_close.getScene().getWindow()).close());
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
                
                // Actualizar la información en la UI
                lbl_shipmentId.setText(shipment.getId().toString());
                lbl_shipmentStatus.setText(shipment.getStatus().toString());
                
                if (deliverer != null) {
                    lbl_delivererName.setText(deliverer.getName());
                    
                    // Obtener y mostrar las coordenadas actuales del repartidor
                    double delivererX = deliverer.getX();
                    double delivererY = deliverer.getY();
                    lbl_coordinates.setText(String.format("(%.0f, %.0f)", delivererX, delivererY));
                    
                    // Establecer las coordenadas en el mapa
                    mapController.setSelectedCoordinates(delivererX, delivererY);
                    
                    // Si hay dirección de destino, calcular la distancia
                    Address destination = shipment.getDestination();
                    if (destination != null) {
                        // Calcular distancia desde la posición actual del repartidor al destino
                        double destX = destination.getCoordX();
                        double destY = destination.getCoordY();
                        
                        double distance = calculateDistance(delivererX, delivererY, destX, destY);
                        lbl_distanceToDestination.setText(decimalFormat.format(distance) + " unidades");
                    } else {
                        lbl_distanceToDestination.setText("N/A");
                    }
                } else {
                    lbl_delivererName.setText("No asignado");
                    lbl_coordinates.setText("N/A");
                    lbl_distanceToDestination.setText("N/A");
                }
            }
        }
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
}