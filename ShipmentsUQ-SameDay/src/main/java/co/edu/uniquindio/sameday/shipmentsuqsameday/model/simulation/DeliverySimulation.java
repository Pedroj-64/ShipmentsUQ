package co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Route;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Waypoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa una simulación activa de entrega
 * Contiene toda la información de estado de la simulación en progreso
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverySimulation {
    
    /**
     * ID único de la simulación
     */
    private UUID id;
    
    /**
     * Envío siendo simulado
     */
    private Shipment shipment;
    
    /**
     * Repartidor que realiza la entrega
     */
    private Deliverer deliverer;
    
    /**
     * Ruta calculada para la entrega
     */
    private Route route;
    
    /**
     * Configuración de la simulación
     */
    private SimulationConfig config;
    
    /**
     * Estado actual de la simulación
     */
    private SimulationStatus status;
    
    /**
     * Índice del waypoint actual en la ruta
     */
    private int currentWaypointIndex;
    
    /**
     * Posición GPS actual del repartidor
     */
    private Coordinates currentPosition;
    
    /**
     * Distancia acumulada recorrida desde el origen (metros)
     */
    private double distanceTraveled;
    
    /**
     * Hora de inicio real de la simulación
     */
    private LocalDateTime simulationStartTime;
    
    /**
     * Hora de inicio simulada del viaje (puede ser diferente por el speedMultiplier)
     */
    private LocalDateTime virtualStartTime;
    
    /**
     * Última actualización de posición
     */
    private LocalDateTime lastUpdateTime;
    
    /**
     * Hora estimada de llegada
     */
    private LocalDateTime estimatedArrival;
    
    /**
     * Porcentaje de progreso (0-100)
     */
    private double progressPercentage;
    
    /**
     * Mensaje de error si la simulación falló
     */
    private String errorMessage;
    
    /**
     * Obtiene el waypoint actual
     */
    public Waypoint getCurrentWaypoint() {
        if (route == null || route.getWaypoints().isEmpty() || 
            currentWaypointIndex >= route.getWaypoints().size()) {
            return null;
        }
        return route.getWaypoints().get(currentWaypointIndex);
    }
    
    /**
     * Obtiene el siguiente waypoint (destino inmediato)
     */
    public Waypoint getNextWaypoint() {
        if (route == null || currentWaypointIndex + 1 >= route.getWaypoints().size()) {
            return null;
        }
        return route.getWaypoints().get(currentWaypointIndex + 1);
    }
    
    /**
     * Obtiene el waypoint de destino final
     */
    public Waypoint getDestinationWaypoint() {
        if (route == null || route.getWaypoints().isEmpty()) {
            return null;
        }
        return route.getWaypoints().get(route.getWaypoints().size() - 1);
    }
    
    /**
     * Verifica si la simulación está activa (running o paused)
     */
    public boolean isActive() {
        return status == SimulationStatus.RUNNING || status == SimulationStatus.PAUSED;
    }
    
    /**
     * Verifica si la simulación ha terminado
     */
    public boolean isFinished() {
        return status == SimulationStatus.COMPLETED || 
               status == SimulationStatus.CANCELLED || 
               status == SimulationStatus.FAILED;
    }
    
    /**
     * Calcula el tiempo transcurrido en la simulación (acelerado)
     */
    public Duration getVirtualElapsedTime() {
        if (simulationStartTime == null) {
            return Duration.ZERO;
        }
        
        Duration realElapsed = Duration.between(simulationStartTime, LocalDateTime.now());
        
        if (config != null && config.getSpeedMultiplier() > 0) {
            long virtualSeconds = (long) (realElapsed.getSeconds() * config.getSpeedMultiplier());
            return Duration.ofSeconds(virtualSeconds);
        }
        
        return realElapsed;
    }
    
    /**
     * Calcula el tiempo restante estimado
     */
    public Duration getRemainingTime() {
        if (route == null || route.getStatistics() == null) {
            return Duration.ZERO;
        }
        
        Duration totalDuration = route.getStatistics().getEstimatedDuration();
        Duration elapsed = getVirtualElapsedTime();
        
        Duration remaining = totalDuration.minus(elapsed);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
    
    /**
     * Calcula la distancia restante hasta el destino
     */
    public double getRemainingDistance() {
        if (route == null || route.getStatistics() == null) {
            return 0.0;
        }
        
        double totalDistance = route.getStatistics().getTotalDistanceMeters();
        return totalDistance - distanceTraveled;
    }
    
    /**
     * Avanza al siguiente waypoint
     */
    public void advanceToNextWaypoint() {
        if (currentWaypointIndex < route.getWaypoints().size() - 1) {
            currentWaypointIndex++;
        }
    }
    
    /**
     * Actualiza el progreso de la simulación
     */
    public void updateProgress() {
        if (route == null || route.getStatistics() == null) {
            this.progressPercentage = 0.0;
            return;
        }
        
        double totalDistance = route.getStatistics().getTotalDistanceMeters();
        if (totalDistance > 0) {
            this.progressPercentage = (distanceTraveled / totalDistance) * 100.0;
            this.progressPercentage = Math.min(100.0, Math.max(0.0, this.progressPercentage));
        }
    }
    
    /**
     * Genera un resumen del estado actual
     */
    public String getStatusSummary() {
        return String.format(
            "Simulación[status=%s, progress=%.1f%%, waypoint=%d/%d, speed=%.0fx]",
            status,
            progressPercentage,
            currentWaypointIndex + 1,
            route != null ? route.getWaypointCount() : 0,
            config != null ? config.getSpeedMultiplier() : 0
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "DeliverySimulation[id=%s, shipment=%s, status=%s, progress=%.1f%%]",
            id,
            shipment != null ? shipment.getId() : "null",
            status,
            progressPercentage
        );
    }
}
