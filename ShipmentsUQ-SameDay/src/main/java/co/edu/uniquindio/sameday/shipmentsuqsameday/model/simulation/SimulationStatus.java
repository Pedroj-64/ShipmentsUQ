package co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation;

/**
 * Estados posibles de una simulación de entrega
 */
public enum SimulationStatus {
    /**
     * Simulación inicializando
     */
    INITIALIZING,
    
    /**
     * Simulación en ejecución activa
     */
    RUNNING,
    
    /**
     * Simulación pausada temporalmente
     */
    PAUSED,
    
    /**
     * Simulación completada exitosamente
     */
    COMPLETED,
    
    /**
     * Simulación cancelada manualmente
     */
    CANCELLED,
    
    /**
     * Simulación terminada por error
     */
    FAILED
}
