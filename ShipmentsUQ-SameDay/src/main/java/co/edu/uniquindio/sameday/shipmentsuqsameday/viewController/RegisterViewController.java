package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.RegisterController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;

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
    private RadioButton rb_user;
    @FXML
    private RadioButton rb_deliverer;
    @FXML
    private ToggleGroup tg_userType;

    // Controlador de negocio
    private RegisterController controller;

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
                controller.registerDeliverer(name, email, phone, password, city, document, zone);
                
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
}