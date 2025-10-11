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
    
    /**
     * Conversor para mostrar direcciones en ComboBox
     */
    public static class AddressDTOStringConverter extends javafx.util.StringConverter<co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO> {
        @Override
        public String toString(co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO addressDTO) {
            if (addressDTO == null) {
                return "";
            }
            // Mostrar alias y calle
            return addressDTO.getAlias() + " - " + addressDTO.getStreet() + ", " + addressDTO.getCity();
        }

        @Override
        public co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO fromString(String string) {
            // Este método se usa para la edición del ComboBox, lo cual no permitimos
            return null;
        }
    }

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

            if (currentScene != null && currentScene.getWindow() != null) {
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
                // Si no hay escena actual válida, intentar crear una nueva ventana
                createNewStageWithScene(fxml, addToHistory);
            }
        } catch (IOException e) {
            showAlert("Error al cambiar la vista", "No se pudo cargar el archivo FXML: " + e.getMessage(),
                    Alert.AlertType.ERROR);
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
            
            // Si es una pantalla de login, recopilar referencias a ventanas existentes para cerrarlas después
            boolean isLoginScreen = fxml.contains("Login");
            java.util.List<Stage> stagesToClose = new java.util.ArrayList<>();
            
            if (isLoginScreen) {
                // Recopilar todas las ventanas abiertas para cerrarlas después
                for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                    if (window instanceof Stage && window.isShowing()) {
                        stagesToClose.add((Stage) window);
                    }
                }
                System.out.println("Se cerrará(n) " + stagesToClose.size() + " ventana(s) después de abrir login");
            }
            
            // Crear la nueva ventana
            Parent root = loadFXML(fxml);
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            Stage stage = new Stage();
            stage.setScene(scene);
            
            // Actualizar la referencia a la escena actual
            currentScene = scene;
            
            // Establecer el título según el tipo de pantalla
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
            
            // Mostrar la nueva ventana
            stage.show();
            
            // Si era una pantalla de login, cerrar todas las ventanas anteriores
            if (isLoginScreen) {
                for (Stage stageToClose : stagesToClose) {
                    if (stageToClose != null && stageToClose != stage) {
                        stageToClose.close();
                    }
                }
                System.out.println("Se cerraron las ventanas anteriores al mostrar login");
            }
            
            // Agregar la escena al historial si se solicita
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
     * @param sourceNode   Un nodo de la escena actual para obtener la ventana (puede ser null si se usa la escena actual)
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

        // Variables para almacenar las dimensiones actuales
        double currentWidth = WINDOW_WIDTH;
        double currentHeight = WINDOW_HEIGHT;
        Stage stage = null;
        
        // Intentar obtener el stage de varias fuentes
        if (sourceNode != null && sourceNode.getScene() != null) {
            // Obtener dimensiones de la ventana actual a través del nodo
            stage = (Stage) sourceNode.getScene().getWindow();
            if (stage != null) {
                currentWidth = stage.getWidth();
                currentHeight = stage.getHeight();
            }
        } 
        
        // Si no se pudo obtener el stage del sourceNode, intentar con currentScene
        if (stage == null && currentScene != null && currentScene.getWindow() != null) {
            // Si no hay nodo pero hay escena actual, usar esa
            stage = (Stage) currentScene.getWindow();
            if (stage != null) {
                currentWidth = stage.getWidth();
                currentHeight = stage.getHeight();
            }
        } 
        
        // Si aún no hay stage, intentar buscar algún stage activo en la aplicación
        if (stage == null) {
            // Intentar obtener el stage principal a través de las ventanas abiertas
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
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
            
            // Si aún no hay stage, crear uno nuevo
            if (stage == null) {
                stage = new Stage();
                System.out.println("Advertencia: Se ha creado un nuevo Stage porque no se pudo encontrar uno existente.");
            }
        }

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
            System.out.println("Iniciando proceso de cierre de sesión...");
            
            // Limpiar el historial de navegación
            sceneHistory.clear();
            
            // Cerrar la sesión en la aplicación
            if (App.getCurrentSession() != null) {
                App.getCurrentSession().logout();
                System.out.println("Sesión cerrada con éxito");
            } else {
                System.out.println("No había sesión activa para cerrar");
            }
            
            // Verificar si tenemos una escena válida antes de navegar
            boolean sceneValid = (currentScene != null && currentScene.getWindow() != null);
            System.out.println("Estado de la escena actual: " + (sceneValid ? "Válida" : "No válida"));
            
            // Guardar referencias a todas las ventanas actuales antes de cerrarlas
            java.util.List<Stage> openStages = new java.util.ArrayList<>();
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    openStages.add((Stage) window);
                }
            }
            System.out.println("Se encontraron " + openStages.size() + " ventanas abiertas");
            
            try {
                // Intentar usar el método restartApp local
                boolean appRestarted = restartApp();
                if (appRestarted) {
                    System.out.println("Aplicación reiniciada con éxito usando restartApp()");
                    return true;
                } else {
                    System.out.println("No se pudo reiniciar la aplicación con restartApp(), intentando navegación directa");
                }
            } catch (Exception ex) {
                System.out.println("Error al intentar usar restartApp(): " + ex.getMessage());
                // Continuar con el método alternativo
            }
            
            // Crear una nueva ventana de login
            Stage loginStage = new Stage();
            try {
                // Cargar la pantalla de login
                Parent root = loadFXML("interfaces/Login.fxml");
                Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
                loginStage.setScene(scene);
                loginStage.setTitle("ShipmentsUQ - Inicio de Sesión");
                
                // Actualizar la referencia a la escena actual
                currentScene = scene;
                
                // Mostrar la nueva ventana de login
                loginStage.show();
                System.out.println("Nueva ventana de login creada con éxito");
                
                // Ahora cerrar todas las ventanas anteriores
                for (Stage stage : openStages) {
                    if (stage != null && stage != loginStage) {
                        stage.close();
                        System.out.println("Se cerró una ventana anterior");
                    }
                }
                
                return true;
            } catch (Exception ex) {
                System.out.println("Error al crear ventana de login: " + ex.getMessage());
                
                // Intentar estrategias alternativas si la creación directa falló
                try {
                    // Estrategia 1: Usar navigateTo normal
                    navigateTo("Login.fxml", null);
                    System.out.println("Navegación a Login.fxml completada mediante navigateTo");
                } catch (Exception ex2) {
                    System.out.println("Error en navegación directa: " + ex2.getMessage());
                    
                    try {
                        // Estrategia 2: Crear una nueva ventana con el método existente
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
        // En una implementación real, esto vendría de un sistema de sesión
        // Por ahora, simulamos un usuario logeado
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
            
            // Guardar el estado actual antes de reiniciar
            try {
                if (App.getCurrentSession() != null) {
                    // Cerrar la sesión
                    App.getCurrentSession().logout();
                    System.out.println("Sesión cerrada correctamente");
                }
            } catch (Exception e) {
                System.out.println("Error al cerrar sesión: " + e.getMessage());
                // Continuar con el proceso de reinicio
            }
            
            // Capturar todas las ventanas abiertas
            java.util.List<Stage> stages = new java.util.ArrayList<>();
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    stages.add((Stage) window);
                }
            }
            System.out.println("Se encontraron " + stages.size() + " ventanas abiertas para cerrar");
            
            // Crear una nueva ventana de login
            Stage newStage = new Stage();
            Parent root = loadFXML("interfaces/Login.fxml");
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            newStage.setScene(scene);
            newStage.setTitle("ShipmentsUQ - Inicio de Sesión");
            
            // Actualizar la referencia de la escena actual
            setCurrentScene(scene);
            
            // Mostrar la nueva ventana
            newStage.show();
            System.out.println("Nueva ventana de login creada");
            
            // Cerrar todas las ventanas anteriores
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