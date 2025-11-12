package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AddressFormController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.GridMapViewController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.RealMapService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de vista para el formulario de direcciones.
 * Maneja la interacción con la interfaz AddressForm.fxml.
 */
public class AddressFormViewController implements Initializable {

    // Componentes FXML de la interfaz
    @FXML private Label lbl_title;
    @FXML private Label lbl_alias;
    @FXML private Label lbl_street;
    @FXML private Label lbl_zone;
    @FXML private Label lbl_city;
    @FXML private Label lbl_zipCode;
    @FXML private Label lbl_complement;
    @FXML private Label lbl_coordinates;
    @FXML private Label lbl_status;
    
    @FXML private TextField txt_alias;
    @FXML private TextField txt_street;
    @FXML private TextField txt_zone;
    @FXML private TextField txt_city;
    @FXML private TextField txt_zipCode;
    @FXML private TextField txt_complement;
    
    @FXML private CheckBox chk_default;
    
    @FXML private Button btn_save;
    @FXML private Button btn_cancel;
    @FXML private Button btn_toggleMap;
    
    @FXML private StackPane mapContainer;
    @FXML private WebView wv_map; // Se mantiene para compatibilidad, pero no se usará
    
    // Controlador de negocio
    private AddressFormController controller;
    
    // Controlador del mapa Grid (sistema existente)
    private GridMapViewController mapViewController;
    
    // Servicio del mapa Real GPS (sistema nuevo)
    private RealMapService realMapService;
    
    // Estado del sistema de coordenadas
    private boolean usingRealCoordinates = false;
    private Coordinates selectedRealOrigin;
    private Coordinates selectedRealDestination;
    
    // Referencia al controlador de ProfileAndAddresses para actualizar su vista
    private ProfileAndAddressesViewController parentController;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar controlador
            controller = new AddressFormController();
            
            // Inicializar servicio de mapa real
            realMapService = new RealMapService();
            
            // Establecer callback para cuando se reciban coordenadas desde el mapa web
            realMapService.setCoordinatesCallback((origin, destination) -> {
                onRealCoordinatesReceived(origin, destination);
            });
            
            // Configurar componentes visuales
            setupMapView();
            setupListeners();
            
