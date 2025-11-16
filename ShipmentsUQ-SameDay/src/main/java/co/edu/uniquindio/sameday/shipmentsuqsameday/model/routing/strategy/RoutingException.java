package co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.strategy;

/**
 * Excepción lanzada cuando ocurre un error al calcular una ruta
 */
public class RoutingException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Nombre de la estrategia que falló
     */
    private final String strategyName;
    
    public RoutingException(String strategyName, String message) {
        super(message);
        this.strategyName = strategyName;
    }
    
    public RoutingException(String strategyName, String message, Throwable cause) {
        super(message, cause);
        this.strategyName = strategyName;
    }
    
    public String getStrategyName() {
        return strategyName;
    }
    
    @Override
    public String toString() {
        return String.format("RoutingException[strategy=%s, message=%s]", 
            strategyName, getMessage());
    }
}
