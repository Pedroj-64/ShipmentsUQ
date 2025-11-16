package co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Contiene estadísticas calculadas de una ruta
 * Incluye distancia total, tiempo estimado, velocidad promedio, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Distancia total de la ruta en metros
     */
    private double totalDistanceMeters;
    
    /**
     * Duración estimada del viaje
     */
    private Duration estimatedDuration;
    
    /**
     * Velocidad promedio en km/h
     */
    private double averageSpeedKmh;
    
    /**
     * Número total de waypoints en la ruta
     */
    private int waypointCount;
    
    /**
     * Tiempo estimado de llegada al destino
     * Se calcula cuando inicia la simulación
     */
    private LocalDateTime estimatedArrival;
    
    /**
     * Indica si la ruta fue calculada con éxito
     */
    private boolean valid;
    
    /**
     * Nombre del algoritmo o servicio usado para calcular
     * Ej: "OSRM", "Dijkstra", "GraphHopper"
     */
    private String calculationMethod;
    
    /**
     * Obtiene la distancia en kilómetros
     */
    public double getTotalDistanceKm() {
        return totalDistanceMeters / 1000.0;
    }
    
    /**
     * Obtiene la duración en minutos
     */
    public long getEstimatedMinutes() {
        return estimatedDuration != null ? estimatedDuration.toMinutes() : 0;
    }
    
    /**
     * Obtiene la duración en horas (decimal)
     */
    public double getEstimatedHours() {
        return estimatedDuration != null ? estimatedDuration.toMinutes() / 60.0 : 0;
    }
    
    /**
     * Calcula el ETA basado en una hora de inicio
     * @param startTime hora de inicio del viaje
     * @return hora estimada de llegada
     */
    public LocalDateTime calculateETA(LocalDateTime startTime) {
        if (startTime == null || estimatedDuration == null) {
            return null;
        }
        this.estimatedArrival = startTime.plus(estimatedDuration);
        return this.estimatedArrival;
    }
    
    /**
     * Genera un resumen legible de las estadísticas
     */
    public String getSummary() {
        return String.format(
            "Distancia: %.2f km | Tiempo: %d min | Velocidad: %.1f km/h | Waypoints: %d",
            getTotalDistanceKm(),
            getEstimatedMinutes(),
            averageSpeedKmh,
            waypointCount
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "RouteStatistics[dist=%.2fkm, time=%dmin, speed=%.1fkm/h, waypoints=%d, method=%s]",
            getTotalDistanceKm(),
            getEstimatedMinutes(),
            averageSpeedKmh,
            waypointCount,
            calculationMethod
        );
    }
}
