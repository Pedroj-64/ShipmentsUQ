package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

/**
 * Interfaz base para objetos observables (patrón Observer)
 */
public interface Observable {
    /**
     * Register a new observer
     * @param observer observer to register
     */
    void registerObserver(Observer observer);

    /**
     * Remove a registered observer
     * @param observer observer to remove
     */
    void removeObserver(Observer observer);

    /**
     * Notify all registered observers
     * @param evento tipo de evento ocurrido
     * @param data datos asociados al evento
     */
    void notifyObservers(String event, Object data);
}