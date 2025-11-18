package co.edu.uniquindio.sameday.shipmentsuqsameday.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * User preferences manager.
 * Handles loading and saving of user configuration settings including theme, notifications, and privacy options.
 * Implements singleton pattern to ensure single instance across application.
 * 
 * @author Maria Jose Valencia
 * @version 1.0
 */
public class UserPreferences {
    
    private static final String PREFERENCES_FILE = "user_preferences.properties";
    private static final String PREFERENCES_DIR = System.getProperty("user.home") + "/.shipmentsuq/";
    private static final String PREFERENCES_PATH = PREFERENCES_DIR + PREFERENCES_FILE;
    
    private static UserPreferences instance;
    private Properties properties;
    
    // Claves de preferencias
    public static final String THEME = "theme";
    public static final String EMAIL_NOTIFICATIONS = "email_notifications";
    public static final String SMS_NOTIFICATIONS = "sms_notifications";
    public static final String SHARE_LOCATION = "share_location";
    
    // Valores por defecto
    public static final String DEFAULT_THEME = "light";
    public static final boolean DEFAULT_EMAIL_NOTIFICATIONS = true;
    public static final boolean DEFAULT_SMS_NOTIFICATIONS = false;
    public static final boolean DEFAULT_SHARE_LOCATION = true;
    
    private UserPreferences() {
        properties = new Properties();
        loadPreferences();
    }
    
    /**
     * Gets the singleton instance of the preferences manager.
     * 
     * @return the unique UserPreferences instance
     */
    public static synchronized UserPreferences getInstance() {
        if (instance == null) {
            instance = new UserPreferences();
        }
        return instance;
    }
    
    /**
     * Loads preferences from the properties file.
     * Creates default preferences if file does not exist.
     */
    private void loadPreferences() {
        File prefsFile = new File(PREFERENCES_PATH);
        
        if (prefsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(prefsFile)) {
                properties.load(fis);
                System.out.println("[INFO] Preferences loaded from: " + PREFERENCES_PATH);
            } catch (IOException e) {
                System.err.println("[WARN] Error loading preferences: " + e.getMessage());
                setDefaultPreferences();
            }
        } else {
            System.out.println("[INFO] Preferences file not found. Creating default preferences...");
            setDefaultPreferences();
            savePreferences();
        }
    }
    
    /**
     * Establece las preferencias por defecto
     */
    private void setDefaultPreferences() {
        properties.setProperty(THEME, DEFAULT_THEME);
        properties.setProperty(EMAIL_NOTIFICATIONS, String.valueOf(DEFAULT_EMAIL_NOTIFICATIONS));
        properties.setProperty(SMS_NOTIFICATIONS, String.valueOf(DEFAULT_SMS_NOTIFICATIONS));
        properties.setProperty(SHARE_LOCATION, String.valueOf(DEFAULT_SHARE_LOCATION));
    }
    
    /**
     * Guarda las preferencias en el archivo
     */
    public void savePreferences() {
        File prefsDir = new File(PREFERENCES_DIR);
        if (!prefsDir.exists()) {
            prefsDir.mkdirs();
        }
        
        try (FileOutputStream fos = new FileOutputStream(PREFERENCES_PATH)) {
            properties.store(fos, "ShipmentsUQ User Preferences");
            System.out.println("[INFO] Preferences saved to: " + PREFERENCES_PATH);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save preferences: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene una preferencia de tipo String
     */
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Obtiene una preferencia de tipo boolean
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Establece una preferencia de tipo String
     */
    public void setString(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Establece una preferencia de tipo boolean
     */
    public void setBoolean(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
    }
    
    /**
     * Obtiene el tema actual
     */
    public String getTheme() {
        return getString(THEME, DEFAULT_THEME);
    }
    
    /**
     * Establece el tema
     */
    public void setTheme(String theme) {
        setString(THEME, theme);
    }
    
    /**
     * Verifica si las notificaciones por email están activadas
     */
    public boolean isEmailNotificationsEnabled() {
        return getBoolean(EMAIL_NOTIFICATIONS, DEFAULT_EMAIL_NOTIFICATIONS);
    }
    
    /**
     * Establece si las notificaciones por email están activadas
     */
    public void setEmailNotificationsEnabled(boolean enabled) {
        setBoolean(EMAIL_NOTIFICATIONS, enabled);
    }
    
    /**
     * Verifica si las notificaciones por SMS están activadas
     */
    public boolean isSmsNotificationsEnabled() {
        return getBoolean(SMS_NOTIFICATIONS, DEFAULT_SMS_NOTIFICATIONS);
    }
    
    /**
     * Establece si las notificaciones por SMS están activadas
     */
    public void setSmsNotificationsEnabled(boolean enabled) {
        setBoolean(SMS_NOTIFICATIONS, enabled);
    }
    
    /**
     * Verifica si compartir ubicación está activado
     */
    public boolean isShareLocationEnabled() {
        return getBoolean(SHARE_LOCATION, DEFAULT_SHARE_LOCATION);
    }
    
    /**
     * Establece si compartir ubicación está activado
     */
    public void setShareLocationEnabled(boolean enabled) {
        setBoolean(SHARE_LOCATION, enabled);
    }
}
