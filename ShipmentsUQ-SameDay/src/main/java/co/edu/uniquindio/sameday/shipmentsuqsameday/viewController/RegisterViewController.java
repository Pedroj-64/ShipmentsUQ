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
import javafx.scene.control.TextField;
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
     * Maneja el evento de clic en el botón de registro
     * 
     * @param event El evento de acción
     */
    private void handleRegister(ActionEvent event) {
        // Obtener datos del formulario
        String name = txt_name.getText().trim();
        String email = txt_email.getText().trim();
        String phone = txt_phone.getText().trim();
        String password = txtp_password.getText();
        String confirmPassword = txtp_confirmPassword.getText();
        String city = txt_city.getText().trim();
        boolean termsAccepted = chb_terms.isSelected();

        // Validar que los campos no estén vacíos
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty() || city.isEmpty()) {
            showErrorStatus("Por favor complete todos los campos");
            return;
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
            // Intentar registrar al usuario
            controller.registerUser(name, email, phone, password, city);

            // Mostrar mensaje de éxito
            showSuccessStatus("Usuario registrado exitosamente");

            // Mostrar alerta y redirigir a la pantalla de inicio de sesión
            AppUtils.showAlertAndRedirect(
                    "Registro exitoso",
                    "Su cuenta ha sido creada. Ahora puede iniciar sesión con sus credenciales.",
                    AlertType.INFORMATION,
                    "Login");

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