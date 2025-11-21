package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.RegisterController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

/**
 * Controlador de vista para la pantalla de registro de usuarios.
 * Maneja la interacción del usuario con la interfaz de registro.
 */
public class RegisterViewController implements Initializable {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Button btn_register;
    @FXML
    private Button btn_cancel;
    @FXML
    private CheckBox chb_terms;
    @FXML
    private ImageView img_register;
    @FXML
    private Label lbl_city;
    @FXML
    private Label lbl_confirmPassword;
    @FXML
    private Label lbl_email;
    @FXML
    private Label lbl_name;
    @FXML
    private Label lbl_password;
    @FXML
    private Label lbl_phone;
    @FXML
    private Label lbl_document;
    @FXML
    private Label lbl_zone;
    @FXML
    private Label lbl_location;
    @FXML
    private Label lbl_locationStatus;
    @FXML
    private Label lbl_userType;
    @FXML
    private Label lbl_status;
    @FXML
    private Label lbl_title;
    @FXML
    private PasswordField txtp_confirmPassword;
    @FXML
    private PasswordField txtp_password;
    @FXML
    private TextField txt_city;
    @FXML
    private TextField txt_email;
    @FXML
    private TextField txt_name;
    @FXML
    private TextField txt_phone;
    @FXML
    private TextField txt_document;
    @FXML
    private TextField txt_zone;
    @FXML
    private Button btn_selectLocation;
    @FXML
    private RadioButton rb_user;
    @FXML
    private RadioButton rb_deliverer;
    @FXML
    private ToggleGroup tg_userType;

    // Controlador de negocio
    private RegisterController controller;
    
    // Coordenadas GPS seleccionadas
    private Double selectedLat = null;
    private Double selectedLng = null;

    /**
     * Inicializa el controlador de vista.
     * Este método se llama automáticamente después de cargar el archivo FXML.
     * 
     * @param location  La ubicación utilizada para resolver rutas relativas
     * @param resources Los recursos utilizados para localizar el objeto raíz
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar componentes y controladores
        initController();
        initButtonListeners();
        initFormValidation();
        initUserTypeListener();

        // Configuración inicial de la interfaz
        clearStatus();
        Platform.runLater(() -> txt_name.requestFocus());
    }

    /**
     * Inicializa el controlador de negocio
     */
    private void initController() {
        controller = new RegisterController();
    }

    /**
     * Configura los listeners de los botones
     */
    private void initButtonListeners() {
        btn_register.setOnAction(this::handleRegister);
        btn_cancel.setOnAction(this::handleCancel);
        btn_selectLocation.setOnAction(this::handleSelectLocation);
    }

    /**
     * Configura la validación de campos del formulario
     */
    private void initFormValidation() {
        // Limpiar el estado cuando cambie cualquier campo
        txt_name.textProperty().addListener((observable, oldValue, newValue) -> clearStatus());
        txt_email.textProperty().addListener((observable, oldValue, newValue) -> clearStatus());
        txt_phone.textProperty().addListener((observable, oldValue, newValue) -> clearStatus());
        txt_document.textProperty().addListener((observable, oldValue, newValue) -> clearStatus());
        txt_zone.textProperty().addListener((observable, oldValue, newValue) -> clearStatus());
        txtp_password.textProperty().addListener((observable, oldValue, newValue) -> clearStatus());
        txtp_confirmPassword.textProperty().addListener((observable, oldValue, newValue) -> clearStatus());
        txt_city.textProperty().addListener((observable, oldValue, newValue) -> clearStatus());

        // Habilitar/deshabilitar el botón de registro según el checkbox de términos
        chb_terms.selectedProperty().addListener((observable, oldValue, newValue) -> {
            btn_register.setDisable(!newValue);
        });

        // Inicialmente, el botón de registro está deshabilitado hasta aceptar términos
        btn_register.setDisable(true);
    }

