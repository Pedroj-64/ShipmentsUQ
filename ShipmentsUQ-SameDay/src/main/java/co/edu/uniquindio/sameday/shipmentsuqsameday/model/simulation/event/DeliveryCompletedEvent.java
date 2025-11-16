package co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation.event;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento que se dispara cuando se completa una entrega
 * Este evento resulta en el cambio automático de estado del envío a DELIVERED
 */
@Getter
public class DeliveryCompletedEvent extends SimulationEvent {
    
    /**
     * Coordenadas finales de entrega
     */
    private final Coordinates deliveryLocation;
    
    /**
     * Hora real de entrega
     */
    private final LocalDateTime actualDeliveryTime;
    
    /**
     * Duración real del viaje
     */
    private final Duration actualDuration;
    
    /**
     * Distancia total recorrida
     */
    private final double totalDistanceMeters;
    
    /**
     * Indica si la entrega fue a tiempo (según el ETA)
     */
    private final boolean onTime;
    
    /**
     * Diferencia en minutos respecto al ETA (positivo = tarde, negativo = temprano)
     */
    private final long minutesDifference;
    
    public DeliveryCompletedEvent(UUID simulationId,
                                  Shipment shipment,
                                  Coordinates deliveryLocation,
                                  LocalDateTime actualDeliveryTime,
                                  Duration actualDuration,
                                  double totalDistanceMeters,
                                  boolean onTime,
                                  long minutesDifference) {
        super(simulationId, shipment);
        this.deliveryLocation = deliveryLocation;
        this.actualDeliveryTime = actualDeliveryTime;
        this.actualDuration = actualDuration;
        this.totalDistanceMeters = totalDistanceMeters;
        this.onTime = onTime;
        this.minutesDifference = minutesDifference;
    }
    
    @Override
    public String getEventType() {
        return "DELIVERY_COMPLETED";
    }
    
    public String getPerformanceSummary() {
        String timing = onTime ? "a tiempo" : 
            (minutesDifference > 0 ? minutesDifference + " min tarde" : 
                                     Math.abs(minutesDifference) + " min temprano");
        
        return String.format(
            "Entrega completada %s | Duración: %d min | Distancia: %.2f km",
            timing,
            actualDuration.toMinutes(),
            totalDistanceMeters / 1000.0
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "DeliveryCompletedEvent[location=%s, time=%s, duration=%dmin, onTime=%b]",
            deliveryLocation,
            actualDeliveryTime,
            actualDuration.toMinutes(),
            onTime
        );
    }
}
