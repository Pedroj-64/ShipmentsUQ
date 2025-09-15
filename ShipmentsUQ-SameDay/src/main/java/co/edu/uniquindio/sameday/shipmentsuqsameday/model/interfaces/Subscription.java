package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

import java.time.LocalDateTime;

/**
 * Interfaz para definir el comportamiento de una suscripción
 */
public interface Subscription {
    /**
     * Verifica si la suscripción está activa
     * @return true si la suscripción está activa
     */
    boolean isActive();

    /**
     * Obtiene la fecha de vencimiento de la suscripción
     * @return fecha de vencimiento
     */
    LocalDateTime getExpirationDate();

    /**
     * Obtiene el descuento aplicable a la tarifa
     * @return porcentaje de descuento (0.0 a 1.0)
     */
    double getDiscount();

    /**
     * Renueva la suscripción por un período
     * @param months número de meses a renovar
     */
    void renew(int months);
}