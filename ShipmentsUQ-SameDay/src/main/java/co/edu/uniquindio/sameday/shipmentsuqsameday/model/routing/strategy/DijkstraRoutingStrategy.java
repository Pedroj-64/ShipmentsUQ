package co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.strategy;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Route;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.RouteStatistics;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Waypoint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Estrategia de ruteo usando algoritmo de Dijkstra sobre línea recta
 * Sirve como FALLBACK cuando OSRM no está disponible
 * 
 * NOTA: Esta es una implementación simplificada que calcula una ruta en línea recta
 * En una implementación completa, se usaría un grafo real de calles
 */
public class DijkstraRoutingStrategy implements RoutingStrategy {
    
    // Velocidad promedio asumida en km/h (para ciudad)
    private static final double AVERAGE_SPEED_KMH = 30.0;
    
    // Número de waypoints intermedios a generar
    private static final int INTERMEDIATE_WAYPOINTS = 20;
    
    @Override
    public Route calculateRoute(Coordinates origin, Coordinates destination) throws RoutingException {
        try {
            System.out.println("[Dijkstra] Calculando ruta simplificada en línea recta...");
            
            // Calcular distancia en línea recta usando fórmula Haversine
            double distanceMeters = calculateHaversineDistance(origin, destination);
            
            // Calcular duración estimada basada en velocidad promedio
            double durationHours = (distanceMeters / 1000.0) / AVERAGE_SPEED_KMH;
            long durationSeconds = (long) (durationHours * 3600);
            
            // Generar waypoints interpolados en línea recta
            List<Waypoint> waypoints = generateStraightLineWaypoints(
                origin, destination, distanceMeters, durationSeconds
            );
            
            // Construir estadísticas
            RouteStatistics statistics = RouteStatistics.builder()
                    .totalDistanceMeters(distanceMeters)
                    .estimatedDuration(Duration.ofSeconds(durationSeconds))
                    .averageSpeedKmh(AVERAGE_SPEED_KMH)
                    .waypointCount(waypoints.size())
                    .valid(true)
                    .calculationMethod(getStrategyName())
                    .build();
            
            // Construir ruta
            Route route = Route.builder()
                    .id(UUID.randomUUID())
                    .origin(origin)
                    .destination(destination)
                    .waypoints(waypoints)
                    .statistics(statistics)
                    .calculatedAt(LocalDateTime.now())
                    .calculationStrategy(getStrategyName())
                    .valid(true)
                    .build();
            
            System.out.println("[Dijkstra] Ruta calculada: " + route.getSummary());
            
            return route;
            
        } catch (Exception e) {
            throw new RoutingException(getStrategyName(), 
                "Error calculando ruta con Dijkstra: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera waypoints interpolados en línea recta entre origen y destino
     */
    private List<Waypoint> generateStraightLineWaypoints(Coordinates origin, 
                                                          Coordinates destination,
                                                          double totalDistance,
                                                          long totalDuration) {
        List<Waypoint> waypoints = new ArrayList<>();
        
        // Agregar punto de origen
        waypoints.add(Waypoint.builder()
                .sequence(0)
                .coordinates(origin)
                .distanceFromStart(0)
                .timeFromStart(Duration.ZERO)
                .description("Origen")
                .reached(false)
                .build());
        
        // Generar waypoints intermedios
        for (int i = 1; i < INTERMEDIATE_WAYPOINTS; i++) {
            double progress = (double) i / INTERMEDIATE_WAYPOINTS;
            
            // Interpolar coordenadas
            double lat = origin.getLatitude() + 
                (destination.getLatitude() - origin.getLatitude()) * progress;
            double lon = origin.getLongitude() + 
                (destination.getLongitude() - origin.getLongitude()) * progress;
            
            // Calcular distancia y tiempo acumulado
            double distanceFromStart = totalDistance * progress;
            long secondsFromStart = (long) (totalDuration * progress);
            
            waypoints.add(Waypoint.builder()
                    .sequence(i)
                    .coordinates(new Coordinates(lat, lon))
                    .distanceFromStart(distanceFromStart)
                    .timeFromStart(Duration.ofSeconds(secondsFromStart))
                    .reached(false)
                    .build());
        }
        
        // Agregar punto de destino
        waypoints.add(Waypoint.builder()
                .sequence(INTERMEDIATE_WAYPOINTS)
                .coordinates(destination)
                .distanceFromStart(totalDistance)
                .timeFromStart(Duration.ofSeconds(totalDuration))
                .description("Destino")
                .reached(false)
                .build());
        
        return waypoints;
    }
    
    /**
     * Calcula la distancia entre dos coordenadas usando la fórmula de Haversine
     * Retorna la distancia en metros
     */
    private double calculateHaversineDistance(Coordinates c1, Coordinates c2) {
        final double EARTH_RADIUS_KM = 6371.0;
        
        double lat1Rad = Math.toRadians(c1.getLatitude());
        double lat2Rad = Math.toRadians(c2.getLatitude());
        double deltaLat = Math.toRadians(c2.getLatitude() - c1.getLatitude());
        double deltaLon = Math.toRadians(c2.getLongitude() - c1.getLongitude());
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double distanceKm = EARTH_RADIUS_KM * c;
        return distanceKm * 1000.0; // Convertir a metros
    }
    
    @Override
    public String getStrategyName() {
        return "Dijkstra-Fallback";
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Siempre disponible (no requiere conexión)
    }
    
    @Override
    public int getPriority() {
        return 1; // Baja prioridad - solo como fallback
    }
    
    @Override
    public boolean requiresInternet() {
        return false; // No requiere conexión
    }
}
