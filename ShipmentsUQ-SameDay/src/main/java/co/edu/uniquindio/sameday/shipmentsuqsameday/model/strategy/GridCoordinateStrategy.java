package co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.ICoordinateStrategy;

/**
 * Implementación de Strategy para sistema de coordenadas de cuadrícula (GridMap)
 * Mantiene la lógica existente del sistema de Grid
 * 
 * Patrón de Diseño: STRATEGY PATTERN (Concrete Strategy)
 */
public class GridCoordinateStrategy implements ICoordinateStrategy {
    
    // Constantes del sistema de Grid (mantenidas del sistema original)
    private static final double COST_PER_CELL = 1000.0; // COP por celda
    private static final double BASE_COST = 3000.0; // COP base
    private static final double TIME_PER_CELL = 2.0; // minutos por celda
    private static final double MAX_SAME_DAY_DISTANCE = 20.0; // celdas
    private static final double SERVICE_AREA_RADIUS = 25.0; // celdas desde el centro
    
    // Centro del área de servicio en el grid (Armenia en coordenadas de grid)
    private static final double CENTER_X = 50.0;
    private static final double CENTER_Y = 50.0;
    
    @Override
    public double calculateDistance(double x1, double y1, double x2, double y2) {
        // Distancia Manhattan (sistema de cuadrícula)
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }
    
    @Override
    public double calculateCost(double distance) {
        return BASE_COST + (distance * COST_PER_CELL);
    }
    
    @Override
    public double calculateEstimatedTime(double distance) {
        return distance * TIME_PER_CELL;
    }
    
    @Override
    public boolean isSameDayDeliveryPossible(double distance) {
        return distance <= MAX_SAME_DAY_DISTANCE;
    }
    
    @Override
    public String getSystemName() {
        return "Grid Map (Sistema de Cuadrícula)";
    }
    
    @Override
    public boolean isInServiceArea(double x, double y) {
        double distance = Math.sqrt(
            Math.pow(x - CENTER_X, 2) + Math.pow(y - CENTER_Y, 2)
        );
        return distance <= SERVICE_AREA_RADIUS;
    }
    
    /**
     * Convierte coordenadas reales (lat/lng) a coordenadas de grid
     * Método auxiliar para migración de datos
     * 
     * @param latitude latitud real
     * @param longitude longitud real
     * @return array [gridX, gridY]
     */
    public static double[] convertRealToGrid(double latitude, double longitude) {
        // Armenia centro: 4.533889, -75.681111
        // Mapeo simple: 1 grado ≈ 20 celdas
        double gridX = CENTER_X + ((longitude + 75.681111) * 20);
        double gridY = CENTER_Y + ((latitude - 4.533889) * 20);
        
        return new double[]{gridX, gridY};
    }
    
    /**
     * Convierte coordenadas de grid a coordenadas reales aproximadas
     * 
     * @param gridX coordenada X del grid
     * @param gridY coordenada Y del grid
     * @return array [latitude, longitude]
     */
    public static double[] convertGridToReal(double gridX, double gridY) {
        double longitude = -75.681111 + ((gridX - CENTER_X) / 20.0);
        double latitude = 4.533889 + ((gridY - CENTER_Y) / 20.0);
        
        return new double[]{latitude, longitude};
    }
}
