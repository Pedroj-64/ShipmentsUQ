package co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuración para las simulaciones de entrega
 * Permite ajustar la velocidad y comportamiento de la simulación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationConfig {
    
    /**
     * Intervalo de actualización en milisegundos
     * Por defecto: 500ms (0.5 segundos) para movimiento fluido
     */
    @Builder.Default
    private long updateIntervalMillis = 500;
    
    /**
     * Multiplicador de velocidad de simulación
     * 1.0 = tiempo real
     * 60.0 = 1 hora en 1 minuto
     * 120.0 = 1 hora en 30 segundos
     * Por defecto: 60x (útil para testing)
     */
    @Builder.Default
    private double speedMultiplier = 60.0;
    
    /**
     * Tolerancia de distancia para considerar que se alcanzó un waypoint (metros)
     * Por defecto: 10 metros
     */
    @Builder.Default
    private double waypointToleranceMeters = 10.0;
    
    /**
     * Indica si la simulación debe auto-iniciar al crear
     * Por defecto: true
     */
    @Builder.Default
    private boolean autoStart = true;
    
    /**
     * Indica si se deben emitir eventos de waypoint alcanzado
     * Por defecto: true
     */
    @Builder.Default
    private boolean emitWaypointEvents = true;
    
    /**
     * Indica si se debe actualizar automáticamente el estado del envío a DELIVERED
     * Por defecto: true
     */
    @Builder.Default
    private boolean autoCompleteDelivery = true;
    
    /**
     * Indica si la simulación debe pausarse al alcanzar ciertos waypoints
     * Útil para debugging
     * Por defecto: false
     */
    @Builder.Default
    private boolean pauseAtWaypoints = false;
    
    /**
     * Obtiene el intervalo de actualización en segundos
     */
    public double getUpdateIntervalSeconds() {
        return updateIntervalMillis / 1000.0;
    }
    
    /**
     * Configuración por defecto para producción
     * Velocidad 60x, actualización cada 2 segundos
     */
    public static SimulationConfig production() {
        return SimulationConfig.builder()
                .updateIntervalMillis(2000)
                .speedMultiplier(60.0)
                .autoStart(true)
                .autoCompleteDelivery(true)
                .build();
    }
    
    /**
     * Configuración para testing/debugging
     * Velocidad 120x (muy rápido), actualización cada segundo
     */
    public static SimulationConfig testing() {
        return SimulationConfig.builder()
                .updateIntervalMillis(1000)
                .speedMultiplier(120.0)
                .autoStart(true)
                .autoCompleteDelivery(true)
                .pauseAtWaypoints(false)
                .build();
    }
    
    /**
     * Configuración de debugging
     * Velocidad 10x (más lenta), pausa en waypoints
     */
    public static SimulationConfig debugging() {
        return SimulationConfig.builder()
                .updateIntervalMillis(1000)
                .speedMultiplier(10.0)
                .autoStart(false)
                .autoCompleteDelivery(false)
                .pauseAtWaypoints(true)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format(
            "SimulationConfig[speed=%.0fx, interval=%dms, autoStart=%b]",
            speedMultiplier,
            updateIntervalMillis,
            autoStart
        );
    }
}
