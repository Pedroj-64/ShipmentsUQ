package co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation.event;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase base para todos los eventos de simulación
 * Implementa patrón Observer para notificar cambios en la simulación
 */
@Data
@AllArgsConstructor
public abstract class SimulationEvent {
    
    /**
     * ID único del evento
     */
    private UUID eventId;
    
    /**
     * ID de la simulación que generó el evento
     */
    private UUID simulationId;
    
    /**
     * Envío asociado al evento
     */
    private Shipment shipment;
    
    /**
     * Timestamp del evento
     */
    private LocalDateTime timestamp;
    
    /**
     * Constructor que genera ID automáticamente
     */
    public SimulationEvent(UUID simulationId, Shipment shipment) {
        this.eventId = UUID.randomUUID();
        this.simulationId = simulationId;
        this.shipment = shipment;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Obtiene el tipo de evento
     */
    public abstract String getEventType();
    
    @Override
    public String toString() {
        return String.format("%s[id=%s, shipment=%s, time=%s]",
                getEventType(),
                eventId,
                shipment != null ? shipment.getId() : "null",
                timestamp);
    }
}
