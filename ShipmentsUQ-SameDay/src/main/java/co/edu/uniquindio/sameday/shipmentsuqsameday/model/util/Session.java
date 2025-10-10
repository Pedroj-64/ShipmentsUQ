package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import java.util.UUID;

/**
 * Clase que maneja la información de la sesión actual del usuario.
 * Implementa el patrón Singleton para garantizar una única instancia en toda la aplicación.
 */
public class Session {
    private static Session instance;
    private UUID userId;
    
    /**
     * Constructor privado para evitar instanciación externa
     */
    private Session() {
        // Constructor privado para Singleton
    }
    
    /**
     * Obtiene la instancia única de Session
     * @return la instancia de Session
     */
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    
    /**
     * Establece el ID del usuario actual
     * @param userId ID del usuario
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    /**
     * Obtiene el ID del usuario actual
     * @return ID del usuario o null si no hay sesión
     */
    public UUID getUserId() {
        return userId;
    }
    
    /**
     * Verifica si hay una sesión activa
     * @return true si hay un usuario con sesión activa
     */
    public boolean isLoggedIn() {
        return userId != null;
    }
    
    /**
     * Cierra la sesión actual
     */
    public void logout() {
        userId = null;
    }
}