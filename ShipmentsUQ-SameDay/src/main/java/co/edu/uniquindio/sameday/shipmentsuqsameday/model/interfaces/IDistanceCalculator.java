package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;

/**
 * Interfaz para el cálculo de distancias entre direcciones
 * usando un sistema de coordenadas cartesianas (X,Y)
 */
public interface IDistanceCalculator {
    /**
     * Calcula la distancia entre dos direcciones usando sus coordenadas cartesianas
     * @param origin dirección de origen
     * @param destination dirección de destino
     * @return distancia en unidades de cuadrícula
     * @throws IllegalArgumentException si alguna de las direcciones es null
     */
    double calculateDistance(Address origin, Address destination);
}