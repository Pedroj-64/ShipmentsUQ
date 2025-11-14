package co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.util.ThemeManager;
import co.edu.uniquindio.sameday.shipmentsuqsameday.util.UserPreferences;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * View controller for application settings.
 * Manages user preferences including theme selection, notifications, and privacy settings.
 * 
 * @author ShipmentsUQ Team
 * @version 1.0
 */
public class SettingsViewController implements Initializable {
    
    @FXML private RadioButton rb_lightTheme;
    @FXML private RadioButton rb_darkTheme;
    @FXML private ToggleGroup themeToggleGroup;
    @FXML private CheckBox cb_emailNotifications;
    @FXML private CheckBox cb_smsNotifications;
    @FXML private CheckBox cb_shareLocation;
    @FXML private Button btn_save;
    @FXML private Button btn_cancel;
    
    private UserPreferences preferences;
    private ThemeManager themeManager;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        preferences = UserPreferences.getInstance();
        themeManager = ThemeManager.getInstance();
        
        // Cargar preferencias actuales
        loadCurrentPreferences();
        
        // Configurar botones
        btn_save.setOnAction(event -> savePreferences());
        btn_cancel.setOnAction(event -> closeWindow());
    }
    
    /**
     * Loads current user preferences from persistent storage.
     * Sets the UI controls to reflect the saved preferences for theme, notifications, and privacy.
     */
    private void loadCurrentPreferences() {
        // Cargar tema
        String currentTheme = preferences.getTheme();
        if ("dark".equals(currentTheme)) {
            rb_darkTheme.setSelected(true);
        } else {
            rb_lightTheme.setSelected(true);
        }
        
        // Cargar notificaciones
        cb_emailNotifications.setSelected(preferences.isEmailNotificationsEnabled());
        cb_smsNotifications.setSelected(preferences.isSmsNotificationsEnabled());
        cb_shareLocation.setSelected(preferences.isShareLocationEnabled());
    }
    
    /**
     * Saves the selected preferences to persistent storage.
     * Applies theme changes immediately to all registered scenes.
     * Displays confirmation message upon successful save.
     */
    private void savePreferences() {
        try {
            // Guardar tema
            String selectedTheme = rb_darkTheme.isSelected() ? "dark" : "light";
            String previousTheme = preferences.getTheme();
            
            preferences.setTheme(selectedTheme);
            
            // Guardar notificaciones
            preferences.setEmailNotificationsEnabled(cb_emailNotifications.isSelected());
            preferences.setSmsNotificationsEnabled(cb_smsNotifications.isSelected());
            preferences.setShareLocationEnabled(cb_shareLocation.isSelected());
            
            // Guardar en archivo
            preferences.savePreferences();
            
            // Aplicar tema inmediatamente si cambió
            if (!selectedTheme.equals(previousTheme)) {
                themeManager.setTheme(selectedTheme);
                showInfo("Tema Aplicado", 
                    "El tema se ha aplicado correctamente.\n" +
                    "Algunas ventanas pueden necesitar ser reabiertas para ver todos los cambios.");
            } else {
                showInfo("Configuración Guardada", 
                    "Tus preferencias han sido guardadas correctamente.");
            }
            
            closeWindow();
            
        } catch (Exception e) {
            showError("Error al Guardar", 
                "No se pudieron guardar las preferencias: " + e.getMessage());
        }
    }
    
    /**
     * Closes the settings window.
     */
    private void closeWindow() {
        Stage stage = (Stage) btn_cancel.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Displays an information dialog to the user.
     * 
     * @param title the dialog title
     * @param message the message content
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Displays an error dialog to the user.
     * 
     * @param title the dialog title
     * @param message the error message content
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
