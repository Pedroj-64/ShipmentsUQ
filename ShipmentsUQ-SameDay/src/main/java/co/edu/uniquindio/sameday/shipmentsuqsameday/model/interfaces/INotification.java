package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

/**
 * Interfaz para el sistema de notificaciones
 */
public interface INotification {
    /**
     * Envía una notificación por correo electrónico
     * @param recipient correo del destinatario
     * @param subject asunto del correo
     * @param message contenido del mensaje
     * @return true si el envío fue exitoso
     */
    boolean sendEmail(String recipient, String subject, String message);

    /**
     * Envía una notificación por SMS
     * @param phone número de teléfono del destinatario
     * @param message contenido del mensaje
     * @return true si el envío fue exitoso
     */
    boolean sendSMS(String phone, String message);

    /**
     * Envía una notificación push
     * @param deviceToken token del dispositivo
     * @param title título de la notificación
     * @param message contenido del mensaje
     * @return true si el envío fue exitoso
     */
    boolean sendPushNotification(String deviceToken, String title, String message);
}