    /**
     * Configura el listener para el tipo de usuario (Usuario/Repartidor)
     */
    private void initUserTypeListener() {
        tg_userType.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == rb_deliverer) {
                showDelivererFields();
            } else {
                hideDelivererFields();
            }
        });
    }

    /**
     * Muestra los campos específicos para repartidores
     */
    private void showDelivererFields() {
        lbl_document.setVisible(true);
        lbl_document.setManaged(true);
        txt_document.setVisible(true);
        txt_document.setManaged(true);
        
        lbl_zone.setVisible(true);
        lbl_zone.setManaged(true);
        txt_zone.setVisible(true);
        txt_zone.setManaged(true);
        
        lbl_location.setVisible(true);
        lbl_location.setManaged(true);
        btn_selectLocation.getParent().setVisible(true);
        btn_selectLocation.getParent().setManaged(true);
    }

    /**
     * Oculta los campos específicos para repartidores
     */
    private void hideDelivererFields() {
        lbl_document.setVisible(false);
        lbl_document.setManaged(false);
        txt_document.setVisible(false);
        txt_document.setManaged(false);
        txt_document.clear();
        
        lbl_zone.setVisible(false);
        lbl_zone.setManaged(false);
        txt_zone.setVisible(false);
        txt_zone.setManaged(false);
        txt_zone.clear();
        
        lbl_location.setVisible(false);
        lbl_location.setManaged(false);
        btn_selectLocation.getParent().setVisible(false);
        btn_selectLocation.getParent().setManaged(false);
        selectedLat = null;
        selectedLng = null;
        lbl_locationStatus.setText("No seleccionada");
    }

    /**
     * Maneja el evento de clic en el botón de registro
     * 
     * @param event El evento de acción
     */
    private void handleRegister(ActionEvent event) {
        // Obtener datos comunes del formulario
        String name = txt_name.getText().trim();
        String email = txt_email.getText().trim();
        String phone = txt_phone.getText().trim();
        String password = txtp_password.getText();
        String confirmPassword = txtp_confirmPassword.getText();
        String city = txt_city.getText().trim();
        boolean termsAccepted = chb_terms.isSelected();
        boolean isDeliverer = rb_deliverer.isSelected();

        // Validar campos comunes
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty() || city.isEmpty()) {
            showErrorStatus("Por favor complete todos los campos");
            return;
        }

        // Si es repartidor, validar campos adicionales
        if (isDeliverer) {
            String document = txt_document.getText().trim();
            String zone = txt_zone.getText().trim();
            
            if (document.isEmpty() || zone.isEmpty()) {
                showErrorStatus("Por favor complete el documento y zona de trabajo");
                return;
            }
            
            if (selectedLat == null || selectedLng == null) {
                showErrorStatus("Por favor seleccione su ubicación inicial en el mapa");
                return;
            }
        }

        // Validar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            showErrorStatus("Las contraseñas no coinciden");
            return;
        }

        // Validar aceptación de términos
        if (!termsAccepted) {
            showErrorStatus("Debe aceptar los términos y condiciones");
            return;
        }

        try {
            if (isDeliverer) {
                // Registrar repartidor
                String document = txt_document.getText().trim();
                String zone = txt_zone.getText().trim();
                controller.registerDeliverer(name, email, phone, password, city, document, zone, 
                                            selectedLat, selectedLng);
                
                // Mostrar mensaje de éxito
                showSuccessStatus("Repartidor registrado exitosamente");
                
                // Mostrar alerta y redirigir
                AppUtils.showAlertAndRedirect(
                        "Registro exitoso",
                        "Tu cuenta de repartidor ha sido creada. Ahora puedes iniciar sesión en el portal de repartidores.",
                        AlertType.INFORMATION,
                        "Login");
            } else {
                // Registrar usuario normal
                controller.registerUser(name, email, phone, password, city);
                
                // Mostrar mensaje de éxito
                showSuccessStatus("Usuario registrado exitosamente");
                
                // Mostrar alerta y redirigir
                AppUtils.showAlertAndRedirect(
                        "Registro exitoso",
                        "Tu cuenta ha sido creada. Ahora puedes iniciar sesión con tus credenciales.",
                        AlertType.INFORMATION,
                        "Login");
            }

        } catch (IllegalArgumentException e) {
            // Error específico de validación
            showErrorStatus("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            // Otros errores inesperados
            showErrorStatus("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maneja el evento de clic en el botón de cancelar
     * 
     * @param event El evento de acción
     */
    private void handleCancel(ActionEvent event) {
        try {
            // Navegar de vuelta a la pantalla de inicio de sesión
            AppUtils.navigateTo("Login.fxml", btn_cancel);
        } catch (Exception e) {
            showErrorStatus("Error al volver a la pantalla de inicio de sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje de error en la interfaz
     * 
     * @param message El mensaje a mostrar
     */
    private void showErrorStatus(String message) {
        lbl_status.setText(message);
        lbl_status.getStyleClass().removeAll("success-message");
        lbl_status.getStyleClass().add("error-message");
        lbl_status.setVisible(true);
    }

    /**
     * Muestra un mensaje de éxito en la interfaz
     * 
     * @param message El mensaje a mostrar
     */
    private void showSuccessStatus(String message) {
        lbl_status.setText(message);
        lbl_status.getStyleClass().removeAll("error-message");
        lbl_status.getStyleClass().add("success-message");
        lbl_status.setVisible(true);
    }

    /**
     * Limpia el mensaje de estado de la interfaz
     */
    private void clearStatus() {
        lbl_status.setText("");
        lbl_status.setVisible(false);
    }
    
    /**
     * Maneja el evento de seleccionar ubicación en el mapa
     * 
     * @param event El evento de acción
     */
    private void handleSelectLocation(ActionEvent event) {
        try {
            // Crear archivo HTML temporal para selección de ubicación
            Path tempFile = Files.createTempFile("location_picker_", ".html");
            String htmlContent = generateLocationPickerHtml();
            
            try (FileWriter writer = new FileWriter(tempFile.toFile())) {
                writer.write(htmlContent);
            }
            
            // Abrir en el navegador
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(tempFile.toUri());
                
                // Iniciar thread para monitorear el archivo de coordenadas
                startLocationMonitoring();
            } else {
                showErrorStatus("No se puede abrir el navegador en este sistema");
            }
            
        } catch (Exception e) {
            showErrorStatus("Error al abrir selector de ubicación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Genera el HTML para el selector de ubicación con mapa
     */
    private String generateLocationPickerHtml() {
        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Seleccionar Ubicación Inicial</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; height: 100vh; display: flex; flex-direction: column; }
        #map { flex: 1; }
        .info-panel { position: absolute; top: 20px; right: 20px; background: white; padding: 20px; border-radius: 12px; 
                      box-shadow: 0 4px 20px rgba(0,0,0,0.15); z-index: 1000; max-width: 350px; }
        .info-panel h2 { font-size: 20px; margin-bottom: 15px; color: #1f2937; }
        .coordinates { background: #fef3c7; padding: 15px; border-radius: 8px; margin: 15px 0; }
        .coordinates p { margin: 5px 0; color: #92400e; }
        .btn-confirm { background: #10b981; color: white; border: none; padding: 12px 24px; border-radius: 8px; 
                       font-size: 16px; font-weight: 600; cursor: pointer; width: 100%; }
        .btn-confirm:hover { background: #059669; }
        .btn-confirm:disabled { background: #9ca3af; cursor: not-allowed; }
        .instructions { color: #6b7280; font-size: 14px; margin: 10px 0; }
    </style>
</head>
<body>
    <div id="map"></div>
    <div class="info-panel">
        <h2>📍 Selecciona tu Ubicación</h2>
        <p class="instructions">Haz clic en el mapa para seleccionar tu ubicación inicial como repartidor</p>
        <div class="coordinates">
            <p><strong>Latitud:</strong> <span id="lat">-</span></p>
            <p><strong>Longitud:</strong> <span id="lng">-</span></p>
        </div>
        <button id="btn-confirm" class="btn-confirm" disabled>Confirmar Ubicación</button>
    </div>
    
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
        // Inicializar mapa centrado en Armenia, Quindío
        const map = L.map('map').setView([4.533889, -75.681111], 14);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors',
            maxZoom: 19
        }).addTo(map);
        
        let selectedMarker = null;
        let selectedLat = null;
        let selectedLng = null;
        
        // Manejar clic en el mapa
        map.on('click', function(e) {
            selectedLat = e.latlng.lat;
            selectedLng = e.latlng.lng;
            
            // Actualizar coordenadas en UI
            document.getElementById('lat').textContent = selectedLat.toFixed(6);
            document.getElementById('lng').textContent = selectedLng.toFixed(6);
            document.getElementById('btn-confirm').disabled = false;
            
            // Remover marcador anterior si existe
            if (selectedMarker) {
                map.removeLayer(selectedMarker);
            }
            
            // Agregar nuevo marcador
            selectedMarker = L.marker([selectedLat, selectedLng], {
                icon: L.divIcon({
                    className: 'custom-marker',
                    html: '<div style="background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); color: white; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; border: 3px solid white; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">🚴</div>',
                    iconSize: [40, 40]
                })
            }).addTo(map).bindPopup('<b>Tu ubicación inicial</b>').openPopup();
        });
        
        // Confirmar ubicación
        document.getElementById('btn-confirm').addEventListener('click', function() {
            if (selectedLat && selectedLng) {
                // Cambiar el título de la página para que Java pueda leerlo
                document.title = 'COORDS:' + selectedLat.toFixed(6) + ',' + selectedLng.toFixed(6);
                
                // Guardar en localStorage como respaldo
                localStorage.setItem('shipmentsuq_deliverer_location', JSON.stringify({
                    lat: selectedLat,
                    lng: selectedLng,
                    timestamp: Date.now()
                }));
                
                // Mostrar mensaje de confirmación
                alert('✅ Ubicación confirmada correctamente\\n\\nLatitud: ' + selectedLat.toFixed(6) + '\\nLongitud: ' + selectedLng.toFixed(6) + '\\n\\nPuedes cerrar esta ventana ahora.');
                
                // Cerrar automáticamente después de 2 segundos
                setTimeout(() => {
                    window.close();
                }, 2000);
            }
        });
    </script>
</body>
</html>
                """;
    }
    
    /**
     * Inicia el monitoreo para verificar si se seleccionó una ubicación
     */
    private void startLocationMonitoring() {
        Platform.runLater(() -> {
            lbl_locationStatus.setText("Esperando selección...");
            
            // Mostrar diálogo modal para ingresar coordenadas después de seleccionar
            Dialog<Pair<Double, Double>> dialog = new Dialog<>();
            dialog.setTitle("Ubicación Seleccionada");
            dialog.setHeaderText("Ingresa las coordenadas que aparecen en el mapa");
            
            // Botones
            ButtonType confirmButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
            
            // Campos de entrada
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            
            TextField latField = new TextField();
            latField.setPromptText("Ejemplo: 4.533889");
            TextField lngField = new TextField();
            lngField.setPromptText("Ejemplo: -75.681111");
            
            grid.add(new Label("Latitud:"), 0, 0);
            grid.add(latField, 1, 0);
            grid.add(new Label("Longitud:"), 0, 1);
            grid.add(lngField, 1, 1);
            
            dialog.getDialogPane().setContent(grid);
            
            // Solicitar foco en el primer campo
            Platform.runLater(() -> latField.requestFocus());
            
            // Convertir resultado cuando se presiona confirmar
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    try {
                        double lat = Double.parseDouble(latField.getText());
                        double lng = Double.parseDouble(lngField.getText());
                        return new Pair<>(lat, lng);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            });
            
            // Mostrar diálogo y procesar resultado
            dialog.showAndWait().ifPresent(coords -> {
                if (coords != null) {
                    selectedLat = coords.getKey();
                    selectedLng = coords.getValue();
                    
                    lbl_locationStatus.setText(String.format("✓ Lat: %.6f, Lng: %.6f", 
                        selectedLat, selectedLng));
                    lbl_locationStatus.setStyle("-fx-text-fill: green;");
                }
            });
        });
    }
}