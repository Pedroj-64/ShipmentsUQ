package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.UserDashboardController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de vista para el dashboard del usuario.
 * Maneja la interacción con la interfaz del panel de control de usuario.
 */
public class UserDashboardViewController implements Initializable {

    // Elementos de la interfaz
    @FXML private Label lbl_welcomeUser;
    @FXML private Label lbl_statusMessage;
    @FXML private Label lbl_noContentSelected;
    @FXML private Button btn_logout;
    @FXML private Button btn_profile;
    @FXML private Button btn_addresses;
    @FXML private Button btn_shipments;
    @FXML private Button btn_payments;
    @FXML private Button btn_settings;
    @FXML private Button btn_help;
    @FXML private StackPane stk_contentArea;
    
    // Controlador de negocio
    private UserDashboardController controller;
    
    // Usuario actual
    private UserDTO currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar controlador y componentes
        initController();
        initButtonListeners();
        
        // Cargar datos del usuario actual
        loadCurrentUserData();
        
        // Actualizar la interfaz con la información del usuario
        updateWelcomeMessage();
        updateLastLoginMessage();
    }
    
    /**
     * Inicializa el controlador de negocio
     */
    private void initController() {
        controller = new UserDashboardController();
        
        // Obtener el usuario actual de la sesión
        try {
            currentUser = controller.getCurrentUserData();
        } catch (Exception e) {
            AppUtils.showError("Error", "No se pudo cargar la información del usuario: " + e.getMessage());
        }
    }
    
    /**
     * Configura los manejadores de eventos para los botones
     */
    private void initButtonListeners() {
        // Botón de cerrar sesión
        btn_logout.setOnAction(event -> handleLogout());
        
        // Botones de navegación del menú
        btn_profile.setOnAction(event -> loadModule("UserProfile.fxml", "Perfil"));
        btn_addresses.setOnAction(event -> loadModule("ProfileAndAddresses.fxml", "Direcciones"));
        btn_shipments.setOnAction(event -> loadModule("UserShipments.fxml", "Envíos"));
        btn_payments.setOnAction(event -> loadModule("Payments.fxml", "Pagos"));
        
        // Botones de configuración
        btn_settings.setOnAction(event -> handleSettings());
        btn_help.setOnAction(event -> handleHelp());
    }
    
    /**
     * Carga los datos del usuario actual
     */
    private void loadCurrentUserData() {
        if (currentUser == null) {
            AppUtils.showError("Error de sesión", "No hay una sesión activa.");
            handleLogout();
        }
    }
    
    /**
     * Actualiza el mensaje de bienvenida con el nombre del usuario
     */
    private void updateWelcomeMessage() {
        if (currentUser != null) {
            lbl_welcomeUser.setText("Bienvenido(a), " + currentUser.getName());
        }
    }
    
    /**
     * Actualiza el mensaje de último inicio de sesión
     */
    private void updateLastLoginMessage() {
        // Formato para la fecha actual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = LocalDateTime.now().format(formatter);
        
        lbl_statusMessage.setText("Último inicio de sesión: " + formattedDate);
    }
    
    /**
     * Carga un módulo específico en el área de contenido
     * 
     * @param fxmlFile Nombre del archivo FXML a cargar
     * @param moduleName Nombre del módulo para mostrar en mensajes
     */
    private void loadModule(String fxmlFile, String moduleName) {
        try {
            // Limpiar el contenido actual
            stk_contentArea.getChildren().clear();
            lbl_noContentSelected.setVisible(false);
            
            // Cargar la nueva vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/interfaces/" + fxmlFile));
            Parent moduleView = loader.load();
            
            // Asegurarse de que el módulo cargado se ajuste correctamente al área de contenido
            if (moduleView instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region region = (javafx.scene.layout.Region) moduleView;
                
                // Configurar propiedades de layout para una adaptación óptima
                region.setPrefWidth(-1);  // USE_COMPUTED_SIZE
                region.setPrefHeight(-1); // USE_COMPUTED_SIZE
                region.setMinWidth(-1);   // USE_PREF_SIZE
                region.setMinHeight(-1);  // USE_PREF_SIZE
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
                
                // Establecer restricciones de crecimiento
                javafx.scene.layout.StackPane.setAlignment(region, javafx.geometry.Pos.CENTER);
                javafx.scene.layout.StackPane.setMargin(region, new javafx.geometry.Insets(0));
            }
            
            // Si el módulo tiene un controlador que implementa initializable, podemos pasar datos
            if (loader.getController() != null) {
                // Pasar datos específicos según el controlador
                Object viewController = loader.getController();
                
                // Si es el controlador de perfil, pasarle los datos del usuario
                if (viewController instanceof UserProfileViewController) {
                    ((UserProfileViewController) viewController).setUserData(currentUser);
                }
                // Aquí se pueden agregar más casos para otros controladores si es necesario
            }
            
            // Agregar la vista al área de contenido
            stk_contentArea.getChildren().add(moduleView);
            
            // Notificar al controlador de negocio sobre el módulo cargado
            controller.logModuleAccess(moduleName);
            
        } catch (IOException e) {
            AppUtils.showError("Error", "No se pudo cargar el módulo " + moduleName + ": " + e.getMessage());
            e.printStackTrace();
            
            // Mostrar el mensaje de no selección en caso de error
            lbl_noContentSelected.setVisible(true);
        }
    }
    
    /**
     * Maneja el cierre de sesión
     */
    private void handleLogout() {
        if (AppUtils.showConfirmation("Cerrar sesión", "¿Está seguro que desea cerrar sesión?")) {
            try {
                // Notificar al controlador de negocio
                controller.logout();
                
                // Intentar cerrar la sesión usando AppUtils
                boolean loggedOut = AppUtils.logOut();
                
                if (!loggedOut) {
                    // Si falló el método principal, intentar alternativamente con AppUtils.restartApp()
                    System.out.println("Método logOut falló, intentando con AppUtils.restartApp()");
                    AppUtils.restartApp();
                }
            } catch (Exception e) {
                System.err.println("Error al cerrar sesión: " + e.getMessage());
                e.printStackTrace();
                AppUtils.showError("Error", "Ocurrió un problema al cerrar la sesión: " + e.getMessage());
                
                // Último intento: tratar de navegar directamente a la pantalla de login
                try {
                    AppUtils.navigateTo("Login.fxml", btn_logout);
                } catch (Exception ex) {
                    // No podemos hacer mucho más si esto falla
                    System.err.println("No se pudo recuperar de un error de cierre de sesión: " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Maneja la acción del botón de configuración
     */
    private void handleSettings() {
        // Por ahora, simplemente muestra un mensaje
        AppUtils.showInfo("Configuración", "Esta funcionalidad estará disponible próximamente.");
    }
    
    /**
     * Maneja la acción del botón de ayuda
     */
    private void handleHelp() {
        // Mostrar mensaje de contacto con administradores
        AppUtils.showInfo("Contacto de Ayuda", "Comuníquese con un administrador de MargaDev-Society.");
    }
}