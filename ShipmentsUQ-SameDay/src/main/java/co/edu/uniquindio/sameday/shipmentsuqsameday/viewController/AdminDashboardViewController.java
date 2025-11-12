package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.AdminDashboardController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de vista para el Panel de Administración
 * Se encarga de la interacción con la interfaz de usuario y la navegación entre las
 * distintas secciones del panel de administración
 */
public class AdminDashboardViewController implements Initializable {
    
    // Referencias a los elementos del encabezado
    @FXML private Label lbl_title;
    @FXML private Button btn_logout;
    
    // Referencias a los elementos del menú
    @FXML private Label lbl_menuTitle;
    @FXML private Button btn_users;
    @FXML private Button btn_couriers;
    @FXML private Button btn_shipments;
    @FXML private Button btn_metrics;
    
    // Referencias al área de contenido
    @FXML private StackPane stk_contentArea;
    @FXML private Label lbl_welcomeMessage;
    
    // Referencias al pie de página
    @FXML private Label lbl_statusMessage;
    
    // Controlador de negocio
    private AdminDashboardController controller;
    
    // Vistas cargadas
    private Parent usersView;
    private Parent shipmentsView;
    private Parent metricsView;
    
    // Usuario administrador actual
    @SuppressWarnings("unused")
    private User adminUser;

    /**
     * Inicializa el controlador de vista
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar controlador
        controller = new AdminDashboardController();
        
        // Configurar botones del menú
        setupMenuButtons();
        
        // Configurar botón de cierre de sesión
        setupLogoutButton();
    }
    
    /**
     * Establece el usuario administrador
     * @param user usuario administrador
     */
    public void setAdminUser(User user) {
        this.adminUser = user;
        lbl_welcomeMessage.setText("Bienvenido/a, " + user.getName() + "!");
        lbl_statusMessage.setText("Conectado como: " + user.getEmail());
    }

    /**
     * Configura los botones del menú
     */
    private void setupMenuButtons() {
        // Botón de usuarios y repartidores
        btn_users.setOnAction(e -> loadUsersAndCouriersView());
        
        // Botón de repartidores (actúa como el botón de usuarios)
        btn_couriers.setOnAction(e -> loadUsersAndCouriersView());
        
        // Botón de envíos
        btn_shipments.setOnAction(e -> loadShipmentsView());
        
        // Botón de métricas - AHORA CARGA LA NUEVA VISTA
        btn_metrics.setOnAction(e -> loadMetricsNewView());
    }
    
    /**
     * Configura el botón de cierre de sesión
     */
    private void setupLogoutButton() {
        btn_logout.setOnAction(e -> logout());
    }
    
    /**
     * Carga la vista de gestión de usuarios y repartidores
     */
    private void loadUsersAndCouriersView() {
        try {
            if (usersView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/interfaces/AdminUsersCouriers.fxml"));
                usersView = loader.load();
                
                // Obtener el controlador de vista
                @SuppressWarnings("unused")
                AdminUsersCouriersViewController viewController = loader.getController();
                // Aquí podrías pasar datos adicionales al controlador si es necesario
            }
            
            // Limpiar y mostrar la vista
            stk_contentArea.getChildren().clear();
            stk_contentArea.getChildren().add(usersView);
            
            // Actualizar botones de menú
            updateMenuButtonsState(btn_users);
            
            // Actualizar estado
            lbl_statusMessage.setText("Gestión de Usuarios y Repartidores");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Error al cargar la vista de usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Carga la vista de gestión de envíos
     */
    private void loadShipmentsView() {
        try {
            if (shipmentsView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/interfaces/AdminShipments.fxml"));
                shipmentsView = loader.load();
                
                // Obtener el controlador de vista
                @SuppressWarnings("unused")
                AdminShipmentsViewController viewController = loader.getController();
                // Aquí podrías pasar datos adicionales al controlador si es necesario
            }
            
            // Limpiar y mostrar la vista
            stk_contentArea.getChildren().clear();
            stk_contentArea.getChildren().add(shipmentsView);
            
            // Actualizar botones de menú
            updateMenuButtonsState(btn_shipments);
            
            // Actualizar estado
            lbl_statusMessage.setText("Gestión de Envíos");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Error al cargar la vista de envíos: " + e.getMessage());
        }
    }
    
    /**
     * Carga la vista de métricas y reportes
     */
    @SuppressWarnings("unused")
    private void loadMetricsView() {
        try {
            if (metricsView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/interfaces/AdminMetrics.fxml"));
                metricsView = loader.load();
                
                // Obtener el controlador de vista
                @SuppressWarnings("unused")
                AdminMetricsViewController viewController = loader.getController();
                // Aquí podrías pasar datos adicionales al controlador si es necesario
            }
            
            // Limpiar y mostrar la vista
            stk_contentArea.getChildren().clear();
            stk_contentArea.getChildren().add(metricsView);
            
            // Actualizar botones de menú
            updateMenuButtonsState(btn_metrics);
            
            // Actualizar estado
            lbl_statusMessage.setText("Métricas y Reportes");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Error al cargar la vista de métricas: " + e.getMessage());
        }
    }
    
    /**
     * Carga la NUEVA vista de métricas y reportes (sin errores)
     */
    private void loadMetricsNewView() {
        try {
            // Siempre cargar una vista nueva para asegurar datos actualizados
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/sameday/shipmentsuqsameday/interfaces/AdminMetrics.fxml"));
            Parent newMetricsView = loader.load();
            
            // Limpiar y mostrar la vista
            stk_contentArea.getChildren().clear();
            stk_contentArea.getChildren().add(newMetricsView);
            
            // Actualizar botones de menú
            updateMenuButtonsState(btn_metrics);
            
            // Actualizar estado
            lbl_statusMessage.setText("Métricas y Reportes");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Error al cargar la vista de métricas: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza el estado visual de los botones del menú
     * @param activeButton el botón activo actualmente
     */
    private void updateMenuButtonsState(Button activeButton) {
        // Quitar la clase active de todos los botones
        btn_users.getStyleClass().remove("active");
        btn_couriers.getStyleClass().remove("active");
        btn_shipments.getStyleClass().remove("active");
        btn_metrics.getStyleClass().remove("active");
        
        // Agregar la clase active solo al botón activo
        activeButton.getStyleClass().add("active");
        
        // Si el botón de usuarios o repartidores está activo, ambos deben verse activos
        if (activeButton == btn_users || activeButton == btn_couriers) {
            btn_users.getStyleClass().add("active");
            btn_couriers.getStyleClass().add("active");
        }
    }
    
    /**
     * Muestra un mensaje de error en la barra de estado
     * @param message mensaje a mostrar
     */
    private void showErrorMessage(String message) {
        lbl_statusMessage.setText(message);
        lbl_statusMessage.getStyleClass().add("error-message");
    }
    
    /**
     * Cierra la sesión del usuario administrador
     */
    private void logout() {
        controller.logout();
    }
}
