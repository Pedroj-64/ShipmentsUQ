package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.UserProfileController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador de vista para el perfil de usuario.
 * Maneja la edición de información personal y cambio de contraseña.
 */
public class UserProfileViewController implements Initializable {

    // Campos de información personal
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private Button btnSavePersonalInfo;
    
    // Campos de cambio de contraseña
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnChangePassword;
    
    // Campos de preferencias
    @FXML private CheckBox chkEmailNotifications;
    @FXML private CheckBox chkSmsNotifications;
    @FXML private CheckBox chkPromoNotifications;
    @FXML private Button btnSavePreferences;
    
    // Mensajes
    @FXML private Label lblStatus;
    
    // Controlador de negocio
    private UserProfileController controller;
    
    // Usuario actual
    private UserDTO currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initController();
        setupEventHandlers();
        loadUserData();
    }
    
    /**
     * Inicializa el controlador de negocio
     */
    private void initController() {
        try {
            controller = new UserProfileController();
            // La carga del usuario actual se realiza mediante setUserData
            // que es llamado desde el UserDashboardViewController
        } catch (Exception e) {
            showErrorMessage("Error al inicializar: " + e.getMessage());
        }
    }
    
    /**
     * Configura los manejadores de eventos
     */
    private void setupEventHandlers() {
        // Guardar información personal
        btnSavePersonalInfo.setOnAction(event -> handleSavePersonalInfo());
        
        // Cambiar contraseña
        btnChangePassword.setOnAction(event -> handleChangePassword());
        
        // Guardar preferencias
        btnSavePreferences.setOnAction(event -> handleSavePreferences());
    }
    
    /**
     * Carga los datos del usuario en el formulario
     */
    private void loadUserData() {
        if (currentUser == null) {
            showErrorMessage("No se pudo cargar la información del usuario");
            return;
        }
        
        // Cargar datos personales
        txtName.setText(currentUser.getName());
        txtEmail.setText(currentUser.getEmail());
        txtPhone.setText(currentUser.getPhone());
        
        // Cargar preferencias (estas serían cargadas desde el usuario en una implementación completa)
        chkEmailNotifications.setSelected(true);
        chkSmsNotifications.setSelected(true);
        chkPromoNotifications.setSelected(false);
        
        clearStatus();
    }
    
    /**
     * Maneja el guardado de información personal
     */
    private void handleSavePersonalInfo() {
        try {
            // Validaciones básicas
            if (txtName.getText().trim().isEmpty() || 
                txtEmail.getText().trim().isEmpty() || 
                txtPhone.getText().trim().isEmpty()) {
                showErrorMessage("Todos los campos son obligatorios");
                return;
            }
            
            // Validar formato de email (implementación básica)
            if (!txtEmail.getText().contains("@") || !txtEmail.getText().contains(".")) {
                showErrorMessage("Formato de email inválido");
                return;
            }
            
            // Guardar cambios
            boolean success = controller.updateUserInfo(
                txtName.getText(),
                txtEmail.getText(),
                txtPhone.getText()
            );
            
            if (success) {
                showSuccessMessage("Información personal actualizada correctamente");
                // Actualizar datos locales
                currentUser = controller.getCurrentUser();
            } else {
                showErrorMessage("No se pudo actualizar la información personal");
            }
        } catch (Exception e) {
            showErrorMessage("Error: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el cambio de contraseña
     */
    private void handleChangePassword() {
        try {
            // Validaciones
            String currentPassword = txtCurrentPassword.getText();
            String newPassword = txtNewPassword.getText();
            String confirmPassword = txtConfirmPassword.getText();
            
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showErrorMessage("Todos los campos de contraseña son obligatorios");
                return;
            }
            
            if (newPassword.length() < 6) {
                showErrorMessage("La nueva contraseña debe tener al menos 6 caracteres");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                showErrorMessage("Las contraseñas no coinciden");
                return;
            }
            
            // Validar contraseña actual y cambiar
            boolean success = controller.changePassword(currentPassword, newPassword);
            
            if (success) {
                showSuccessMessage("Contraseña actualizada correctamente");
                
                // Limpiar campos
                txtCurrentPassword.clear();
                txtNewPassword.clear();
                txtConfirmPassword.clear();
            } else {
                showErrorMessage("La contraseña actual es incorrecta");
            }
        } catch (Exception e) {
            showErrorMessage("Error: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el guardado de preferencias
     */
    private void handleSavePreferences() {
        try {
            // Aquí se guardarían las preferencias del usuario
            // En una implementación completa, esto llamaría al controlador
            
            // Por ahora solo mostramos un mensaje de éxito
            showSuccessMessage("Preferencias guardadas correctamente");
        } catch (Exception e) {
            showErrorMessage("Error: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void showErrorMessage(String message) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("success-message");
        lblStatus.getStyleClass().add("error-message");
    }
    
    /**
     * Muestra un mensaje de éxito
     */
    private void showSuccessMessage(String message) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("error-message");
        lblStatus.getStyleClass().add("success-message");
    }
    
    /**
     * Limpia el mensaje de estado
     */
    private void clearStatus() {
        lblStatus.setText("");
        lblStatus.getStyleClass().removeAll("error-message", "success-message");
    }
    
    /**
     * Establece el usuario actual
     */
    public void setUserData(UserDTO userData) {
        this.currentUser = userData;
        if (txtName != null) {  // Verificar que la interfaz ya esté inicializada
            loadUserData();
        }
    }
}