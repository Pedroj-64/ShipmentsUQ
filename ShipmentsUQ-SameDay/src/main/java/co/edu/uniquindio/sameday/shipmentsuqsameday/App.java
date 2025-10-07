package co.edu.uniquindio.sameday.shipmentsuqsameday;

import co.edu.uniquindio.sameday.shipmentsuqsameday.util.AppUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de la aplicación.
 * Inicializa la interfaz de usuario y establece el punto de entrada para la aplicación JavaFX.
 */
public class App extends Application {

    /** Escena principal de la aplicación */
    private static Scene mainScene;
    
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar la interfaz principal
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("interfaces/AddressForm.fxml"));
        mainScene = new Scene(fxmlLoader.load(), 900, 500);
        
        // Configurar la ventana principal
        stage.setTitle("Registrar Dirección");
        stage.setScene(mainScene);
        
        // Inicializar la clase de utilidades con la escena actual
        AppUtils.setCurrentScene(mainScene);
        
        // Mostrar la ventana
        stage.show();
    }
    
    /**
     * Método principal que inicia la aplicación JavaFX.
     * 
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        launch();
    }
}
