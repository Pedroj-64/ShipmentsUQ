package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

/**
 * Interfaz Strategy para sistemas de coordenadas
 * Permite cambiar dinámicamente entre GridMap y RealMap
 * 
 * Patrón de Diseño: STRATEGY PATTERN
 * Propósito: Definir una familia de algoritmos de coordenadas,
 * encapsular cada uno y hacerlos intercambiables.
 */
public interface ICoordinateStrategy {
    
    /**
     * Calcula la distancia entre dos puntos
     * @param x1 coordenada X/latitud del punto 1
     * @param y1 coordenada Y/longitud del punto 1
     * @param x2 coordenada X/latitud del punto 2
     * @param y2 coordenada Y/longitud del punto 2
     * @return distancia en la unidad correspondiente (celdas para Grid, km para Real)
     */
    double calculateDistance(double x1, double y1, double x2, double y2);
    
    /**
     * Calcula el costo basado en la distancia
     * @param distance distancia calculada
     * @return costo en COP
     */
    double calculateCost(double distance);
    
    /**
     * Calcula el tiempo estimado en minutos
     * @param distance distancia calculada
     * @return tiempo en minutos
     */
    double calculateEstimatedTime(double distance);
    
    /**
     * Verifica si el envío puede ser entregado el mismo día
     * @param distance distancia calculada
     * @return true si es posible entrega mismo día
     */
    boolean isSameDayDeliveryPossible(double distance);
    
    /**
     * Obtiene el nombre del sistema de coordenadas
     * @return nombre descriptivo ("Grid" o "Real GPS")
     */
    String getSystemName();
    
    /**
     * Verifica si las coordenadas están dentro del área de servicio
     * @param x coordenada X/latitud
     * @param y coordenada Y/longitud
     * @return true si está dentro del área de servicio
     */
    boolean isInServiceArea(double x, double y);
}
