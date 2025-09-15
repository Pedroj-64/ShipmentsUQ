package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

/**
 * Base interface for observers (Observer pattern)
 */
public interface Observer {
    /**
     * Method called when an event occurs in the observed object
     * @param event type of event that occurred
     * @param data data associated with the event
     */
    void update(String event, Object data);
}