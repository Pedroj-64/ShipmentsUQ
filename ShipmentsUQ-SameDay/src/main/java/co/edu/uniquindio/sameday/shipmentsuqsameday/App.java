package co.edu.uniquindio.sameday.shipmentsuqsameday;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.DataManager;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.Session;

/**
 * Clase principal de la aplicación.
 * Inicializa la interfaz de usuario y establece el punto de entrada para la
 * aplicación JavaFX. Maneja la carga y guardado del estado de la aplicación.
 */
public class App extends Application {

    /** Dimensiones estándar para la ventana de la aplicación */
    public static final double WINDOW_WIDTH = 800;
    public static final double WINDOW_HEIGHT = 600;

    /** Escena principal de la aplicación */
    private static Scene mainScene;

    /** Administrador de datos para la persistencia */
    private static DataManager dataManager; // Gestor de datos para persistencia
    
    /** Sesión actual del usuario */
    private static Session currentSession;
    
    /** Servicios del host para abrir archivos */
    private static javafx.application.HostServices appHostServices;

    /**
     * Método que se ejecuta antes de iniciar la aplicación.
     * Inicializa el DataManager para cargar el estado guardado.
     */
    @Override
    public void init() throws Exception {
        System.out.println("Inicializando la aplicación...");
        // Obtener la instancia del DataManager para cargar los datos
        dataManager = DataManager.getInstance();
        // Inicializar la sesión
        currentSession = Session.getInstance();
    }
    
    @Override
    public void start(Stage stage) throws IOException {
        // Inicializar hostServices para abrir archivos
        appHostServices = getHostServices();
        
        // Configurar la escena inicial con la pantalla de login
        Parent root = FXMLLoader.load(App.class.getResource("interfaces/Login.fxml"));
        mainScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Configurar la ventana principal
        stage.setTitle("ShipmentsUQ - Inicio de Sesión");
        stage.setScene(mainScene);

        // Inicializar la clase de utilidades con la escena actual
        AppUtils.setCurrentScene(mainScene);

        // Mostrar la ventana
        stage.show();
    }

    /**
     * Método que se ejecuta al cerrar la aplicación.
     * Guarda el estado actual usando el DataManager.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Cerrando la aplicación...");
        // Guardar el estado actual antes de cerrar
        if (dataManager != null) {
            dataManager.saveState();
        }
    }

    /**
     * Método principal que inicia la aplicación JavaFX.
     * 
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Reinicia la aplicación volviendo a la pantalla de inicio de sesión.
     * Guarda el estado actual antes de reiniciar.
     * Útil para implementar la funcionalidad de cerrar sesión.
     * 
     * @return true si se pudo reiniciar correctamente
     */
    public static boolean restartApp() {
        try {
            System.out.println("App.restartApp(): Delegando a AppUtils.restartApp()...");
            
            // Guardar el estado actual antes de reiniciar
            if (dataManager != null) {
                dataManager.saveState();
            }
            
            return AppUtils.restartApp();
            
        } catch (Exception e) {
            AppUtils.showError("Error al reiniciar", "No se pudo volver a la pantalla de inicio: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
        /**
     * Devuelve la sesión actual del usuario.
     * @return instancia de Session
     */
    public static Session getCurrentSession() {
        return currentSession;
    }
    
    /**
     * Devuelve los servicios del host para abrir archivos en aplicaciones externas.
     * @return servicios del host
     */
    public static javafx.application.HostServices getAppHostServices() {
        return appHostServices;
    }
}
