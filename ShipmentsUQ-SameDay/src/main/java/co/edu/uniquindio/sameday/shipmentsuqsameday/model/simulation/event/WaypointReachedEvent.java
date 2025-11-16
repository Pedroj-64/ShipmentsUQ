package co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation.event;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Waypoint;
import lombok.Getter;

import java.util.UUID;

/**
 * Evento que se dispara cuando el repartidor alcanza un waypoint
 */
@Getter
public class WaypointReachedEvent extends SimulationEvent {
    
    /**
     * Waypoint que fue alcanzado
     */
    private final Waypoint waypoint;
    
    /**
     * Número de waypoints restantes hasta el destino
     */
    private final int remainingWaypoints;
    
    public WaypointReachedEvent(UUID simulationId, 
                                Shipment shipment,
                                Waypoint waypoint,
                                int remainingWaypoints) {
        super(simulationId, shipment);
        this.waypoint = waypoint;
        this.remainingWaypoints = remainingWaypoints;
    }
    
    @Override
    public String getEventType() {
        return "WAYPOINT_REACHED";
    }
    
    @Override
    public String toString() {
        return String.format(
            "WaypointReachedEvent[waypoint=%d, remaining=%d, coords=%s]",
            waypoint.getSequence(),
            remainingWaypoints,
            waypoint.getCoordinates()
        );
    }
}
