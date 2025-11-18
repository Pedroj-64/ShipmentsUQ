package co.edu.uniquindio.sameday.shipmentsuqsameday.mapping;

import java.time.Duration;

/**
 * Calculadora para métricas basadas en coordenadas reales del mapa
 * Calcula distancias, tiempos estimados y costos
 */
public class MapCalculator {
    
    // Velocidad promedio en ciudad (km/h)
    private static final double AVERAGE_SPEED_KMH = 30.0;
    
    // Costo base por kilómetro
    private static final double COST_PER_KM = 2500.0; // COP
    
    // Costo base del servicio
    private static final double BASE_COST = 5000.0; // COP
    
    /**
     * Calcula el costo estimado del envío basado en la distancia
     * @param origin Coordenadas de origen
     * @param destination Coordenadas de destino
     * @return Costo estimado en pesos colombianos
     */
    public static double calculateCost(Coordinates origin, Coordinates destination) {
        double distance = origin.distanceTo(destination);
        return BASE_COST + (distance * COST_PER_KM);
    }
    
    /**
     * Calcula el tiempo estimado de entrega
     * @param origin Coordenadas de origen
     * @param destination Coordenadas de destino
     * @return Tiempo estimado en minutos
     */
    public static long calculateEstimatedTimeMinutes(Coordinates origin, Coordinates destination) {
        double distance = origin.distanceTo(destination);
        double hours = distance / AVERAGE_SPEED_KMH;
        // Agregar 10 minutos de buffer por procesamiento y carga/descarga
        return Math.round(hours * 60) + 10;
    }
    
    /**
     * Calcula el tiempo estimado de entrega como Duration
     * @param origin Coordenadas de origen
     * @param destination Coordenadas de destino
     * @return Duration con el tiempo estimado
     */
    public static Duration calculateEstimatedTime(Coordinates origin, Coordinates destination) {
        return Duration.ofMinutes(calculateEstimatedTimeMinutes(origin, destination));
    }
    
    /**
     * Formatea el tiempo estimado como string legible
     * @param origin Coordenadas de origen
     * @param destination Coordenadas de destino
     * @return String formateado (ej: "45 minutos", "1 hora 30 minutos")
     */
    public static String formatEstimatedTime(Coordinates origin, Coordinates destination) {
        long totalMinutes = calculateEstimatedTimeMinutes(origin, destination);
        
        if (totalMinutes < 60) {
            return totalMinutes + " minutos";
        } else {
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            
            if (minutes == 0) {
                return hours + (hours == 1 ? " hora" : " horas");
            } else {
                return hours + (hours == 1 ? " hora " : " horas ") + minutes + " minutos";
            }
        }
    }
    
    /**
     * Encuentra el repartidor más cercano a una ubicación
     * @param delivererCoordinates Lista de coordenadas de repartidores disponibles
     * @param targetLocation Ubicación objetivo
     * @return Índice del repartidor más cercano, -1 si la lista está vacía
     */
    public static int findNearestDeliverer(java.util.List<Coordinates> delivererCoordinates, Coordinates targetLocation) {
        if (delivererCoordinates == null || delivererCoordinates.isEmpty()) {
            return -1;
        }
        
        int nearestIndex = 0;
        double minDistance = delivererCoordinates.get(0).distanceTo(targetLocation);
        
        for (int i = 1; i < delivererCoordinates.size(); i++) {
            double distance = delivererCoordinates.get(i).distanceTo(targetLocation);
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }
        
        return nearestIndex;
    }
    
    /**
     * Verifica si la distancia es razonable para entrega el mismo día
     * @param origin Coordenadas de origen
     * @param destination Coordenadas de destino
     * @return true si la distancia permite entrega el mismo día
     */
    public static boolean isSameDayDeliveryPossible(Coordinates origin, Coordinates destination) {
        double distance = origin.distanceTo(destination);
        // Máximo 30 km para entrega el mismo día
        return distance <= 30.0;
    }
    
    /**
     * Calcula la distancia y retorna información detallada
     * @param origin Coordenadas de origen
     * @param destination Coordenadas de destino
     * @return String con información detallada del recorrido
     */
    @SuppressWarnings("unused")
    public static String getRouteInfo(Coordinates origin, Coordinates destination) {
        double distance = origin.distanceTo(destination);
        long time = calculateEstimatedTimeMinutes(origin, destination);
        double cost = calculateCost(origin, destination);
        
        return String.format(
            "Distancia: %.2f km\n" +
            "Tiempo estimado: %s\n" +
            "Costo estimado: $%,.0f COP\n" +
            "Entrega mismo día: %s",
            distance,
            formatEstimatedTime(origin, destination),
            cost,
            isSameDayDeliveryPossible(origin, destination) ? "Sí" : "No"
        );
    }
}
