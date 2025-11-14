package co.edu.uniquindio.sameday.shipmentsuqsameday.util;

import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Application theme manager.
 * Provides functionality to switch between light and dark themes dynamically.
 * Manages theme application across multiple scenes and persists theme preference.
 * Implements singleton pattern.
 * 
 * @author ShipmentsUQ Team
 * @version 1.0
 */
public class ThemeManager {
    
    private static ThemeManager instance;
    private String currentTheme;
    private final List<Scene> registeredScenes;
    
    // Rutas de los CSS de temas
    private static final String DARK_THEME_CSS = "/co/edu/uniquindio/sameday/shipmentsuqsameday/css/dark-theme.css";
    
    private ThemeManager() {
        registeredScenes = new ArrayList<>();
        currentTheme = UserPreferences.getInstance().getTheme();
    }
    
    /**
     * Gets the singleton instance of the theme manager.
     * 
     * @return the unique ThemeManager instance
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Registers a scene for theme management.
     * Applies the current theme to the scene immediately.
     * 
     * @param scene the JavaFX scene to register
     */
    public void registerScene(Scene scene) {
        if (scene != null && !registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyThemeToScene(scene, currentTheme);
        }
    }
    
    /**
     * Unregisters a scene from theme management.
     * 
     * @param scene the JavaFX scene to unregister
     */
    public void unregisterScene(Scene scene) {
        registeredScenes.remove(scene);
    }
    
    /**
     * Aplica un tema a una escena específica
     */
    private void applyThemeToScene(Scene scene, String theme) {
        if (scene == null) return;
        
        // Remover tema oscuro si existe
        scene.getStylesheets().removeIf(css -> css.contains("dark-theme.css"));
        
        // Aplicar tema oscuro si es necesario
        if ("dark".equals(theme)) {
            String darkThemeUrl = getClass().getResource(DARK_THEME_CSS).toExternalForm();
            scene.getStylesheets().add(darkThemeUrl);
            System.out.println("[THEME] Dark theme applied");
        } else {
            System.out.println("[THEME] Light theme applied");
        }
    }
    
    /**
     * Changes the theme for all registered scenes.
     * 
     * @param theme the theme name ("light" or "dark")
     */
    public void setTheme(String theme) {
        if (theme == null || theme.isEmpty()) {
            theme = "light";
        }
        
        this.currentTheme = theme;
        
        // Aplicar a todas las escenas registradas
        for (Scene scene : registeredScenes) {
            applyThemeToScene(scene, theme);
        }
        
        // Guardar preferencia
        UserPreferences.getInstance().setTheme(theme);
        UserPreferences.getInstance().savePreferences();
        
        System.out.println("[THEME] Theme changed to: " + theme);
    }
    
    /**
     * Obtiene el tema actual
     */
    public String getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Verifica si el tema oscuro está activo
     */
    public boolean isDarkTheme() {
        return "dark".equals(currentTheme);
    }
    
    /**
     * Aplica el tema guardado a una nueva escena
     * @param scene escena a la que aplicar el tema
     */
    public void applyCurrentTheme(Scene scene) {
        registerScene(scene);
    }
}
