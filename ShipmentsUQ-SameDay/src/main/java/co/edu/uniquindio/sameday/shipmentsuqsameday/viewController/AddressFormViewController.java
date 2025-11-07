package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AddressFormController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.GridMapViewController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
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
    
    @FXML private StackPane mapContainer;
    @FXML private WebView wv_map; // Se mantiene para compatibilidad, pero no se usará
    
    // Controlador de negocio
    private AddressFormController controller;
    
    // Controlador del mapa
    private GridMapViewController mapViewController;
    
    // Referencia al controlador de ProfileAndAddresses para actualizar su vista
    private ProfileAndAddressesViewController parentController;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar controlador
            controller = new AddressFormController();
            
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
            
            // Obtener coordenadas seleccionadas
            double coordX = mapViewController.getSelectedX();
            double coordY = mapViewController.getSelectedY();
            
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
            
            if (success) {
                updateStatusMessage("Dirección guardada correctamente", false);
                
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
}