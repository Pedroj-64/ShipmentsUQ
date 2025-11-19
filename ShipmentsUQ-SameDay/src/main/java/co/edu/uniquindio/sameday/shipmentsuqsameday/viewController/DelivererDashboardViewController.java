package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.DelivererController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.Session;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import java.awt.Desktop;
import java.net.URI;

/**
 * Controlador de vista para el dashboard del repartidor.
 * Muestra envíos activos, métricas y permite abrir rutas en el navegador.
 * 
 * Patrón MVC: ViewController - maneja interacción con UI
 * 
 * @author ShipmentsUQ Team
 * @version 3.0
 */
public class DelivererDashboardViewController implements Initializable {

    @FXML
    private Label lbl_title;
    @FXML
    private Label lbl_delivererName;
    @FXML
    private Label lbl_activeShipmentsCount;
    @FXML
    private Label lbl_totalDeliveries;
    @FXML
    private Label lbl_averageRating;
    @FXML
    private Label lbl_status;
    @FXML
    private ListView<Shipment> listView_shipments;
    @FXML
    private Button btn_logout;
    @FXML
    private Button btn_markDelivered;
    @FXML
    private Button btn_refresh;
    @FXML
    private Button btn_calculateRoute;
    @FXML
    private Label lbl_shipmentInfo;
    @FXML
    private VBox vbox_shipmentDetails;
    @FXML
    private Label lbl_shipmentId;
    @FXML
    private Label lbl_customer;
    @FXML
    private Label lbl_origin;
    @FXML
    private Label lbl_destination;
    @FXML
    private Label lbl_shipmentStatus;

