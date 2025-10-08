package co.edu.uniquindio.sameday.shipmentsuqsameday.internalController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

/**
 * Clase de utilidad con métodos estáticos para la gestión de interfaces y
 * alertas.
 * Centraliza la lógica común para cambios de escena y notificaciones al
 * usuario.
 */
public class AppUtils {

    /** Escena actual de la aplicación */
    private static Scene currentScene;

    /** Pila para almacenar el historial de navegación */
    private static final Stack<String> sceneHistory = new Stack<>();

    // Referenciando las constantes desde App para mantener la coherencia
    public static final double WINDOW_WIDTH = App.WINDOW_WIDTH;
    public static final double WINDOW_HEIGHT = App.WINDOW_HEIGHT;

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
     */
    public static void showAlertAndRedirect(String title, String message, AlertType type, String fxml) {
        Alert alert = new Alert(type); // Crear la alerta
        alert.setTitle(title); // Establecer el título
        alert.setHeaderText(null); // Sin encabezado
        alert.setContentText(message); // Mensaje

        // Redirigir a una nueva escena al cerrar la alerta
        alert.setOnHidden(evt -> loadScene(fxml));
        alert.show();
    }

    /**
     * Método principal para cambiar el contenido de la escena actual.
     * 
     * @param fxml         El nombre del archivo FXML.
     * @param addToHistory Si debe agregarse al historial de navegación.
     */
    private static void changeScene(String fxml, boolean addToHistory) {
        try {
            Parent root = loadFXML(fxml);

            if (currentScene != null) {
                currentScene.setRoot(root);

                // Mantener el tamaño actual de la ventana
                // o usar el tamaño estándar si aún no se ha establecido
                if (currentScene.getWindow().getWidth() == 0) {
                    currentScene.getWindow().setWidth(WINDOW_WIDTH);
                    currentScene.getWindow().setHeight(WINDOW_HEIGHT);
                }

                // Agregar la escena al historial si se solicita
                if (addToHistory) {
                    pushScene(fxml);
                }
            } else {
                throw new IllegalStateException(
                        "La escena actual no está inicializada. Llame a setCurrentScene primero.");
            }
        } catch (IOException e) {
            showAlert("Error al cambiar la vista", "No se pudo cargar el archivo FXML: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Carga y establece una nueva escena en la ventana principal.
     * 
     * @param fxml El nombre del archivo FXML.
     */
    public static void loadScene(String fxml) {
        changeScene(fxml, true);
    }

    /**
     * Carga y establece una nueva escena en la ventana principal sin agregarla al
     * historial.
     * 
     * @param fxml El nombre del archivo FXML.
     */
    public static void loadSceneNoHistory(String fxml) {
        changeScene(fxml, false);
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
     * @param title   El título de la alerta
     * @param message El mensaje a mostrar
     */
    public static void showInfo(String title, String message) {
        showAlert(title, message, AlertType.INFORMATION);
    }

    /**
     * Muestra una alerta de error.
     *
     * @param title   El título de la alerta
     * @param message El mensaje de error a mostrar
     */
    public static void showError(String title, String message) {
        showAlert(title, message, AlertType.ERROR);
    }

    /**
     * Muestra una alerta de advertencia.
     *
     * @param title   El título de la alerta
     * @param message El mensaje de advertencia a mostrar
     */
    public static void showWarning(String title, String message) {
        showAlert(title, message, AlertType.WARNING);
    }
    
    /**
     * Muestra un diálogo de confirmación.
     *
     * @param title   El título de la alerta
     * @param message El mensaje de confirmación
     * @return true si el usuario confirma, false en caso contrario
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(response -> response == javafx.scene.control.ButtonType.OK).isPresent();
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
     * @return true si se navegó a una escena anterior, false si no hay historial
     */
    public static boolean goBack() {
        // Quitar la escena actual del historial
        if (!sceneHistory.isEmpty()) {
            sceneHistory.pop();
        }

        // Si hay historial, ir a la escena anterior
        if (!sceneHistory.isEmpty()) {
            String previousScene = sceneHistory.pop(); // Quitar para no duplicar
            changeScene(previousScene, false);
            return true;
        }

        return false;
    }

    /**
     * Crea una nueva ventana con la escena especificada.
     *
     * @param fxml  Ruta al archivo FXML
     * @param title Título de la ventana
     * @return La Stage (ventana) creada
     */
    public static Stage openNewWindow(String fxml, String title) {
        try {
            Stage stage = new Stage();
            Parent root = loadFXML(fxml);
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            return stage;
        } catch (IOException e) {
            showError("Error al abrir ventana", "No se pudo cargar el archivo FXML: " + e.getMessage());
            return null;
        }
    }

    /**
     * Navega a una nueva escena a partir de un nodo de la escena actual.
     * 
     * @param fxmlFile   La ruta al archivo FXML de la nueva escena
     * @param sourceNode Un nodo de la escena actual para obtener la ventana
     * @throws IOException Si hay un error al cargar el archivo FXML
     */
    public static void navigateTo(String fxmlFile, Node sourceNode) throws IOException {
        navigateTo(fxmlFile, sourceNode, true);
    }

    /**
     * Navega a una nueva escena a partir de un nodo de la escena actual.
     * 
     * @param fxmlFile     La ruta al archivo FXML de la nueva escena
     * @param sourceNode   Un nodo de la escena actual para obtener la ventana
     * @param addToHistory Si debe agregarse al historial de navegación
     * @throws IOException Si hay un error al cargar el archivo FXML
     */
    public static void navigateTo(String fxmlFile, Node sourceNode, boolean addToHistory) throws IOException {
        // Asegurarse de que la ruta tenga la extensión correcta
        if (!fxmlFile.endsWith(".fxml")) {
            fxmlFile = fxmlFile + ".fxml";
        }

        // Si la ruta no comienza con interfaces/, agregarla
        if (!fxmlFile.startsWith("interfaces/")) {
            fxmlFile = "interfaces/" + fxmlFile;
        }

        // Obtener dimensiones actuales de la ventana
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();

        // Cargar la nueva escena
        Parent root = loadFXML(fxmlFile);
        Scene scene = new Scene(root);
        stage.setScene(scene);

        // Mantener el tamaño actual de la ventana
        if (currentWidth > 0 && currentHeight > 0) {
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
        } else {
            // Si no hay un tamaño previo, usar el tamaño estándar
            stage.setWidth(WINDOW_WIDTH);
            stage.setHeight(WINDOW_HEIGHT);
        }

        // Agregar la escena al historial si se solicita
        if (addToHistory) {
            pushScene(fxmlFile);
        }
    }

    /**
     * Cierra la sesión actual del usuario y vuelve a la pantalla de inicio de
     * sesión.
     * Este método limpia el historial de navegación y reinicia la aplicación.
     *
     * @return true si se cerró la sesión correctamente
     */
    public static boolean logOut() {
        try {
            // Limpiar el historial de navegación
            sceneHistory.clear();

            // Usar el método de la clase App para reiniciar la aplicación
            return App.restartApp();
        } catch (Exception e) {
            showError("Error al cerrar sesión", "Ocurrió un problema al cerrar la sesión: " + e.getMessage());
            return false;
        }
    }
}