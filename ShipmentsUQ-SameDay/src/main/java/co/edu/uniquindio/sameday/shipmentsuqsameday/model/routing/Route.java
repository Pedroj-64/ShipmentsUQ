package co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa una ruta completa calculada entre dos puntos
 * Contiene todos los waypoints, estadísticas y metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Identificador único de la ruta
     */
    private UUID id;
    
    /**
     * Coordenadas del punto de origen
     */
    private Coordinates origin;
    
    /**
     * Coordenadas del punto de destino
     */
    private Coordinates destination;
    
    /**
     * Lista ordenada de waypoints que conforman la ruta
     * El primer elemento es el origen, el último es el destino
     */
    @Builder.Default
    private List<Waypoint> waypoints = new ArrayList<>();
    
    /**
     * Estadísticas calculadas de la ruta
     */
    private RouteStatistics statistics;
    
    /**
     * Fecha y hora en que se calculó la ruta
     */
    private LocalDateTime calculatedAt;
    
    /**
     * Nombre de la estrategia/algoritmo usado para calcular
     * Ej: "OSRM", "Dijkstra", "GraphHopper"
     */
    private String calculationStrategy;
    
    /**
     * Geometría completa de la ruta en formato GeoJSON (opcional)
     * Útil para dibujar la línea completa en el mapa
     */
    private String geoJsonGeometry;
    
    /**
     * Indica si esta ruta es válida y navegable
     */
    private boolean valid;
    
    /**
     * Mensaje de error si la ruta no es válida
     */
    private String errorMessage;
    
    /**
     * Obtiene el primer waypoint (origen)
     */
    public Waypoint getOriginWaypoint() {
        return waypoints.isEmpty() ? null : waypoints.get(0);
    }
    
    /**
     * Obtiene el último waypoint (destino)
     */
    public Waypoint getDestinationWaypoint() {
        return waypoints.isEmpty() ? null : waypoints.get(waypoints.size() - 1);
    }
    
    /**
     * Obtiene el número total de waypoints
     */
    public int getWaypointCount() {
        return waypoints.size();
    }
    
    /**
     * Busca el waypoint más cercano a una distancia acumulada dada
     * @param distanceFromStart distancia desde el origen en metros
     * @return waypoint más cercano a esa distancia
     */
    public Waypoint findWaypointAtDistance(double distanceFromStart) {
        if (waypoints.isEmpty()) return null;
        
        Waypoint closest = waypoints.get(0);
        double minDiff = Math.abs(closest.getDistanceFromStart() - distanceFromStart);
        
        for (Waypoint wp : waypoints) {
            double diff = Math.abs(wp.getDistanceFromStart() - distanceFromStart);
            if (diff < minDiff) {
                minDiff = diff;
                closest = wp;
            }
        }
        
        return closest;
    }
    
    /**
     * Busca el índice del waypoint en una secuencia específica
     * @param sequence número de secuencia
     * @return índice en la lista o -1 si no existe
     */
    public int findWaypointIndexBySequence(int sequence) {
        for (int i = 0; i < waypoints.size(); i++) {
            if (waypoints.get(i).getSequence() == sequence) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Verifica si la ruta tiene geometría GeoJSON
     */
    public boolean hasGeoJsonGeometry() {
        return geoJsonGeometry != null && !geoJsonGeometry.isEmpty();
    }
    
    /**
     * Calcula el progreso en porcentaje dado una distancia recorrida
     * @param currentDistance distancia actual desde el origen
     * @return porcentaje de progreso (0-100)
     */
    public double calculateProgress(double currentDistance) {
        if (statistics == null || statistics.getTotalDistanceMeters() == 0) {
            return 0.0;
        }
        
        double progress = (currentDistance / statistics.getTotalDistanceMeters()) * 100.0;
        return Math.min(100.0, Math.max(0.0, progress));
    }
    
    /**
     * Genera un resumen textual de la ruta
     */
    public String getSummary() {
        if (!valid) {
            return "Ruta inválida: " + errorMessage;
        }
        
        return String.format(
            "Ruta: %d waypoints | %s | Calculado con: %s",
            getWaypointCount(),
            statistics != null ? statistics.getSummary() : "Sin estadísticas",
            calculationStrategy
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "Route[id=%s, waypoints=%d, valid=%b, strategy=%s]",
            id,
            getWaypointCount(),
            valid,
            calculationStrategy
        );
    }
}
