package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IDistanceCalculator;

/**
 * Calculador de distancia euclidiana para un sistema de coordenadas cartesianas
 * Reemplaza el cálculo de distancias basado en coordenadas geográficas (Haversine)
 */
public class EuclideanDistanceCalculator implements IDistanceCalculator {

    /**
     * Calcula la distancia euclidiana entre dos direcciones utilizando sus coordenadas X e Y
     * @param origin dirección de origen
     * @param destination dirección de destino
     * @return distancia euclidiana entre las dos direcciones (en unidades de la cuadrícula)
     */
    @Override
    public double calculateDistance(Address origin, Address destination) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Las direcciones no pueden ser nulas");
        }
        
        // Distancia euclidiana: sqrt((x2-x1)² + (y2-y1)²)
        double deltaX = destination.getCoordX() - origin.getCoordX();
        double deltaY = destination.getCoordY() - origin.getCoordY();
        
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * Estima el tiempo de viaje en minutos basado en la distancia euclidiana
     * @param distance distancia en unidades de cuadrícula
     * @param speedFactor factor de velocidad (unidades por minuto)
     * @return tiempo estimado en minutos
     */
    public int estimateTravelTime(double distance, double speedFactor) {
        return (int) Math.ceil(distance / speedFactor);
    }
}