package co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Representa un punto intermedio en una ruta de entrega
 * Contiene coordenadas GPS y metadata de tiempo/distancia
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Waypoint implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Orden/secuencia en la ruta (0 = origen, último = destino)
     */
    private int sequence;
    
    /**
     * Coordenadas GPS del waypoint
     */
    private Coordinates coordinates;
    
    /**
     * Distancia acumulada desde el origen en metros
     */
    private double distanceFromStart;
    
    /**
     * Tiempo acumulado desde el origen
     */
    private Duration timeFromStart;
    
    /**
     * Tiempo estimado de llegada a este punto
     * Se calcula basado en la hora de inicio del viaje
     */
    private LocalDateTime estimatedArrival;
    
    /**
     * Nombre descriptivo del punto (opcional)
     * Ej: "Calle 15 con Carrera 8"
     */
    private String description;
    
    /**
     * Indica si este waypoint ha sido alcanzado durante la simulación
     */
    private boolean reached;
    
    /**
     * Hora real en que se alcanzó este waypoint (null si no se ha alcanzado)
     */
    private LocalDateTime actualArrival;
    
    /**
     * Verifica si este es el primer waypoint (origen)
     */
    public boolean isOrigin() {
        return sequence == 0;
    }
    
    /**
     * Calcula el ETA basado en una hora de inicio
     * @param startTime hora de inicio del viaje
     * @return hora estimada de llegada
     */
    public LocalDateTime calculateETA(LocalDateTime startTime) {
        if (startTime == null || timeFromStart == null) {
            return null;
        }
        this.estimatedArrival = startTime.plus(timeFromStart);
        return this.estimatedArrival;
    }
    
    /**
     * Marca el waypoint como alcanzado
     */
    public void markAsReached() {
        this.reached = true;
        this.actualArrival = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("Waypoint[seq=%d, coords=%s, dist=%.0fm, time=%s]",
                sequence,
                coordinates != null ? coordinates.toString() : "null",
                distanceFromStart,
                timeFromStart != null ? formatDuration(timeFromStart) : "null");
    }
    
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
