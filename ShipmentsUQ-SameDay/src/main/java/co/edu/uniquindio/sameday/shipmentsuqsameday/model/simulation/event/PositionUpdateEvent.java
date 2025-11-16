package co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation.event;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento que se dispara cuando el repartidor actualiza su posición
 */
@Getter
public class PositionUpdateEvent extends SimulationEvent {
    
    /**
     * Nueva posición del repartidor
     */
    private final Coordinates newPosition;
    
    /**
     * Posición anterior
     */
    private final Coordinates previousPosition;
    
    /**
     * Distancia recorrida desde el origen (metros)
     */
    private final double distanceFromOrigin;
    
    /**
     * Distancia restante hasta el destino (metros)
     */
    private final double distanceToDestination;
    
    /**
     * Porcentaje de progreso (0-100)
     */
    private final double progressPercentage;
    
    /**
     * Tiempo estimado de llegada
     */
    private final LocalDateTime estimatedArrival;
    
    /**
     * Tiempo restante estimado
     */
    private final Duration remainingTime;
    
    public PositionUpdateEvent(UUID simulationId, 
                              Shipment shipment,
                              Coordinates newPosition,
                              Coordinates previousPosition,
                              double distanceFromOrigin,
                              double distanceToDestination,
                              double progressPercentage,
                              LocalDateTime estimatedArrival,
                              Duration remainingTime) {
        super(simulationId, shipment);
        this.newPosition = newPosition;
        this.previousPosition = previousPosition;
        this.distanceFromOrigin = distanceFromOrigin;
        this.distanceToDestination = distanceToDestination;
        this.progressPercentage = progressPercentage;
        this.estimatedArrival = estimatedArrival;
        this.remainingTime = remainingTime;
    }
    
    @Override
    public String getEventType() {
        return "POSITION_UPDATE";
    }
    
    @Override
    public String toString() {
        return String.format(
            "PositionUpdateEvent[pos=%s, progress=%.1f%%, distRemaining=%.0fm, ETA=%s]",
            newPosition,
            progressPercentage,
            distanceToDestination,
            estimatedArrival
        );
    }
}
