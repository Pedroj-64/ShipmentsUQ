package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import java.util.UUID;

/**
 * Clase que maneja la información de la sesión actual del usuario o repartidor.
 * Implementa el patrón Singleton para garantizar una única instancia en toda la aplicación.
 */
public class Session {
    private static Session instance;
    private UUID userId;
    private Deliverer currentDeliverer;
    
    /**
     * Constructor privado para evitar instanciación externa
     */
    private Session() {
        
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
        currentDeliverer = null;
    }
    
    /**
     * Establece el repartidor actual
     * @param deliverer el repartidor que inicia sesión
     */
    public void setCurrentDeliverer(Deliverer deliverer) {
        this.currentDeliverer = deliverer;
    }
    
    /**
     * Obtiene el repartidor actual
     * @return el repartidor actual o null si no hay sesión de repartidor
     */
    public Deliverer getCurrentDeliverer() {
        return currentDeliverer;
    }
    
    /**
     * Verifica si hay un repartidor con sesión activa
     * @return true si hay un repartidor con sesión activa
     */
    public boolean isDelivererLoggedIn() {
        return currentDeliverer != null;
    }
}