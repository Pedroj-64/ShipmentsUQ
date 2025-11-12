package co.edu.uniquindio.sameday.shipmentsuqsameday.mapping;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.ShipmentDetails;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.ICoordinateStrategy;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy.GridCoordinateStrategy;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy.RealCoordinateStrategy;

import java.util.List;
import java.util.Optional;

/**
 * Servicio Facade que unifica el acceso a los sistemas de coordenadas
 * Grid y Real GPS, proporcionando una interfaz simplificada
 * 
 * Patrón de Diseño: FACADE PATTERN
 * Propósito: Proporcionar una interfaz unificada para un conjunto de interfaces
 * en un subsistema (GridMap y RealMap)
 * 
 * Este es el punto de entrada principal para todo lo relacionado con coordenadas
 */
public class MapCoordinateIntegrationService {
    
    private final GridCoordinateStrategy gridStrategy;
    private final RealCoordinateStrategy realStrategy;
    private final RealMapService realMapService;
    private final co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.EuclideanDistanceCalculator euclideanCalculator;
    
    /**
     * Constructor con inyección de dependencias (opcional)
     */
    public MapCoordinateIntegrationService() {
        this.gridStrategy = new GridCoordinateStrategy();
        this.realStrategy = new RealCoordinateStrategy();
        this.realMapService = new RealMapService();
        this.euclideanCalculator = new co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.EuclideanDistanceCalculator();
    }
    
    // ========== OPERACIONES CON REPARTIDORES ==========
    
    /**
     * Obtiene la ubicación de un repartidor en el sistema que esté usando
     * 
     * @param deliverer repartidor
     * @return descripción de la ubicación
     */
    public String getDelivererLocation(Deliverer deliverer) {
        if (deliverer.hasRealCoordinates()) {
            return String.format("GPS: (%.6f, %.6f)", 
                deliverer.getRealLatitude(), 
                deliverer.getRealLongitude());
        } else {
            return String.format("Grid: (%.2f, %.2f)", 
                deliverer.getCurrentX(), 
                deliverer.getCurrentY());
        }
    }
    
    /**
     * Encuentra el repartidor más cercano a una ubicación
     * Funciona con ambos sistemas de coordenadas
     * 
     * @param deliverers lista de repartidores disponibles
     * @param targetLocation ubicación objetivo (Coordinates o null si se usa Grid)
     * @param gridX coordenada X del grid (si no se usan coordenadas reales)
     * @param gridY coordenada Y del grid (si no se usan coordenadas reales)
     * @return repartidor más cercano
     */
    public Optional<Deliverer> findNearestDeliverer(
            List<Deliverer> deliverers, 
            Coordinates targetLocation,
            double gridX,
            double gridY) {
        
        if (targetLocation != null) {
            // Usar coordenadas GPS reales
            return realMapService.findNearestDelivererByRealCoords(deliverers, targetLocation);
        } else {
            // Usar sistema Grid tradicional
            return deliverers.stream()
                .min((d1, d2) -> {
                    double dist1 = gridStrategy.calculateDistance(
                        d1.getCurrentX(), d1.getCurrentY(), gridX, gridY
                    );
                    double dist2 = gridStrategy.calculateDistance(
                        d2.getCurrentX(), d2.getCurrentY(), gridX, gridY
                    );
                    return Double.compare(dist1, dist2);
                });
        }
    }
    
    /**
     * Sincroniza las coordenadas de un repartidor
     * Si tiene GPS, actualiza Grid; si tiene Grid, puede estimar GPS
     * 
     * @param deliverer repartidor a sincronizar
     */
    public void syncDelivererCoordinates(Deliverer deliverer) {
        realMapService.syncDelivererCoordinates(deliverer);
    }
    
    // ========== OPERACIONES CON ENVÍOS ==========
    
    /**
     * Calcula el costo de un envío basándose en el sistema de coordenadas apropiado
     * 
     * @param shipment envío a calcular
     * @return costo en COP
     */
    public double calculateShipmentCost(Shipment shipment) {
        if (usesRealCoordinates(shipment)) {
            ShipmentDetails details = shipment.getDetails();
            return realMapService.calculateShipmentCost(
                details.getOriginCoordinates(),
                details.getDestinationCoordinates()
            );
        } else {
            // Usar Grid tradicional con EuclideanDistanceCalculator
            double distance = euclideanCalculator.calculateDistance(
                shipment.getOrigin(), shipment.getDestination());
            return gridStrategy.calculateCost(distance);
        }
    }
    
    /**
     * Calcula el tiempo estimado de un envío
     * 
     * @param shipment envío a calcular
     * @return tiempo en minutos
     */
    public double calculateEstimatedTime(Shipment shipment) {
        if (usesRealCoordinates(shipment)) {
            ShipmentDetails details = shipment.getDetails();
            return realMapService.calculateEstimatedTime(
                details.getOriginCoordinates(),
                details.getDestinationCoordinates()
            );
        } else {
            double distance = euclideanCalculator.calculateDistance(
                shipment.getOrigin(), shipment.getDestination());
            return gridStrategy.calculateEstimatedTime(distance);
        }
    }
    
