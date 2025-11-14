package co.edu.uniquindio.sameday.shipmentsuqsameday;

import javafx.application.Application;
import javafx.application.HostServices;
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
    public static final double WINDOW_WIDTH = 800;
    public static final double WINDOW_HEIGHT = 600;
    private static Scene mainScene;
    private static DataManager dataManager; 
    private static Session currentSession;
    private static HostServices appHostServices;

    /**
     * Método que se ejecuta antes de iniciar la aplicación.
     * Inicializa el DataManager para cargar el estado guardado.
     */
    @Override
    public void init() throws Exception {
        dataManager = DataManager.getInstance();
        currentSession = Session.getInstance();
        
    }
    
    @Override
    public void start(Stage stage) throws IOException {
        appHostServices = getHostServices();
        Parent root = FXMLLoader.load(App.class.getResource("interfaces/Login.fxml"));
        mainScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Aplicar tema guardado
        co.edu.uniquindio.sameday.shipmentsuqsameday.util.ThemeManager.getInstance().applyCurrentTheme(mainScene);
        
        stage.setTitle("ShipmentsUQ - Inicio de Sesión");
        stage.setScene(mainScene);
        AppUtils.setCurrentScene(mainScene);
        stage.show();
    }

    /**
     * Método que se ejecuta al cerrar la aplicación.
     * Guarda el estado actual usando el DataManager.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Cerrando la aplicación...");
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
    public static HostServices getAppHostServices() {
        return appHostServices;
    }
}
