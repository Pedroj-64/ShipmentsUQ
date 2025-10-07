package co.edu.uniquindio.sameday.shipmentsuqsameday.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

/**
 * Clase de utilidad con métodos estáticos para la gestión de interfaces y alertas.
 * Centraliza la lógica común para cambios de escena y notificaciones al usuario.
 */
public class AppUtils {

    /** Escena actual de la aplicación */
    private static Scene currentScene;
    
    /** Pila para almacenar el historial de navegación */
    private static final Stack<String> sceneHistory = new Stack<>();

    /**
     * Establece la escena actual para poder utilizarla en los cambios de vista.
     *
     * @param scene La escena principal de la aplicación
     */
    public static void setCurrentScene(Scene scene) {
        currentScene = scene;
    }

    /**
     * Muestra una alerta y redirige a una nueva escena al cerrar la alerta.
     * 
     * @param title   El título de la alerta.
     * @param message El contenido del mensaje.
     * @param type    El tipo de alerta.
     * @param fxml    El nombre del archivo FXML de la nueva escena.
     * @param width   El ancho de la nueva escena.
     * @param height  La altura de la nueva escena.
     */
    public static void showAlertAndRedirect(String title, String message, AlertType type, String fxml, double width,
            double height) {
        Alert alert = new Alert(type); // Crear la alerta
        alert.setTitle(title); // Establecer el título
        alert.setHeaderText(null); // Sin encabezado
        alert.setContentText(message); // Mensaje

        // Redirigir a una nueva escena al cerrar la alerta
        alert.setOnHidden(evt -> loadScene(fxml, width, height));
        alert.show();
    }

    /**
     * Carga y establece una nueva escena en la ventana principal.
     * 
     * @param fxml   El nombre del archivo FXML.
     * @param width  El ancho de la nueva escena.
     * @param height La altura de la nueva escena.
     */
    public static void loadScene(String fxml, double width, double height) {
        try {
            Parent root = loadFXML(fxml);
            
            if (currentScene != null) {
                currentScene.setRoot(root);
                currentScene.getWindow().setWidth(width);
                currentScene.getWindow().setHeight(height);
                
                // Agregar la escena al historial
                pushScene(fxml);
            } else {
                throw new IllegalStateException("La escena actual no está inicializada. Llame a setCurrentScene primero.");
            }
        } catch (IOException e) {
            showAlert("Error al cambiar la vista", "No se pudo cargar el archivo FXML: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Carga y establece una nueva escena en la ventana principal sin agregarla al
     * historial.
     * 
     * @param fxml   El nombre del archivo FXML.
     * @param width  El ancho de la nueva escena.
     * @param height La altura de la nueva escena.
     */
    public static void loadSceneNoHistory(String fxml, double width, double height) {
        try {
            Parent root = loadFXML(fxml);
            
            if (currentScene != null) {
                currentScene.setRoot(root);
                currentScene.getWindow().setWidth(width);
                currentScene.getWindow().setHeight(height);
            } else {
                throw new IllegalStateException("La escena actual no está inicializada. Llame a setCurrentScene primero.");
            }
        } catch (IOException e) {
            showAlert("Error al cambiar la vista", "No se pudo cargar el archivo FXML: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Carga un archivo FXML y devuelve el nodo raíz.
     * 
     * @param fxml El nombre del archivo FXML.
     * @return El nodo raíz del archivo FXML cargado.
     * @throws IOException Si ocurre un error al cargar el archivo.
     */
    private static Parent loadFXML(String fxml) throws IOException {
        // Asegurar que la ruta comienza con /co/edu/... si es necesario
        String fxmlPath = fxml.startsWith("interfaces/") || fxml.startsWith("/") ? fxml : "interfaces/" + fxml;
        
        // Asegurar que tenga la extensión .fxml
        if (!fxmlPath.endsWith(".fxml")) {
            fxmlPath = fxmlPath + ".fxml";
        }
        
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxmlPath));
        return fxmlLoader.load(); // Cargar y devolver el nodo raíz del archivo FXML
    }

    /**
     * Muestra una alerta con el mensaje especificado.
     * 
     * @param title   El título de la alerta.
     * @param message El contenido del mensaje.
     * @param type    El tipo de alerta.
     */
    public static void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type); // Crear la alerta
        alert.setTitle(title); // Establecer el título de la alerta
        alert.setHeaderText(null); // No usar encabezado
        alert.setContentText(message); // Establecer el contenido del mensaje
        alert.showAndWait(); // Mostrar la alerta y esperar a que el usuario la cierre
    }
    
    /**
     * Muestra una alerta informativa.
     *
     * @param title El título de la alerta
     * @param message El mensaje a mostrar
     */
    public static void showInfo(String title, String message) {
        showAlert(title, message, AlertType.INFORMATION);
    }
    
    /**
     * Muestra una alerta de error.
     *
     * @param title El título de la alerta
     * @param message El mensaje de error a mostrar
     */
    public static void showError(String title, String message) {
        showAlert(title, message, AlertType.ERROR);
    }
    
    /**
     * Muestra una alerta de advertencia.
     *
     * @param title El título de la alerta
     * @param message El mensaje de advertencia a mostrar
     */
    public static void showWarning(String title, String message) {
        showAlert(title, message, AlertType.WARNING);
    }
    
    /**
     * Agrega una escena al historial de navegación.
     * 
     * @param fxml La ruta del archivo FXML
     */
    public static void pushScene(String fxml) {
        sceneHistory.push(fxml);
    }
    
    /**
     * Navega a la escena anterior en el historial.
     * 
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     * @return true si se navegó a una escena anterior, false si no hay historial
     */
    public static boolean goBack(double width, double height) {
        // Quitar la escena actual del historial
        if (!sceneHistory.isEmpty()) {
            sceneHistory.pop();
        }
        
        // Si hay historial, ir a la escena anterior
        if (!sceneHistory.isEmpty()) {
            String previousScene = sceneHistory.pop(); // Quitar para no duplicar
            loadSceneNoHistory(previousScene, width, height);
            return true;
        }
        
        return false;
    }
    
    /**
     * Crea una nueva ventana con la escena especificada.
     *
     * @param fxml Ruta al archivo FXML
     * @param title Título de la ventana
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     * @return La Stage (ventana) creada
     */
    public static Stage openNewWindow(String fxml, String title, double width, double height) {
        try {
            Stage stage = new Stage();
            Parent root = loadFXML(fxml);
            Scene scene = new Scene(root, width, height);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            return stage;
        } catch (IOException e) {
            showError("Error al abrir ventana", "No se pudo cargar el archivo FXML: " + e.getMessage());
            return null;
        }
    }
}