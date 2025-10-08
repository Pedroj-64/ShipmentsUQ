package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.ProfileController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controlador de vista para el perfil de usuario.
 * Maneja la presentación y edición de información personal.
 */
public class ProfileViewController implements Initializable {

    @FXML private VBox profileContainer;
    @FXML private Label lbl_title;
    @FXML private Label lbl_message;
    @FXML private Label lbl_defaultAddress;
    @FXML private TextField txt_name;
    @FXML private TextField txt_email;
    @FXML private TextField txt_phone;
    @FXML private Button btn_changePassword;
    @FXML private Button btn_editAddress;
    @FXML private Button btn_cancel;
    @FXML private Button btn_save;
    
    // Controlador de negocio
    private ProfileController controller;
    
    // Usuario actual
    private UserDTO currentUser;
    
    // Dirección predeterminada
    private AddressDTO defaultAddress;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar controlador y componentes
        initController();
        setupUIControls();
        
        // Cargar datos del usuario
        loadUserData();
    }
    
    /**
     * Inicializa el controlador de negocio
     */
    private void initController() {
        controller = new ProfileController();
        try {
            // Obtener datos del usuario y su dirección predeterminada
            currentUser = controller.getCurrentUser();
            defaultAddress = controller.getDefaultAddress();
        } catch (Exception e) {
            showErrorMessage("Error al cargar datos: " + e.getMessage());
        }
    }
    
    /**
     * Configura los controles de la interfaz
     */
    private void setupUIControls() {
        // Configurar manejadores de eventos para botones
        btn_save.setOnAction(event -> handleSave());
        btn_cancel.setOnAction(event -> handleCancel());
        btn_changePassword.setOnAction(event -> handleChangePassword());
        btn_editAddress.setOnAction(event -> handleEditAddress());
        
        // Limpiar mensajes
        clearMessage();
    }
    
    /**
     * Carga los datos del usuario en el formulario
     */
    private void loadUserData() {
        if (currentUser == null) {
            showErrorMessage("No se pudo cargar la información del usuario");
            return;
        }
        
        // Cargar datos básicos
        txt_name.setText(currentUser.getName());
        txt_email.setText(currentUser.getEmail());
        txt_phone.setText(currentUser.getPhone());
        
        // Mostrar dirección predeterminada
        if (defaultAddress != null) {
            lbl_defaultAddress.setText(defaultAddress.getStreet() + ", " + 
                defaultAddress.getZone() + ", " + defaultAddress.getCity());
        } else {
            lbl_defaultAddress.setText("No hay dirección predeterminada");
        }
    }
    
    /**
     * Maneja el guardado de cambios
     */
    private void handleSave() {
        try {
            // Validar datos
            if (txt_name.getText().isEmpty() || txt_email.getText().isEmpty() || txt_phone.getText().isEmpty()) {
                showErrorMessage("Todos los campos son obligatorios");
                return;
            }
            
            // Actualizar datos en el modelo
            boolean success = controller.updateUserInfo(
                txt_name.getText(),
                txt_email.getText(),
                txt_phone.getText()
            );
            
            if (success) {
                showSuccessMessage("Información actualizada correctamente");
            } else {
                showErrorMessage("No se pudo actualizar la información");
            }
        } catch (Exception e) {
            showErrorMessage("Error: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la acción de cancelar
     */
    private void handleCancel() {
        // Volver a cargar los datos originales
        loadUserData();
        clearMessage();
    }
    
    /**
     * Maneja la acción de cambiar contraseña
     */
    private void handleChangePassword() {
        // Por ahora, simplemente mostrar un mensaje
        AppUtils.showInfo("Cambio de contraseña", "Esta funcionalidad estará disponible próximamente");
    }
    
    /**
     * Maneja la acción de editar dirección
     */
    private void handleEditAddress() {
        // Por ahora, simplemente mostrar un mensaje
        AppUtils.showInfo("Editar dirección", "Esta funcionalidad estará disponible próximamente");
    }
    
    /**
     * Muestra un mensaje de éxito
     */
    private void showSuccessMessage(String message) {
        lbl_message.setText(message);
        lbl_message.getStyleClass().remove("error-message");
        lbl_message.getStyleClass().add("success-message");
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void showErrorMessage(String message) {
        lbl_message.setText(message);
        lbl_message.getStyleClass().remove("success-message");
        lbl_message.getStyleClass().add("error-message");
    }
    
    /**
     * Limpia el mensaje
     */
    private void clearMessage() {
        lbl_message.setText("");
        lbl_message.getStyleClass().removeAll("success-message", "error-message");
    }
    
    /**
     * Establece los datos del usuario a mostrar
     */
    public void setUserData(UserDTO userData) {
        this.currentUser = userData;
        if (txt_name != null) {  // Verificar que la vista ya esté inicializada
            loadUserData();
        }
    }
}