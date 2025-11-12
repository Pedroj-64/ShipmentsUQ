package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.ShipmentDetails;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.ICoordinateStrategy;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy.GridCoordinateStrategy;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy.RealCoordinateStrategy;

/**
 * Utilidad para cálculos relacionados con envíos
 * Soporta tanto coordenadas de Grid como coordenadas GPS reales
 * 
 * Usa Strategy Pattern para cambiar dinámicamente entre sistemas de coordenadas
 */
public class ShipmentCalculator {
    private static final int MINUTES_PER_UNIT = 2;  // 2 minutos por unidad de distancia base
    private static final double URGENT_TIME_MULTIPLIER = 0.7;  // 30% más rápido
    private static final double PRIORITY_TIME_MULTIPLIER = 0.85;  // 15% más rápido
    
    // Estrategias de coordenadas
    private static final GridCoordinateStrategy gridStrategy = new GridCoordinateStrategy();
    private static final RealCoordinateStrategy realStrategy = new RealCoordinateStrategy();
    
    //**
    //*Esto teniendo en cuenta que el veneco no se quede en un trancon, mentira
    //*/

    /**
     * Calcula el tiempo estimado de entrega en minutos basado en la distancia y prioridad
     * Detecta automáticamente si el envío usa coordenadas Grid o GPS reales
     * 
     * @param shipment envío a calcular
     * @return tiempo estimado en minutos
     */
    public static int calculateEstimatedTime(Shipment shipment) {
        if (shipment.getOrigin() == null || shipment.getDestination() == null) {
            throw new IllegalArgumentException("El envío debe tener origen y destino definidos");
        }

        // Determinar qué estrategia usar y calcular distancia
        double distance;
        ICoordinateStrategy strategy;
        
        // Verificar si el ShipmentDetails tiene coordenadas reales
        if (shipment.getDetails() != null && shipment.getDetails().usesRealCoordinates()) {
            // Usar coordenadas GPS reales
            strategy = realStrategy;
            Coordinates origin = shipment.getDetails().getOriginCoordinates();
            Coordinates destination = shipment.getDetails().getDestinationCoordinates();
            distance = strategy.calculateDistance(
                origin.getLatitude(), origin.getLongitude(),
                destination.getLatitude(), destination.getLongitude()
            );
        } else {
            // Usar sistema de Grid tradicional (compatibilidad con código existente)
            strategy = gridStrategy;
            EuclideanDistanceCalculator calculator = new EuclideanDistanceCalculator();
            distance = calculator.calculateDistance(shipment.getOrigin(), shipment.getDestination());
        }
        
        // Calcular tiempo usando la estrategia seleccionada
        double estimatedTime = strategy.calculateEstimatedTime(distance);
        
        // Aplicar multiplicador según prioridad
        return switch (shipment.getPriority()) {
            case URGENT -> (int) (estimatedTime * URGENT_TIME_MULTIPLIER);
            case PRIORITY -> (int) (estimatedTime * PRIORITY_TIME_MULTIPLIER);
            default -> (int) estimatedTime;
        };
    }
    
    /**
     * Calcula el costo de un envío basándose en la distancia
     * Detecta automáticamente el sistema de coordenadas a usar
     * 
     * @param shipment envío a calcular
     * @return costo del envío en COP
     */
    public static double calculateCost(Shipment shipment) {
        if (shipment.getOrigin() == null || shipment.getDestination() == null) {
            throw new IllegalArgumentException("El envío debe tener origen y destino definidos");
        }
        
        // Determinar estrategia y calcular distancia
        double distance;
        ICoordinateStrategy strategy;
        
        if (shipment.getDetails() != null && shipment.getDetails().usesRealCoordinates()) {
            // Coordenadas GPS reales
            strategy = realStrategy;
            Coordinates origin = shipment.getDetails().getOriginCoordinates();
            Coordinates destination = shipment.getDetails().getDestinationCoordinates();
            distance = strategy.calculateDistance(
                origin.getLatitude(), origin.getLongitude(),
                destination.getLatitude(), destination.getLongitude()
            );
        } else {
            // Sistema Grid tradicional
            strategy = gridStrategy;
            EuclideanDistanceCalculator calculator = new EuclideanDistanceCalculator();
            distance = calculator.calculateDistance(shipment.getOrigin(), shipment.getDestination());
        }
        
        // Calcular costo usando la estrategia
        return strategy.calculateCost(distance);
    }
    
    /**
     * Verifica si un envío puede ser entregado el mismo día
     * 
     * @param shipment envío a verificar
     * @return true si es posible entrega mismo día
     */
    public static boolean isSameDayDeliveryPossible(Shipment shipment) {
        if (shipment.getOrigin() == null || shipment.getDestination() == null) {
            return false;
        }
        
        // Determinar estrategia
        ICoordinateStrategy strategy;
        double distance;
        
        if (shipment.getDetails() != null && shipment.getDetails().usesRealCoordinates()) {
            strategy = realStrategy;
            Coordinates origin = shipment.getDetails().getOriginCoordinates();
            Coordinates destination = shipment.getDetails().getDestinationCoordinates();
            distance = strategy.calculateDistance(
                origin.getLatitude(), origin.getLongitude(),
                destination.getLatitude(), destination.getLongitude()
            );
        } else {
            strategy = gridStrategy;
            EuclideanDistanceCalculator calculator = new EuclideanDistanceCalculator();
            distance = calculator.calculateDistance(shipment.getOrigin(), shipment.getDestination());
        }
        
        return strategy.isSameDayDeliveryPossible(distance);
    }
    
    /**
     * Obtiene el nombre del sistema de coordenadas que está usando un envío
     * 
     * @param shipment envío a consultar
     * @return nombre del sistema ("Grid Map" o "Real GPS")
     */
    public static String getCoordinateSystemName(Shipment shipment) {
        if (shipment.getDetails() != null && shipment.getDetails().usesRealCoordinates()) {
            return realStrategy.getSystemName();
        } else {
            return gridStrategy.getSystemName();
        }
    }
}