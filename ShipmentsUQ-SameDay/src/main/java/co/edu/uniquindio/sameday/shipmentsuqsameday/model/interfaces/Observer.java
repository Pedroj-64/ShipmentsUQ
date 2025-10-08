package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

import java.io.Serializable;

/**
 * Base interface for observers (Observer pattern)
 */
public interface Observer extends Serializable {
    /**
     * Method called when an event occurs in the observed object
     * @param event type of event that occurred
     * @param data data associated with the event
     */
    void update(String event, Object data);
}