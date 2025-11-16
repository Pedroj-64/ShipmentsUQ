package co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.strategy;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Route;

/**
 * Strategy Pattern: Interfaz para diferentes algoritmos de cálculo de rutas
 * Permite intercambiar entre OSRM, Dijkstra, GraphHopper, etc.
 */
public interface RoutingStrategy {
    
    /**
     * Calcula la ruta óptima entre dos puntos
     * 
     * @param origin coordenadas del punto de origen
     * @param destination coordenadas del punto de destino
     * @return ruta calculada con waypoints y estadísticas
     * @throws RoutingException si no se puede calcular la ruta
     */
    Route calculateRoute(Coordinates origin, Coordinates destination) throws RoutingException;
    
    /**
     * Obtiene el nombre descriptivo de esta estrategia
     * 
     * @return nombre de la estrategia (ej: "OSRM", "Dijkstra")
     */
    String getStrategyName();
    
    /**
     * Verifica si esta estrategia está disponible para usar
     * Útil para verificar conexión a APIs externas
     * 
     * @return true si está disponible, false en caso contrario
     */
    boolean isAvailable();
    
    /**
     * Obtiene la prioridad de esta estrategia
     * Mayor prioridad = se intenta primero
     * 
     * @return valor de prioridad (1-10, donde 10 es máxima prioridad)
     */
    default int getPriority() {
        return 5;
    }
    
    /**
     * Indica si esta estrategia requiere conexión a internet
     * 
     * @return true si requiere internet (APIs externas)
     */
    default boolean requiresInternet() {
        return false;
    }
}