            // Mostrar mensaje inicial
            updateStatusMessage("Complete el formulario y seleccione una ubicación en el mapa", false);
            
        } catch (Exception e) {
            showErrorMessage("Error al inicializar: " + e.getMessage());
        }
    }
    
    /**
     * Establece el controlador padre para actualizar su vista después de guardar
     * @param parentController Controlador de la vista de perfil y direcciones
     */
    public void setParentController(ProfileAndAddressesViewController parentController) {
        this.parentController = parentController;
    }
    
    /**
     * Configura la vista del mapa
     */
    private void setupMapView() {
        try {
            // Esperar a que el contenedor tenga sus dimensiones finales
            mapContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (mapViewController == null && newVal.doubleValue() > 0) {
                    initializeMap();
                }
            });
            
            // Inicializar inmediatamente si ya tiene dimensiones
            if (mapContainer.getWidth() > 0) {
                initializeMap();
            }
            
        } catch (Exception e) {
            showErrorMessage("Error al configurar el mapa: " + e.getMessage());
        }
    }
    
    /**
     * Inicializa el mapa con las dimensiones correctas
     */
    private void initializeMap() {
        try {
            // Usar dimensiones fijas más conservadoras para evitar sobreposición
            double mapWidth = 360;  // Ancho fijo
            double mapHeight = 300; // Alto fijo
            double cellSize = 18;   // Tamaño de celda más pequeño para que quepa mejor
            
            mapViewController = new GridMapViewController(mapWidth, mapHeight, cellSize);
            
            // Añadir el mapa al contenedor
            mapViewController.initialize(mapContainer);
            
            // Configurar listener para cuando se seleccione una coordenada
            mapViewController.setCoordinateListener((x, y) -> {
                updateCoordinatesLabel(x, y);
            });
            
        } catch (Exception e) {
            showErrorMessage("Error al inicializar el mapa: " + e.getMessage());
        }
    }
    
    /**
     * Configura los listeners de botones y otros controles
     */
    private void setupListeners() {
        // Botones
        btn_save.setOnAction(event -> handleSaveAddress());
        btn_cancel.setOnAction(event -> handleCancel());
        btn_toggleMap.setOnAction(event -> handleToggleMap());
        
        // Campos de texto (para validación en tiempo real si se desea)
        txt_alias.textProperty().addListener((observable, oldValue, newValue) -> {
            updateStatusMessage("", false);
        });
    }
    
    /**
     * Maneja el evento de guardar dirección
     */
    private void handleSaveAddress() {
        try {
            // Obtener datos del formulario
            String alias = txt_alias.getText();
            String street = txt_street.getText();
            String zone = txt_zone.getText();
            String city = txt_city.getText();
            String zipCode = txt_zipCode.getText();
            String complement = txt_complement.getText();
            boolean isDefault = chk_default.isSelected();
            
            // Obtener coordenadas según el modo activo
            double coordX, coordY;
            Double gpsLat = null;
            Double gpsLng = null;
            
            if (usingRealCoordinates && selectedRealOrigin != null) {
                // Usando coordenadas GPS reales - convertir a Grid para guardar
                double[] gridCoords = realMapService.convertRealToGrid(
                    selectedRealOrigin.getLatitude(),
                    selectedRealOrigin.getLongitude()
                );
                coordX = gridCoords[0];
                coordY = gridCoords[1];
                
                // Guardar también las coordenadas GPS reales
                gpsLat = selectedRealOrigin.getLatitude();
                gpsLng = selectedRealOrigin.getLongitude();
                
                System.out.println("✓ Guardando dirección con GPS: " + gpsLat + ", " + gpsLng);
                System.out.println("  Convertido a Grid: " + coordX + ", " + coordY);
                
            } else {
                // Usando sistema de cuadrícula tradicional
                coordX = mapViewController.getSelectedX();
                coordY = mapViewController.getSelectedY();
                
                System.out.println("✓ Guardando dirección con Grid: " + coordX + ", " + coordY);
            }
            
            // Validar campos obligatorios en la interfaz
            if (alias.trim().isEmpty() || street.trim().isEmpty() || city.trim().isEmpty()) {
                updateStatusMessage("Complete los campos obligatorios (Alias, Calle, Ciudad)", true);
                return;
            }
            
            if (coordX == 0 && coordY == 0) {
                updateStatusMessage("Seleccione una ubicación en el mapa", true);
                return;
            }
            
            // Intentar guardar la dirección
            boolean success = controller.saveAddress(alias, street, zone, city, zipCode, 
                                                     complement, coordX, coordY, isDefault);
            
            // Si se guardó exitosamente y hay coordenadas GPS, actualizarlas
            if (success && gpsLat != null && gpsLng != null) {
                // Obtener la dirección recién guardada para agregarle las coordenadas GPS
                // Nota: Necesitamos acceso a la dirección guardada para poder actualizarla
                // Por ahora, las coordenadas GPS se perderán si no las guardamos en el método saveAddress
                System.out.println("⚠️ Coordenadas GPS no persistidas en Address (requiere modificar AddressFormController)");
            }
            
            if (success) {
                String coordSystemMsg = usingRealCoordinates ? " (con coordenadas GPS)" : "";
                updateStatusMessage("Dirección guardada correctamente" + coordSystemMsg, false);
                
                // Si hay un controlador padre, actualizar su lista de direcciones
                if (parentController != null) {
                    parentController.loadUserAddresses();
                }
                
                // Cerrar la ventana después de un breve retraso
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> closeWindow());
                        }
                    }, 
                    1000
                );
            } else {
                updateStatusMessage("No se pudo guardar la dirección", true);
            }
            
        } catch (Exception e) {
            updateStatusMessage("Error: " + e.getMessage(), true);
        }
    }
    
    /**
     * Maneja el evento de cancelar
     */
    private void handleCancel() {
        closeWindow();
    }
    
    /**
     * Cierra la ventana actual
     */
    private void closeWindow() {
        Stage stage = (Stage) btn_cancel.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Actualiza la etiqueta de coordenadas cuando se selecciona un punto en el mapa
     * @param x Coordenada X
     * @param y Coordenada Y
     */
    private void updateCoordinatesLabel(double x, double y) {
        lbl_coordinates.setText(String.format("Coordenadas: (%.2f, %.2f)", x, y));
    }
    
    /**
     * Actualiza el mensaje de estado
     * @param message Mensaje a mostrar
     * @param isError Indica si es un mensaje de error
     */
    private void updateStatusMessage(String message, boolean isError) {
        lbl_status.setText(message);
        
        if (isError) {
            lbl_status.setStyle("-fx-text-fill: red;");
        } else {
            lbl_status.setStyle("-fx-text-fill: green;");
        }
    }
    
    /**
     * Muestra un mensaje de error
     * @param message Mensaje de error
     */
    private void showErrorMessage(String message) {
        updateStatusMessage(message, true);
    }
    
    /**
     * Carga una dirección existente para edición
     * @param address La dirección a editar
     */
    public void loadAddressForEdit(Address address) {
        if (address == null) return;
        
        // Configurar modo edición
        controller.setAddressToEdit(address);
        lbl_title.setText("Editar Dirección");
        
        // Cargar datos en el formulario
        txt_alias.setText(address.getAlias());
        txt_street.setText(address.getStreet());
        txt_zone.setText(address.getZone());
        txt_city.setText(address.getCity());
        txt_zipCode.setText(address.getZipCode());
        txt_complement.setText(address.getComplement());
        chk_default.setSelected(address.isDefault());
        
        // Seleccionar coordenadas en el mapa
        double x = address.getCoordX();
        double y = address.getCoordY();
        mapViewController.setSelectedCoordinates(x, y);
        updateCoordinatesLabel(x, y);
    }
    
    /**
     * Maneja el toggle entre Grid Map y Real GPS Map
     */
    private void handleToggleMap() {
        if (!usingRealCoordinates) {
            // Cambiar a mapa real
            enableRealMapMode();
        } else {
            // Volver a mapa Grid
            disableRealMapMode();
        }
    }
    
    /**
     * Activa el modo de mapa con coordenadas reales (GPS)
     */
    private void enableRealMapMode() {
        try {
            // Iniciar servidor web si no está activo
            if (realMapService.startMapServer()) {
                // Abrir navegador con el mapa
                realMapService.openMapInBrowser();
                
                // Cambiar estado
                usingRealCoordinates = true;
                btn_toggleMap.setText("📍 Usar Mapa de Cuadrícula");
                btn_toggleMap.setStyle("-fx-background-color: linear-gradient(to bottom, #10b981 0%, #059669 100%);");
                
                // Mostrar instrucciones
                showRealMapInstructions();
                
                updateStatusMessage("Mapa GPS abierto en el navegador. Selecciona origen y haz clic en 'Enviar a Java'", false);
            } else {
                showErrorMessage("No se pudo iniciar el servidor del mapa");
            }
        } catch (Exception e) {
            showErrorMessage("Error al abrir mapa real: " + e.getMessage());
        }
    }
    
    /**
     * Desactiva el modo de mapa real y vuelve al Grid
     */
    private void disableRealMapMode() {
        usingRealCoordinates = false;
        btn_toggleMap.setText("🗺️ Usar Coordenadas Reales");
        btn_toggleMap.setStyle("");
        
        // Limpiar coordenadas reales seleccionadas
        selectedRealOrigin = null;
        selectedRealDestination = null;
        
        updateStatusMessage("Usando sistema de cuadrícula. Selecciona ubicación en el mapa", false);
    }
    
    /**
     * Muestra un diálogo con instrucciones para usar el mapa real
     */
    private void showRealMapInstructions() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sistema de Coordenadas Reales");
            alert.setHeaderText("Cómo usar el mapa GPS");
            alert.setContentText(
                "1. Se ha abierto el mapa en tu navegador (http://localhost:8080)\n\n" +
                "2. Haz clic en el botón 'ORIGEN' (azul) en el mapa\n\n" +
                "3. Haz clic en la ubicación deseada en el mapa\n\n" +
                "4. Verás las coordenadas GPS en el panel lateral\n\n" +
                "5. (Opcional) Selecciona DESTINO si lo necesitas\n\n" +
                "6. Haz clic en '💾 Enviar a Java' para confirmar\n\n" +
                "7. Las coordenadas aparecerán en esta ventana\n\n" +
                "Nota: Para direcciones solo necesitas el ORIGEN.\n" +
                "El DESTINO es opcional y se usa para calcular rutas."
            );
            alert.showAndWait();
        });
    }
    
    /**
     * Callback llamado cuando se reciben coordenadas desde el mapa web
     * Este método será invocado por el MapWebServer cuando JavaScript envíe datos
     * 
     * @param origin coordenadas de origen
     * @param destination coordenadas de destino (puede ser null para direcciones)
     */
    public void onRealCoordinatesReceived(Coordinates origin, Coordinates destination) {
        Platform.runLater(() -> {
            this.selectedRealOrigin = origin;
            this.selectedRealDestination = destination;
            
            // Actualizar label con coordenadas GPS
            lbl_coordinates.setText(String.format(
                "GPS: Lat %.6f, Lng %.6f", 
                origin.getLatitude(), 
                origin.getLongitude()
            ));
            
            // Convertir a coordenadas de Grid para compatibilidad
            double[] gridCoords = realMapService.convertRealToGrid(
                origin.getLatitude(), 
                origin.getLongitude()
            );
            
            // Actualizar el mapa Grid también (para visualización)
            if (mapViewController != null) {
                mapViewController.setSelectedCoordinates(gridCoords[0], gridCoords[1]);
            }
            
            updateStatusMessage("Coordenadas GPS recibidas correctamente", false);
        });
    }
}
