package co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation.event;

/**
 * Interfaz Observer para escuchar eventos de simulación
 * Los controladores de UI implementan esta interfaz para recibir actualizaciones
 */
public interface SimulationListener {
    
    /**
     * Se llama cuando la posición del repartidor se actualiza
     * @param event evento con la nueva posición y estadísticas
     */
    void onPositionUpdate(PositionUpdateEvent event);
    
    /**
     * Se llama cuando el repartidor alcanza un waypoint
     * @param event evento con información del waypoint alcanzado
     */
    void onWaypointReached(WaypointReachedEvent event);
    
    /**
     * Se llama cuando se completa la entrega
     * @param event evento con información de la entrega completada
     */
    void onDeliveryCompleted(DeliveryCompletedEvent event);
    
    /**
     * Se llama cuando ocurre un error en la simulación
     * @param simulationId ID de la simulación que falló
     * @param error excepción que causó el error
     */
    default void onSimulationError(java.util.UUID simulationId, Exception error) {
        System.err.println("Error en simulación " + simulationId + ": " + error.getMessage());
    }
}
