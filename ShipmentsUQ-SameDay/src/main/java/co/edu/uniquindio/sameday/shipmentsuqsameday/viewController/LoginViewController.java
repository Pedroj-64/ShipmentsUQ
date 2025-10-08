package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.LoginController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador de vista para la pantalla de inicio de sesión.
 * Maneja la interacción del usuario con la interfaz de login.
 */
public class LoginViewController implements Initializable {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Button btn_login;
    @FXML
    private Button btn_register;
    @FXML
    private Label lbl_email;
    @FXML
    private Label lbl_message;
    @FXML
    private Label lbl_password;
    @FXML
    private Label lbl_title;
    @FXML
    private Label lbl_forgotPassword;
    @FXML
    private TextField txt_email;
    @FXML
    private PasswordField txt_password;

    // Controlador de negocio
    private LoginController controller;

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
        clearMessage();
        Platform.runLater(() -> {
            txt_email.requestFocus();
            
            // DEBUG: Verificar usuarios disponibles al iniciar
            System.out.println("=== USUARIOS DISPONIBLES EN LOGIN ===");
            controller.showAllUsersForDebug();
        });
    }

    /**
     * Inicializa el controlador de negocio
     */
    private void initController() {
        controller = new LoginController();
    }

    /**
     * Configura los listeners de los botones y enlaces
     */
    private void initButtonListeners() {
        btn_login.setOnAction(this::handleLogin);
        btn_register.setOnAction(this::handleRegister);

        // Configurar el enlace de recuperación de contraseña
        lbl_forgotPassword.getStyleClass().add("clickable");
        lbl_forgotPassword.setOnMouseClicked(event -> handleForgotPassword());
    }

    /**
     * Configura la validación de campos del formulario
     */
    private void initFormValidation() {
        // Validación básica mientras se escribe
        txt_email.textProperty().addListener((observable, oldValue, newValue) -> {
            clearMessage();
        });

        txt_password.textProperty().addListener((observable, oldValue, newValue) -> {
            clearMessage();
        });
    }

    /**
     * Maneja el evento de clic en el botón de inicio de sesión
     * 
     * @param event El evento de acción
     */
    private void handleLogin(ActionEvent event) {
        // Obtener datos del formulario
        String email = txt_email.getText().trim();
        String password = txt_password.getText();

        // Validar que los campos no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            showErrorMessage("Por favor complete todos los campos");
            return;
        }

        try {
            // Intentar iniciar sesión
            User authenticatedUser = controller.authenticateUser(email, password);

            if (authenticatedUser != null) {
                // Mostrar mensaje de éxito brevemente antes de navegar
                showSuccessMessage("Inicio de sesión exitoso");

                // Navegación según el rol del usuario
                redirectUserBasedOnRole(authenticatedUser);
            } else {
                showErrorMessage("Credenciales incorrectas");
            }
        } catch (IllegalArgumentException e) {
            // Error específico de validación
            showErrorMessage("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            // Otros errores inesperados
            showErrorMessage("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Redirige al usuario a la vista correspondiente según su rol
     * 
     * @param user El usuario autenticado
     */
    private void redirectUserBasedOnRole(User user) {
        try {
            // Establecer el usuario actual en el controlador del dashboard
            co.edu.uniquindio.sameday.shipmentsuqsameday.controller.UserDashboardController.setCurrentUser(user);
            
            // Según el rol, navegamos a diferentes vistas
            if (user.getRole() == UserRole.ADMIN) {
                AppUtils.navigateTo("AdminDashboard.fxml", btn_login);
            } else if (user.getRole() == UserRole.CLIENT) {
                AppUtils.navigateTo("UserDashboard.fxml", btn_login);
            } else {
                // Rol desconocido o no implementado
                showErrorMessage("Rol de usuario no reconocido");
            }
        } catch (Exception e) {
            showErrorMessage("Error al cambiar de ventana: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maneja el evento de clic en el botón de registro
     * 
     * @param event El evento de acción
     */
    private void handleRegister(ActionEvent event) {
        try {
            // Navegar a la vista de registro
            AppUtils.navigateTo("Register.fxml", btn_register);
        } catch (Exception e) {
            showErrorMessage("Error al abrir el formulario de registro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje de error en la interfaz
     * 
     * @param message El mensaje a mostrar
     */
    private void showErrorMessage(String message) {
        lbl_message.setText(message);
        lbl_message.getStyleClass().removeAll("success-message");
        lbl_message.getStyleClass().add("error-message");
        lbl_message.setVisible(true);
    }

    /**
     * Muestra un mensaje de éxito en la interfaz
     * 
     * @param message El mensaje a mostrar
     */
    private void showSuccessMessage(String message) {
        lbl_message.setText(message);
        lbl_message.getStyleClass().removeAll("error-message");
        lbl_message.getStyleClass().add("success-message");
        lbl_message.setVisible(true);
    }

    /**
     * Limpia el mensaje de la interfaz
     */
    private void clearMessage() {
        lbl_message.setText("");
        lbl_message.setVisible(false);
    }

    /**
     * Maneja el evento de clic en "Olvidé mi contraseña"
     */
    private void handleForgotPassword() {
        String email = txt_email.getText().trim();

        // Si el campo de email está vacío, pedimos que lo complete
        if (email.isEmpty()) {
            showErrorMessage("Por favor, ingrese su correo electrónico para recuperar su contraseña");
            return;
        }

        try {
            String password = controller.recoverPassword(email);

            if (password != null) {
                // Mostrar la contraseña en una alerta
                AppUtils.showAlert("Recuperación de contraseña",
                        "Su contraseña es: " + password,
                        AlertType.INFORMATION);

                showSuccessMessage("Contraseña recuperada exitosamente");
            } else {
                showErrorMessage("No se encontró una cuenta con ese correo electrónico");
            }
        } catch (IllegalArgumentException e) {
            showErrorMessage("Error en el formato del correo: " + e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Error al procesar la solicitud: " + e.getMessage());
            e.printStackTrace();
        }
    }
}