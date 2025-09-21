package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;

/**
 * Interfaz para el cálculo de distancias entre direcciones
 */
public interface IDistanceCalculator {
    /**
     * Calcula la distancia entre dos direcciones
     * @param origin dirección de origen
     * @param destination dirección de destino
     * @return distancia en kilómetros
     * @throws IllegalArgumentException si alguna de las direcciones es null
     */
    double calculateDistance(Address origin, Address destination);
}