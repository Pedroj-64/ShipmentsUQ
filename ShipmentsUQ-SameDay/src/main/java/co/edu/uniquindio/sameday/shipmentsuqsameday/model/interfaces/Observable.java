package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

import java.io.Serializable;

/**
 * Interfaz base para objetos observables (patrón Observer)
 */
public interface Observable extends Serializable {
    /**
     * Registrar un observerador
     * @param observer observador a agregar
     */
    void registerObserver(Observer observer);

    /**
     * Eliminar un observador registrado
     * @param observer observador a eliminar
     */
    void removeObserver(Observer observer);

    /**
     * Notificar a todos los observadores registrados
     * @param evento tipo de evento ocurrido
     * @param data datos asociados al evento
     */
    void notifyObservers(String event, Object data);
}