package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AddressFormController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.GridMapViewController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.RealMapService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.ReverseGeocoder;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Map;
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
    
    // Coordenadas pendientes de establecer en el mapa
    private Double pendingCoordX;
    private Double pendingCoordY;
    
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
            
            // Si hay coordenadas pendientes, establecerlas ahora
            if (pendingCoordX != null && pendingCoordY != null) {
                mapViewController.setSelectedCoordinates(pendingCoordX, pendingCoordY);
                pendingCoordX = null;
                pendingCoordY = null;
            }
            
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
                
                System.out.println("[SUCCESS] Guardando dirección con GPS: " + gpsLat + ", " + gpsLng);
                System.out.println("  Convertido a Grid: " + coordX + ", " + coordY);
                
            } else {
                // Usando sistema de cuadrícula tradicional
                coordX = mapViewController.getSelectedX();
                coordY = mapViewController.getSelectedY();
                
                System.out.println("[SUCCESS] Guardando dirección con Grid: " + coordX + ", " + coordY);
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
                System.out.println("[WARN] Coordenadas GPS no persistidas en Address (requiere modificar AddressFormController)");
            }
            
            if (success) {
                String coordSystemMsg = usingRealCoordinates ? " (con coordenadas GPS)" : "";
                updateStatusMessage("Dirección guardada correctamente" + coordSystemMsg, false);
                
                // Si hay un controlador padre, actualizar su lista de direcciones
                if (parentController != null) {
                    parentController.loadUserAddresses();
                }
                
                // Si se usaron coordenadas reales, cerrar el servidor del mapa
                if (usingRealCoordinates && realMapService != null) {
                    System.out.println("[INFO] Cerrando servidor de mapas después de guardar dirección");
                    realMapService.stopMapServer();
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
        // Si se estaba usando el mapa GPS, detener el servidor
        if (usingRealCoordinates && realMapService != null) {
            System.out.println("[INFO] Cancelando - Cerrando servidor de mapas");
            realMapService.stopMapServer();
        }
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
        
        // Obtener coordenadas
        double x = address.getCoordX();
        double y = address.getCoordY();
        
        // Si el mapa ya está inicializado, establecer coordenadas directamente
        if (mapViewController != null) {
            mapViewController.setSelectedCoordinates(x, y);
        } else {
            // Si no está inicializado, guardar para establecer después
            pendingCoordX = x;
            pendingCoordY = y;
        }
        
        // Actualizar etiqueta de coordenadas
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
                // Abrir navegador con el mapa ESPECIALIZADO para direcciones (1 punto)
                realMapService.openMapInBrowser(RealMapService.MapType.ADDRESS);
                
                // Cambiar estado
                usingRealCoordinates = true;
                btn_toggleMap.setText("📍 Usar Mapa de Cuadrícula");
                btn_toggleMap.setStyle("-fx-background-color: linear-gradient(to bottom, #10b981 0%, #059669 100%);");
                
                // Mostrar instrucciones
                showRealMapInstructions();
                
                updateStatusMessage("Mapa GPS abierto - Selecciona la ubicación de la dirección", false);
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
        btn_toggleMap.setText("Usar Coordenadas Reales");
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
            alert.setTitle("Seleccionar Ubicación de Dirección");
            alert.setHeaderText("Cómo usar el mapa GPS");
            alert.setContentText(
                "INSTRUCCIONES PARA DIRECCIONES:\n\n" +
                "1. Se abrió el mapa en tu navegador (http://localhost:8080/address-map.html)\n\n" +
                "2. Haz clic en el mapa en la ubicación exacta de la dirección\n\n" +
                "3. Puedes arrastrar el marcador para ajustar la posición\n\n" +
                "4. Verás las coordenadas GPS en el panel lateral\n\n" +
                "5. Haz clic en 'Guardar Ubicación' para confirmar\n\n" +
                "6. Las coordenadas y la dirección aparecerán aquí automáticamente\n\n" +
                "IMPORTANTE:\n" +
                "• Solo necesitas seleccionar 1 punto (la dirección)\n" +
                "• Debe estar dentro del círculo morado (área de servicio)\n" +
                "• Los campos de dirección se auto-rellenarán con los datos GPS"
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
        System.out.println("\n[INFO] Callback: onRealCoordinatesReceived() executed");
        System.out.println("  Origin: " + (origin != null ? 
            String.format("Lat %.6f, Lng %.6f", origin.getLatitude(), origin.getLongitude()) : "null"));
        System.out.println("  Destination: " + (destination != null ? 
            String.format("Lat %.6f, Lng %.6f", destination.getLatitude(), destination.getLongitude()) : "null (normal)"));
        
        Platform.runLater(() -> {
            this.selectedRealOrigin = origin;
            this.selectedRealDestination = destination;
            
            // Auto-fill address from GPS coordinates
            if (origin != null) {
                System.out.println("[INFO] Calling autoFillAddressFromGPS()...");
                autoFillAddressFromGPS(origin.getLatitude(), origin.getLongitude());
            } else {
                System.err.println("[ERROR] Origin is null, cannot auto-fill");
            }
            
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
            
            System.out.println("[INFO] Coordenadas convertidas a Grid: [" + 
                String.format("%.2f, %.2f", gridCoords[0], gridCoords[1]) + "]");
            
            // Actualizar el mapa Grid también (para visualización)
            if (mapViewController != null) {
                mapViewController.setSelectedCoordinates(gridCoords[0], gridCoords[1]);
                System.out.println("[SUCCESS] Mapa Grid actualizado");
            }
            
            updateStatusMessage("Coordenadas GPS recibidas correctamente", false);
        });
    }
    
    /**
     * Auto-rellena los campos de dirección usando geocodificación inversa
     */
    private void autoFillAddressFromGPS(double latitude, double longitude) {
        System.out.println("=".repeat(80));
        System.out.println("[AUTO-FILL] Iniciando autorellenado desde coordenadas GPS");
        System.out.println("  Latitud:  " + String.format("%.6f", latitude));
        System.out.println("  Longitud: " + String.format("%.6f", longitude));
        System.out.println("=".repeat(80));
        
        // Mostrar mensaje de carga
        Platform.runLater(() -> {
            updateStatusMessage("Buscando dirección para las coordenadas GPS...", false);
        });
        
        // Ejecutar en hilo separado para no bloquear UI
        new Thread(() -> {
            try {
                System.out.println("[1/3] Llamando a ReverseGeocoder...");
                ReverseGeocoder geocoder = ReverseGeocoder.getInstance();
                java.util.Map<String, String> addressComponents = geocoder.reverseGeocode(latitude, longitude);
                
                if (addressComponents == null || addressComponents.isEmpty()) {
                    System.err.println("[ERROR] No se obtuvieron componentes de dirección");
                    Platform.runLater(() -> {
                        updateStatusMessage("No se encontró dirección para estas coordenadas", true);
                    });
                    return;
                }
                
                System.out.println("[2/3] Componentes recibidos (" + addressComponents.size() + "):");
                addressComponents.forEach((key, value) -> 
                    System.out.println("      " + key + " = " + value)
                );
                
                // Extraer componentes relevantes con fallbacks
                String street = getAddressComponent(addressComponents, "road", "street", "path");
                String neighbourhood = getAddressComponent(addressComponents, "neighbourhood", "suburb", "district");
                String city = getAddressComponent(addressComponents, "city", "town", "municipality");
                String postcode = addressComponents.get("postcode");
                
                System.out.println("[3/3] Valores extraídos:");
                System.out.println("      Calle: " + (street != null ? street : "(no disponible)"));
                System.out.println("      Barrio: " + (neighbourhood != null ? neighbourhood : "(no disponible)"));
                System.out.println("      Ciudad: " + (city != null ? city : "(no disponible)"));
                System.out.println("      Código Postal: " + (postcode != null ? postcode : "(no disponible)"));
                
                // Actualizar UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    try {
                        System.out.println("[UI] Actualizando campos del formulario...");
                        
                        int fieldsUpdated = 0;
                        
                        // Actualizar cada campo si hay datos
                        if (street != null && !street.trim().isEmpty()) {
                            txt_street.setText(street);
                            txt_street.positionCaret(street.length());
                            fieldsUpdated++;
                            System.out.println("  [OK] Calle actualizada: " + street);
                        }
                        
                        if (neighbourhood != null && !neighbourhood.trim().isEmpty()) {
                            txt_zone.setText(neighbourhood);
                            txt_zone.positionCaret(neighbourhood.length());
                            fieldsUpdated++;
                            System.out.println("  [OK] Barrio actualizado: " + neighbourhood);
                        }
                        
                        if (city != null && !city.trim().isEmpty()) {
                            txt_city.setText(city);
                            txt_city.positionCaret(city.length());
                            fieldsUpdated++;
                            System.out.println("  [OK] Ciudad actualizada: " + city);
                        }
                        
                        if (postcode != null && !postcode.trim().isEmpty()) {
                            txt_zipCode.setText(postcode);
                            txt_zipCode.positionCaret(postcode.length());
                            fieldsUpdated++;
                            System.out.println("  [OK] Código Postal actualizado: " + postcode);
                        }
                        
                        // Construir dirección legible
                        String formatted = geocoder.formatColombianAddress(addressComponents);
                        
                        if (fieldsUpdated > 0) {
                            updateStatusMessage("Dirección autocompletada (" + fieldsUpdated + " campos): " + formatted, false);
                            System.out.println("[SUCCESS] Autorellenado completado exitosamente");
                            System.out.println("  Campos actualizados: " + fieldsUpdated);
                            System.out.println("  Dirección formateada: " + formatted);
                        } else {
                            updateStatusMessage("No se pudo extraer información de dirección", true);
                            System.err.println("[WARN] No se actualizó ningún campo");
                        }
                        
                        System.out.println("=".repeat(80));
                        
                    } catch (Exception uiError) {
                        System.err.println("[ERROR] Error al actualizar UI:");
                        uiError.printStackTrace();
                        updateStatusMessage("Error al actualizar formulario", true);
                    }
                });
                
            } catch (Exception e) {
                System.err.println("[ERROR] Error en geocodificación inversa:");
                System.err.println("  Tipo: " + e.getClass().getSimpleName());
                System.err.println("  Mensaje: " + e.getMessage());
                e.printStackTrace();
                System.out.println("=".repeat(80));
                
                Platform.runLater(() -> {
                    updateStatusMessage("Error al obtener dirección: " + e.getMessage(), true);
                });
            }
        }).start();
    }
    
    /**
     * Obtiene un componente de dirección con múltiples claves de fallback
     */
    private String getAddressComponent(Map<String, String> components, String... keys) {
        for (String key : keys) {
            String value = components.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }
}