    private DelivererController controller;
    private Deliverer currentDeliverer;
    private Shipment selectedShipment;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initController();
        loadDelivererData();
        initButtonListeners();
        setupShipmentListListener();
    }

    /**
     * Inicializa el controlador de negocio y carga el repartidor actual
     */
    private void initController() {
        controller = new DelivererController();
        currentDeliverer = Session.getInstance().getCurrentDeliverer();
        
        if (currentDeliverer == null) {
            AppUtils.showError("Error", "No hay sesión activa de repartidor");
            handleLogout();
            return;
        }
    }

    /**
     * Carga los datos del repartidor en la interfaz
     */
    private void loadDelivererData() {
        if (currentDeliverer == null) return;
        
        lbl_delivererName.setText(currentDeliverer.getName());
        updateMetrics();
        loadActiveShipments();
    }

    /**
     * Actualiza las métricas del repartidor
     */
    private void updateMetrics() {
        int activeCount = currentDeliverer.getCurrentShipments().size();
        int totalDeliveries = currentDeliverer.getTotalDeliveries();
        double avgRating = currentDeliverer.getAverageRating();
        String status = currentDeliverer.getStatus().toString();
        
        lbl_activeShipmentsCount.setText(String.valueOf(activeCount));
        lbl_totalDeliveries.setText(String.valueOf(totalDeliveries));
        lbl_averageRating.setText(String.format("%.1f", avgRating));
        lbl_status.setText(status);
    }

    /**
     * Carga los envíos activos del repartidor
     */
    private void loadActiveShipments() {
        if (currentDeliverer == null) {
            System.err.println("⚠️ No se puede cargar envíos: currentDeliverer es null");
            return;
        }
        
        List<Shipment> activeShipments = currentDeliverer.getCurrentShipments();
        System.out.println("📦 Cargando envíos activos del repartidor: " + currentDeliverer.getName());
        System.out.println("📊 Total de envíos activos: " + (activeShipments != null ? activeShipments.size() : 0));
        
        if (activeShipments == null || activeShipments.isEmpty()) {
            System.out.println("ℹ️ No hay envíos activos para este repartidor");
            listView_shipments.setItems(FXCollections.observableArrayList());
            return;
        }
        
        listView_shipments.setItems(FXCollections.observableArrayList(activeShipments));
        
        // Configurar el formato de visualización de los envíos
        listView_shipments.setCellFactory(lv -> new javafx.scene.control.ListCell<Shipment>() {
            @Override
            protected void updateItem(Shipment shipment, boolean empty) {
                super.updateItem(shipment, empty);
                if (empty || shipment == null) {
                    setText(null);
                } else {
                    String originCity = shipment.getOrigin() != null ? shipment.getOrigin().getCity() : "N/A";
                    String destCity = shipment.getDestination() != null ? shipment.getDestination().getCity() : "N/A";
                    String customerName = shipment.getUser() != null ? shipment.getUser().getName() : "N/A";
                    
                    String text = String.format("📦 #%s\n👤 %s\n📍 %s → %s\n📊 %s",
                        shipment.getId().toString().substring(0, 8),
                        customerName,
                        originCity,
                        destCity,
                        shipment.getStatus());
                    setText(text);
                    System.out.println("  ✓ Envío mostrado: " + shipment.getId());
                }
            }
        });
    }

    /**
     * Configura los listeners de los botones
     */
    private void initButtonListeners() {
        btn_logout.setOnAction(e -> handleLogout());
        btn_refresh.setOnAction(e -> handleRefresh());
        btn_markDelivered.setOnAction(e -> handleMarkDelivered());
        btn_calculateRoute.setOnAction(e -> handleCalculateRoute());
    }

    /**
     * Configura el listener de selección de envíos
     */
    private void setupShipmentListListener() {
        listView_shipments.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedShipment = newVal;
            updateShipmentDetails(newVal);
        });
    }

    /**
     * Actualiza los detalles del envío seleccionado
     */
    private void updateShipmentDetails(Shipment shipment) {
        if (shipment == null) {
            lbl_shipmentInfo.setText("Selecciona un envío de la lista para ver sus detalles");
            vbox_shipmentDetails.setVisible(false);
            vbox_shipmentDetails.setManaged(false);
            return;
        }
        
        // Mostrar detalles
        vbox_shipmentDetails.setVisible(true);
        vbox_shipmentDetails.setManaged(true);
        lbl_shipmentInfo.setText("Información del envío seleccionado:");
        
        lbl_shipmentId.setText("ID: #" + shipment.getId().toString().substring(0, 8));
        lbl_customer.setText("Cliente: " + (shipment.getUser() != null ? shipment.getUser().getName() : "N/A"));
        
        if (shipment.getOrigin() != null) {
            String originText = "Origen: " + shipment.getOrigin().getCity();
            if (shipment.getOrigin().getFullAddress() != null) {
                originText += " - " + shipment.getOrigin().getFullAddress();
            }
            lbl_origin.setText(originText);
        } else {
            lbl_origin.setText("Origen: N/A");
        }
        
        if (shipment.getDestination() != null) {
            String destText = "Destino: " + shipment.getDestination().getCity();
            if (shipment.getDestination().getFullAddress() != null) {
                destText += " - " + shipment.getDestination().getFullAddress();
            }
            lbl_destination.setText(destText);
        } else {
            lbl_destination.setText("Destino: N/A");
        }
        
        lbl_shipmentStatus.setText("Estado: " + shipment.getStatus());
    }

    /**
     * Obtiene las coordenadas GPS reales de una dirección
     */
    private double[] getGPSCoordinates(co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address address) {
        if (address == null) return null;
        
        // Verificar si tiene coordenadas GPS reales
        if (address.hasGpsCoordinates()) {
            return new double[]{address.getGpsLatitude(), address.getGpsLongitude()};
        }
        
        // Si no tiene GPS, usar coordenadas del grid (aproximación)
        return new double[]{address.getX(), address.getY()};
    }

    /**
     * Maneja el evento de calcular ruta - Abre el mapa en el navegador
     */
    @FXML
    private void handleCalculateRoute() {
        if (selectedShipment == null) {
            AppUtils.showWarning("Selección requerida", "Por favor seleccione un envío primero");
            return;
        }
        
        try {
            // Obtener coordenadas del origen y destino
            double[] originCoords = getGPSCoordinates(selectedShipment.getOrigin());
            double[] destCoords = getGPSCoordinates(selectedShipment.getDestination());
            
            if (originCoords == null || destCoords == null) {
                AppUtils.showWarning("Coordenadas no disponibles", "El envío no tiene coordenadas GPS configuradas");
                return;
            }
            
            // Obtener direcciones completas
            String originAddr = selectedShipment.getOrigin().getFullAddress();
            String destAddr = selectedShipment.getDestination().getFullAddress();
            String customerName = selectedShipment.getUser() != null ? selectedShipment.getUser().getName() : "Cliente";
            
            // Construir ruta al archivo HTML local con el mapa Dijkstra
            String htmlPath = getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/webapp/route-map.html").toExternalForm();
            
            // Construir URL con parámetros
            String routeUrl = String.format(
                "%s?id=%s&customer=%s&originLat=%f&originLng=%f&originAddr=%s&destLat=%f&destLng=%f&destAddr=%s",
                htmlPath,
                selectedShipment.getId().toString(),
                java.net.URLEncoder.encode(customerName, "UTF-8"),
                originCoords[0], originCoords[1],
                java.net.URLEncoder.encode(originAddr, "UTF-8"),
                destCoords[0], destCoords[1],
                java.net.URLEncoder.encode(destAddr, "UTF-8")
            );
            
            System.out.println("🗺️ Abriendo ruta con algoritmo Dijkstra en navegador...");
            System.out.println("📦 Envío ID: " + selectedShipment.getId());
            System.out.println("📍 Origen: " + originAddr + " (" + originCoords[0] + ", " + originCoords[1] + ")");
            System.out.println("🎯 Destino: " + destAddr + " (" + destCoords[0] + ", " + destCoords[1] + ")");
            
            // Abrir en el navegador predeterminado
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(routeUrl));
                    AppUtils.showAlert("Ruta Calculada", "La ruta óptima se ha calculado y abierto en tu navegador", javafx.scene.control.Alert.AlertType.INFORMATION);
                } else {
                    AppUtils.showWarning("No soportado", "No se puede abrir el navegador automáticamente");
                }
            } else {
                AppUtils.showWarning("No soportado", "Tu sistema no soporta abrir URLs en el navegador");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al calcular ruta: " + e.getMessage());
            e.printStackTrace();
            AppUtils.showError("Error", "No se pudo calcular la ruta: " + e.getMessage());
        }
    }

    /**
     * Maneja el evento de marcar como entregado
     */
    @FXML
    private void handleMarkDelivered() {
        if (selectedShipment == null) {
            AppUtils.showWarning("Selección requerida", "Por favor seleccione un envío primero");
            return;
        }
        
        try {
            controller.getDelivererService().completeShipment(currentDeliverer, selectedShipment);
            AppUtils.showAlert("Éxito", "Envío marcado como entregado", javafx.scene.control.Alert.AlertType.INFORMATION);
            handleRefresh();
        } catch (Exception e) {
            AppUtils.showError("Error", "No se pudo marcar el envío: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maneja el evento de actualizar datos
     */
    @FXML
    private void handleRefresh() {
        // Recargar el repartidor desde el servicio para obtener datos actualizados
        currentDeliverer = controller.getDelivererService()
            .getRepository()
            .findById(currentDeliverer.getId())
            .orElse(currentDeliverer);
        
        Session.getInstance().setCurrentDeliverer(currentDeliverer);
        loadDelivererData();
        
        // Limpiar selección
        selectedShipment = null;
        listView_shipments.getSelectionModel().clearSelection();
    }

    /**
     * Maneja el evento de cerrar sesión
     */
    @FXML
    private void handleLogout() {
        Session.getInstance().logout();
        Platform.runLater(() -> {
            try {
                AppUtils.navigateTo("Login.fxml", btn_logout);
            } catch (Exception e) {
                AppUtils.showError("Error", "No se pudo cerrar sesión: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

}
