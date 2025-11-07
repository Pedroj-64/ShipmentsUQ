package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

/**
 * Interfaz para representar un punto en un sistema de coordenadas cartesianas
 */
public interface IGridCoordinate {
    /**
     * Obtiene la coordenada X
     * @return valor de la coordenada X
     */
    double getX();
    
    /**
     * Obtiene la coordenada Y
     * @return valor de la coordenada Y
     */
    double getY();
    
    /**
     * Calcula la distancia euclidiana a otro punto
     * @param other el otro punto para calcular la distancia
     * @return distancia euclidiana entre los dos puntos
     */
    default double distanceTo(IGridCoordinate other) {
        double deltaX = other.getX() - getX();
        double deltaY = other.getY() - getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}