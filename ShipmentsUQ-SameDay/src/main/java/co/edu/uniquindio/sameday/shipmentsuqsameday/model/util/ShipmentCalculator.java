package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;

/**
 * Utilidad para cálculos relacionados con envíos
 * Adaptada para trabajar con coordenadas cartesianas
 */
public class ShipmentCalculator {
    private static final int MINUTES_PER_UNIT = 2;  // 2 minutos por unidad de distancia base
    private static final double URGENT_TIME_MULTIPLIER = 0.7;  // 30% más rápido
    private static final double PRIORITY_TIME_MULTIPLIER = 0.85;  // 15% más rápido
    //**
    //*Esto teniendo en cuenta que el veneco no se quede en un trancon, mentira
    //*/

    /**
     * Calcula el tiempo estimado de entrega en minutos basado en la distancia euclidiana y prioridad
     * @param shipment envío a calcular
     * @return tiempo estimado en minutos
     */
    public static int calculateEstimatedTime(Shipment shipment) {
        if (shipment.getOrigin() == null || shipment.getDestination() == null) {
            throw new IllegalArgumentException("El envío debe tener origen y destino definidos");
        }

        // Obtener la distancia usando el calculador euclidiano
        EuclideanDistanceCalculator calculator = new EuclideanDistanceCalculator();
        double distance = calculator.calculateDistance(shipment.getOrigin(), shipment.getDestination());
        
        // Calcular tiempo base
        int baseTime = (int) (distance * MINUTES_PER_UNIT);
        
        // Aplicar multiplicador según prioridad
        return switch (shipment.getPriority()) {
            case URGENT -> (int) (baseTime * URGENT_TIME_MULTIPLIER);
            case PRIORITY -> (int) (baseTime * PRIORITY_TIME_MULTIPLIER);
            default -> baseTime;
        };
    }
}