    /**
     * Verifica si un envío puede ser entregado el mismo día
     * 
     * @param shipment envío a verificar
     * @return true si es posible same-day delivery
     */
    public boolean isSameDayDeliveryPossible(Shipment shipment) {
        if (usesRealCoordinates(shipment)) {
            ShipmentDetails details = shipment.getDetails();
            return realMapService.isSameDayDeliveryPossible(
                details.getOriginCoordinates(),
                details.getDestinationCoordinates()
            );
        } else {
            double distance = euclideanCalculator.calculateDistance(
                shipment.getOrigin(), shipment.getDestination());
            return gridStrategy.isSameDayDeliveryPossible(distance);
        }
    }
    
    /**
     * Crea ShipmentDetails con coordenadas reales
     * 
     * @param origin coordenadas de origen GPS
     * @param destination coordenadas de destino GPS
     * @return ShipmentDetails configurado
     */
    public ShipmentDetails createShipmentDetailsWithRealCoordinates(
            Coordinates origin, 
            Coordinates destination) {
        
        double distance = realStrategy.calculateDistance(origin, destination);
        double baseCost = 5000.0; // Costo base
        double totalCost = realStrategy.calculateCost(distance);
        double estimatedTime = realStrategy.calculateEstimatedTime(distance);
        
        return ShipmentDetails.builder()
            .distance(distance)
            .baseCost(baseCost)
            .totalCost(totalCost)
            .estimatedDuration(estimatedTime)
            .originCoordinates(origin)
            .destinationCoordinates(destination)
            .coordinateSystem("Real GPS")
            .build();
    }
    
    /**
     * Crea ShipmentDetails con coordenadas de Grid (tradicional)
     * 
     * @param originX coordenada X origen
     * @param originY coordenada Y origen
     * @param destX coordenada X destino
     * @param destY coordenada Y destino
     * @return ShipmentDetails configurado
     */
    public ShipmentDetails createShipmentDetailsWithGridCoordinates(
            double originX, double originY,
            double destX, double destY) {
        
        double distance = gridStrategy.calculateDistance(originX, originY, destX, destY);
        double baseCost = 3000.0; // Costo base para Grid
        double totalCost = gridStrategy.calculateCost(distance);
        double estimatedTime = gridStrategy.calculateEstimatedTime(distance);
        
        return ShipmentDetails.builder()
            .distance(distance)
            .baseCost(baseCost)
            .totalCost(totalCost)
            .estimatedDuration(estimatedTime)
            .originCoordinates(null) // Sin coordenadas reales
            .destinationCoordinates(null)
            .coordinateSystem("Grid")
            .build();
    }
    
    // ========== CONVERSIONES Y UTILIDADES ==========
    
    /**
     * Convierte coordenadas GPS a Grid
     * 
     * @param latitude latitud GPS
     * @param longitude longitud GPS
     * @return array [gridX, gridY]
     */
    public double[] convertRealToGrid(double latitude, double longitude) {
        return realMapService.convertRealToGrid(latitude, longitude);
    }
    
    /**
     * Convierte coordenadas Grid a GPS (aproximado)
     * 
     * @param gridX coordenada X
     * @param gridY coordenada Y
     * @return array [latitude, longitude]
     */
    public double[] convertGridToReal(double gridX, double gridY) {
        return realMapService.convertGridToReal(gridX, gridY);
    }
    
    /**
     * Verifica si una ubicación GPS está dentro del área de servicio
     * 
     * @param coordinates coordenadas a verificar
     * @return true si está dentro del área
     */
    public boolean isInServiceArea(Coordinates coordinates) {
        return realMapService.isInServiceArea(coordinates);
    }
    
    /**
     * Obtiene el servicio de mapa real para operaciones avanzadas
     * 
     * @return instancia de RealMapService
     */
    public RealMapService getRealMapService() {
        return realMapService;
    }
    
    /**
     * Obtiene la estrategia apropiada para un envío
     * 
     * @param shipment envío
     * @return estrategia a usar
     */
    public ICoordinateStrategy getStrategyForShipment(Shipment shipment) {
        return usesRealCoordinates(shipment) ? realStrategy : gridStrategy;
    }
    
    /**
     * Obtiene el nombre del sistema de coordenadas que usa un envío
     * 
     * @param shipment envío
     * @return nombre del sistema
     */
    public String getCoordinateSystemName(Shipment shipment) {
        return usesRealCoordinates(shipment) ? "Real GPS" : "Grid Map";
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    /**
     * Verifica si un envío usa coordenadas reales
     */
    private boolean usesRealCoordinates(Shipment shipment) {
        return shipment.getDetails() != null && 
               shipment.getDetails().usesRealCoordinates();
    }
}
