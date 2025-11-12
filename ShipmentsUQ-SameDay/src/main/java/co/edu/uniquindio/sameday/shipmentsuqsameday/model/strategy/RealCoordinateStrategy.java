package co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.ICoordinateStrategy;

/**
 * Implementación de Strategy para sistema de coordenadas reales (GPS/OpenStreetMap)
 * Usa la fórmula de Haversine para cálculos precisos
 * 
 * Patrón de Diseño: STRATEGY PATTERN (Concrete Strategy)
 */
public class RealCoordinateStrategy implements ICoordinateStrategy {
    
    // Constantes del sistema real (del MapCalculator)
    private static final double COST_PER_KM = 2500.0; // COP por kilómetro
    private static final double BASE_COST = 5000.0; // COP base
    private static final double AVERAGE_SPEED_KMH = 30.0; // km/h promedio
    private static final double BUFFER_TIME_MINUTES = 10.0; // minutos adicionales
    private static final double MAX_SAME_DAY_DISTANCE_KM = 30.0; // kilómetros
    private static final double SERVICE_AREA_RADIUS_KM = 20.0; // kilómetros
    
    // Centro del área de servicio (Armenia, Quindío)
    private static final double ARMENIA_LAT = 4.533889;
    private static final double ARMENIA_LNG = -75.681111;
    
    // Radio de la Tierra en kilómetros
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    @Override
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Fórmula de Haversine para distancia entre dos puntos en una esfera
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    @Override
    public double calculateCost(double distanceKm) {
        return BASE_COST + (distanceKm * COST_PER_KM);
    }
    
    @Override
    public double calculateEstimatedTime(double distanceKm) {
        // Tiempo = (distancia / velocidad) * 60 + buffer
        double travelTime = (distanceKm / AVERAGE_SPEED_KMH) * 60.0;
        return travelTime + BUFFER_TIME_MINUTES;
    }
    
    @Override
    public boolean isSameDayDeliveryPossible(double distanceKm) {
        return distanceKm <= MAX_SAME_DAY_DISTANCE_KM;
    }
    
    @Override
    public String getSystemName() {
        return "Real GPS (OpenStreetMap)";
    }
    
    @Override
    public boolean isInServiceArea(double lat, double lng) {
        double distance = calculateDistance(ARMENIA_LAT, ARMENIA_LNG, lat, lng);
        return distance <= SERVICE_AREA_RADIUS_KM;
    }
    
    /**
     * Calcula la distancia usando objetos Coordinates
     * 
     * @param coord1 coordenada de origen
     * @param coord2 coordenada de destino
     * @return distancia en kilómetros
     */
    public double calculateDistance(Coordinates coord1, Coordinates coord2) {
        return calculateDistance(
            coord1.getLatitude(), coord1.getLongitude(),
            coord2.getLatitude(), coord2.getLongitude()
        );
    }
    
    /**
     * Verifica si una coordenada está en el área de servicio
     * 
     * @param coordinates coordenada a verificar
     * @return true si está dentro del área de servicio
     */
    public boolean isInServiceArea(Coordinates coordinates) {
        return coordinates.isInServiceArea();
    }
}
