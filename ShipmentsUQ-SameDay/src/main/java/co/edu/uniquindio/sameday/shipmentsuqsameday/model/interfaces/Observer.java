package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

import java.io.Serializable;

/**
 * Base interface for observers (Observer pattern)
 */
public interface Observer extends Serializable {
    /**
     * Metodo para actualizar al observador sobre un evento ocurrido
     * @param event tipo de evento que ocurrió
     * @param data datos asociados al evento
     */
    void update(String event, Object data);
}