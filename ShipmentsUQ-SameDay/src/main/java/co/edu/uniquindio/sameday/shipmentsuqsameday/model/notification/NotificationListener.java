package co.edu.uniquindio.sameday.shipmentsuqsameday.model.notification;

/**
 * Interface del patrón Observer para escuchar notificaciones
 * Los componentes UI que implementen esta interface recibirán
 * notificaciones en tiempo real del sistema
 */
@FunctionalInterface
public interface NotificationListener {
    
    /**
     * Método invocado cuando se crea una nueva notificación
     * 
     * @param notification La notificación recién creada
     */
    void onNotificationReceived(Notification notification);
}
