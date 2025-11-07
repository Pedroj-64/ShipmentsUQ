package co.edu.uniquindio.sameday.shipmentsuqsameday.internalController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Clase de utilidad con métodos estáticos para la gestión de interfaces y
 * alertas.
 * Centraliza la lógica común para cambios de escena y notificaciones al
 * usuario.
 */
public class AppUtils {
    
    /**
     * Conversor para mostrar direcciones en ComboBox
     */
    public static class AddressDTOStringConverter extends StringConverter<AddressDTO> {
        @Override
        public String toString(AddressDTO addressDTO) {
            if (addressDTO == null) {
                return "";
            }
            return addressDTO.getAlias() + " - " + addressDTO.getStreet() + ", " + addressDTO.getCity();
        }

        @Override
        public AddressDTO fromString(String string) {
            return null;
        }
    }

    private static Scene currentScene;
    private static final Stack<String> sceneHistory = new Stack<>();
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
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

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

            if (currentScene != null && currentScene.getWindow() != null) {
                currentScene.setRoot(root);


                if (currentScene.getWindow().getWidth() == 0) {
                    currentScene.getWindow().setWidth(WINDOW_WIDTH);
                    currentScene.getWindow().setHeight(WINDOW_HEIGHT);
                }

       
                if (addToHistory) {
                    pushScene(fxml);
                }
            } else {
           
                createNewStageWithScene(fxml, addToHistory);
            }
        } catch (IOException e) {
            showAlert("Error al cambiar la vista", "No se pudo cargar el archivo FXML: " + e.getMessage(),
                    AlertType.ERROR);
        }
    }
    
    /**
     * Crea una nueva ventana con la escena especificada.
     * Este método se utiliza como respaldo cuando no hay una escena actual válida.
     * 
     * @param fxml El nombre del archivo FXML a cargar
     * @param addToHistory Si se debe agregar esta escena al historial de navegación
     */
    private static void createNewStageWithScene(String fxml, boolean addToHistory) {
        try {
            System.out.println("Creando nueva ventana para la escena: " + fxml);
            

            boolean isLoginScreen = fxml.contains("Login");
            List<Stage> stagesToClose = new ArrayList<>();
            
            if (isLoginScreen) {
                for (Window window : Window.getWindows()) {
                    if (window instanceof Stage && window.isShowing()) {
                        stagesToClose.add((Stage) window);
                    }
                }
                System.out.println("Se cerrará(n) " + stagesToClose.size() + " ventana(s) después de abrir login");
            }
            
            Parent root = loadFXML(fxml);
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            Stage stage = new Stage();
            stage.setScene(scene);
            
            currentScene = scene;
            
            if (fxml.contains("Login")) {
                stage.setTitle("ShipmentsUQ - Inicio de Sesión");
            } else if (fxml.contains("Register")) {
                stage.setTitle("ShipmentsUQ - Registro");
            } else if (fxml.contains("Admin")) {
                stage.setTitle("ShipmentsUQ - Panel de Administración");
            } else if (fxml.contains("User")) {
                stage.setTitle("ShipmentsUQ - Panel de Usuario");
            } else {
                stage.setTitle("ShipmentsUQ");
            }
            
            stage.show();
            
            if (isLoginScreen) {
                for (Stage stageToClose : stagesToClose) {
                    if (stageToClose != null && stageToClose != stage) {
                        stageToClose.close();
                    }
                }
                System.out.println("Se cerraron las ventanas anteriores al mostrar login");
            }
            
            if (addToHistory) {
                pushScene(fxml);
            }
            
            System.out.println("Nueva ventana creada exitosamente para: " + fxml);
        } catch (Exception e) {
            showError("Error al crear nueva ventana", "No se pudo crear una nueva ventana: " + e.getMessage());
            e.printStackTrace();
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
        String fxmlPath = fxml.startsWith("interfaces/") || fxml.startsWith("/") ? fxml : "interfaces/" + fxml;
        if (!fxmlPath.endsWith(".fxml")) {
            fxmlPath = fxmlPath + ".fxml";
        }

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxmlPath));
        return fxmlLoader.load(); 
    }

    /**
     * Muestra una alerta con el mensaje especificado.
     * 
     * @param title   El título de la alerta.
     * @param message El contenido del mensaje.
     * @param type    El tipo de alerta.
     */
    public static void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
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
        if (!sceneHistory.isEmpty()) {
            sceneHistory.pop();
        }

        if (!sceneHistory.isEmpty()) {
            String previousScene = sceneHistory.pop();
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
     * @param sourceNode   Un nodo de la escena actual para obtener la ventana (puede ser null si se usa la escena actual)
     * @param addToHistory Si debe agregarse al historial de navegación
     * @throws IOException Si hay un error al cargar el archivo FXML
     */
    public static void navigateTo(String fxmlFile, Node sourceNode, boolean addToHistory) throws IOException {
        if (!fxmlFile.endsWith(".fxml")) {
            fxmlFile = fxmlFile + ".fxml";
        }

        if (!fxmlFile.startsWith("interfaces/")) {
            fxmlFile = "interfaces/" + fxmlFile;
        }

        double currentWidth = WINDOW_WIDTH;
        double currentHeight = WINDOW_HEIGHT;
        Stage stage = null;
        
 
        if (sourceNode != null && sourceNode.getScene() != null) {
            stage = (Stage) sourceNode.getScene().getWindow();
            if (stage != null) {
                currentWidth = stage.getWidth();
                currentHeight = stage.getHeight();
            }
        } 
        

        if (stage == null && currentScene != null && currentScene.getWindow() != null) {
            stage = (Stage) currentScene.getWindow();
            if (stage != null) {
                currentWidth = stage.getWidth();
                currentHeight = stage.getHeight();
            }
        } 
        
        
        if (stage == null) {
            // Intentar obtener el stage principal a través de las ventanas abiertas
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    stage = (Stage) window;
                    if (stage.getWidth() > 0) {
                        currentWidth = stage.getWidth();
                    }
                    if (stage.getHeight() > 0) {
                        currentHeight = stage.getHeight();
                    }
                    break;
                }
            }
            
            if (stage == null) {
                stage = new Stage();
                System.out.println("Advertencia: Se ha creado un nuevo Stage porque no se pudo encontrar uno existente.");
            }
        }

   
        Parent root = loadFXML(fxmlFile);
        Scene scene = new Scene(root);
        stage.setScene(scene);


        if (currentWidth > 0 && currentHeight > 0) {
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
        } else {

            stage.setWidth(WINDOW_WIDTH);
            stage.setHeight(WINDOW_HEIGHT);
        }


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
            System.out.println("Iniciando proceso de cierre de sesión...");
            
            sceneHistory.clear();
            
            if (App.getCurrentSession() != null) {
                App.getCurrentSession().logout();
                System.out.println("Sesión cerrada con éxito");
            } else {
                System.out.println("No había sesión activa para cerrar");
            }
            
            boolean sceneValid = (currentScene != null && currentScene.getWindow() != null);
            System.out.println("Estado de la escena actual: " + (sceneValid ? "Válida" : "No válida"));

            List<Stage> openStages = new ArrayList<>();
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    openStages.add((Stage) window);
                }
            }
            System.out.println("Se encontraron " + openStages.size() + " ventanas abiertas");
            
            try {
                boolean appRestarted = restartApp();
                if (appRestarted) {
                    System.out.println("Aplicación reiniciada con éxito usando restartApp()");
                    return true;
                } else {
                    System.out.println("No se pudo reiniciar la aplicación con restartApp(), intentando navegación directa");
                }
            } catch (Exception ex) {
                System.out.println("Error al intentar usar restartApp(): " + ex.getMessage());
            }
            

            Stage loginStage = new Stage();
            try {
                Parent root = loadFXML("interfaces/Login.fxml");
                Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
                loginStage.setScene(scene);
                loginStage.setTitle("ShipmentsUQ - Inicio de Sesión");
                
                currentScene = scene;
                
                loginStage.show();
                System.out.println("Nueva ventana de login creada con éxito");
                
                for (Stage stage : openStages) {
                    if (stage != null && stage != loginStage) {
                        stage.close();
                        System.out.println("Se cerró una ventana anterior");
                    }
                }
                
                return true;
            } catch (Exception ex) {
                System.out.println("Error al crear ventana de login: " + ex.getMessage());
                
                try {
                    navigateTo("Login.fxml", null);
                    System.out.println("Navegación a Login.fxml completada mediante navigateTo");
                } catch (Exception ex2) {
                    System.out.println("Error en navegación directa: " + ex2.getMessage());
                    
                    try {
                        System.out.println("Intentando crear una nueva ventana de login con createNewStageWithScene...");
                        createNewStageWithScene("interfaces/Login.fxml", false);
                        System.out.println("Nueva ventana de login creada con éxito");
                    } catch (Exception ex3) {
                        System.err.println("Error al crear nueva ventana de login: " + ex3.getMessage());
                        ex3.printStackTrace();
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            showError("Error al cerrar sesión", "Ocurrió un problema al cerrar la sesión: " + e.getMessage());
            System.err.println("Error durante el cierre de sesión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene el ID del usuario actual desde la sesión
     * En un caso real, esto debería obtenerse de un sistema de gestión de sesiones
     * 
     * @return String con el ID del usuario actual o null si no hay sesión
     */
    public static String getCurrentUserId() {
        try {
            return App.getCurrentSession().getUserId().toString();
        } catch (Exception e) {
            System.err.println("Error al obtener ID de usuario: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Reinicia la aplicación volviendo a la pantalla de inicio de sesión.
     * Este método cierra todas las ventanas existentes y abre una nueva ventana de login.
     * Guarda el estado actual antes de reiniciar.
     * 
     * @return true si se pudo reiniciar correctamente
     */
    public static boolean restartApp() {
        try {
            System.out.println("Reiniciando aplicación desde AppUtils...");
            
            try {
                if (App.getCurrentSession() != null) {
                    App.getCurrentSession().logout();
                    System.out.println("Sesión cerrada correctamente");
                }
            } catch (Exception e) {
                System.out.println("Error al cerrar sesión: " + e.getMessage());

            }
            
            // Capturar todas las ventanas abiertas
            List<Stage> stages = new ArrayList<>();
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    stages.add((Stage) window);
                }
            }
            System.out.println("Se encontraron " + stages.size() + " ventanas abiertas para cerrar");
            
            Stage newStage = new Stage();
            Parent root = loadFXML("interfaces/Login.fxml");
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            newStage.setScene(scene);
            newStage.setTitle("ShipmentsUQ - Inicio de Sesión");
            
            setCurrentScene(scene);
            
            newStage.show();
            System.out.println("Nueva ventana de login creada");
            
            for (Stage stage : stages) {
                if (stage != null && stage != newStage) {
                    stage.close();
                    System.out.println("Se cerró una ventana anterior");
                }
            }
            
            return true;
        } catch (Exception e) {
            showError("Error al reiniciar", "No se pudo volver a la pantalla de inicio: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}