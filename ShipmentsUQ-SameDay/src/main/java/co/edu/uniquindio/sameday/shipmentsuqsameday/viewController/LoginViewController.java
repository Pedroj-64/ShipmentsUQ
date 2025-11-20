package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.LoginController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.Session;

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
 * Controlador de vista para la pantalla de inicio de sesión unificado.
 * Maneja la autenticación tanto de usuarios como de repartidores.
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
    @FXML
    private Button btn_webVersion;

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
        btn_webVersion.setOnAction(this::handleOpenWebVersion);

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
     * Maneja el evento de clic en el botón de inicio de sesión.
     * Login unificado: detecta automáticamente si es usuario (email) o repartidor (documento).
     * 
     * @param event El evento de acción
     */
    private void handleLogin(ActionEvent event) {
        // Obtener datos del formulario
        String identifier = txt_email.getText().trim();
        String password = txt_password.getText();

        // Validar que los campos no estén vacíos
        if (identifier.isEmpty() || password.isEmpty()) {
            showErrorMessage("Por favor complete todos los campos");
            return;
        }

        try {
            // Intentar autenticación unificada
            Object authenticated = controller.authenticateAny(identifier, password);

            if (authenticated != null) {
                // Mostrar mensaje de éxito brevemente antes de navegar
                showSuccessMessage("Inicio de sesión exitoso");

                // Navegación según el tipo de usuario
                if (authenticated instanceof User) {
                    redirectUserBasedOnRole((User) authenticated);
                } else if (authenticated instanceof Deliverer) {
                    redirectDelivererToDashboard((Deliverer) authenticated);
                }
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
            
            // Establecer el ID de usuario en la sesión
            if (co.edu.uniquindio.sameday.shipmentsuqsameday.App.getCurrentSession() != null) {
                co.edu.uniquindio.sameday.shipmentsuqsameday.App.getCurrentSession().setUserId(user.getId());
                System.out.println("DEBUG: ID de usuario establecido en la sesión: " + user.getId());
            } else {
                System.out.println("DEBUG: No se pudo establecer la sesión del usuario porque App.getCurrentSession() es null");
            }
            
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
     * Redirige al repartidor a su dashboard
     * 
     * @param deliverer El repartidor autenticado
     */
    private void redirectDelivererToDashboard(Deliverer deliverer) {
        try {
            // Guardar en sesión
            Session.getInstance().setCurrentDeliverer(deliverer);
            System.out.println("✅ Repartidor autenticado: " + deliverer.getName());
            
            // Navegar al dashboard del repartidor
            AppUtils.navigateTo("DelivererDashboard.fxml", btn_login);
        } catch (Exception e) {
            showErrorMessage("Error al cargar dashboard de repartidor: " + e.getMessage());
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
        String password = txt_password.getText();

        // Si hay una contraseña escrita, verificar a qué correo pertenece
        if (!password.isEmpty()) {
            String foundEmail = controller.findEmailByPassword(password);
            
            if (foundEmail != null) {
                // La contraseña pertenece a un usuario registrado
                if (email.isEmpty() || !email.equalsIgnoreCase(foundEmail)) {
                    // Mostrar a qué correo pertenece
                    showErrorMessage("Error: Esta contraseña ya pertenece al correo " + foundEmail);
                    return;
                } else {
                    // El correo y contraseña coinciden, están correctos
                    showSuccessMessage("El correo y contraseña son correctos. Puede iniciar sesión.");
                    return;
                }
            }
            // Si no se encuentra la contraseña, continuar con el flujo normal
        }

        // Si el campo de email está vacío, pedimos que lo complete
        if (email.isEmpty()) {
            showErrorMessage("Por favor, ingrese su correo electrónico para recuperar su contraseña");
            return;
        }

        try {
            String recoveredPassword = controller.recoverPassword(email);

            if (recoveredPassword != null) {
                // Mostrar la contraseña en una alerta
                AppUtils.showAlert("Recuperación de contraseña",
                        "Su contraseña es: " + recoveredPassword,
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
    
    /**
     * Maneja el evento de abrir la versión web
     * Inicia el servidor web y abre el navegador
     * 
     * @param event El evento de acción
     */
    private void handleOpenWebVersion(ActionEvent event) {
        try {
            showSuccessMessage("Iniciando servidor web...");
            
            // Iniciar servidor web en un thread separado (daemon para que no bloquee el cierre de la app)
            Thread serverThread = new Thread(() -> {
                try {
                    System.out.println("=".repeat(60));
                    System.out.println("🚀 INICIANDO SERVIDOR WEB SPRING BOOT");
                    System.out.println("=".repeat(60));
                    co.edu.uniquindio.sameday.shipmentsuqsameday.webapp.WebApplication.main(new String[]{});
                } catch (Exception e) {
                    System.err.println("❌ ERROR al iniciar servidor web: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> 
                        showErrorMessage("Error al iniciar servidor web: " + e.getMessage())
                    );
                }
            });
            serverThread.setDaemon(true);
            serverThread.setName("SpringBootWebServer");
            serverThread.start();
            
            // Esperar a que el servidor inicie
            Thread.sleep(3000);
            
            // Abrir navegador con el frontend React
            String webUrl = "http://localhost:3000";
            co.edu.uniquindio.sameday.shipmentsuqsameday.App.getAppHostServices().showDocument(webUrl);
            
            System.out.println("✅ Servidor web iniciado en http://localhost:8080");
            System.out.println("📚 API disponible en http://localhost:8080/api");
            System.out.println("🌐 Frontend disponible en http://localhost:3000");
            System.out.println("💡 IMPORTANTE: Ejecuta 'npm run dev' en la carpeta frontend");
            
            showSuccessMessage("Servidor API iniciado!\nAhora ejecuta el frontend con 'npm run dev'");
            
        } catch (Exception e) {
            showErrorMessage("Error al abrir versión web: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}