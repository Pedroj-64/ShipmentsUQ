package co.edu.uniquindio.sameday.shipmentsuqsameday.mapping;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy.GridCoordinateStrategy;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.strategy.RealCoordinateStrategy;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para integrar el sistema de coordenadas reales con el sistema existente
 * 
 * Patrón de Diseño: ADAPTER PATTERN
 * Propósito: Adaptar el sistema de coordenadas GPS real al sistema de Grid existente,
 * permitiendo que ambos coexistan sin conflictos
 */
public class RealMapService {
    
    private final RealCoordinateStrategy realStrategy;
    private final GridCoordinateStrategy gridStrategy;
    private MapWebServer webServer;
    private MapWebServer.CoordinatesCallback coordinatesCallback;
    
    public RealMapService() {
        this.realStrategy = new RealCoordinateStrategy();
        this.gridStrategy = new GridCoordinateStrategy();
    }
    
    /**
     * Establece el callback para cuando se reciban coordenadas desde el mapa web
     * @param callback función que se ejecutará al recibir coordenadas
     */
    public void setCoordinatesCallback(MapWebServer.CoordinatesCallback callback) {
        this.coordinatesCallback = callback;
        
        // Si el servidor ya está activo, actualizar su callback
        if (webServer != null) {
            // Reiniciar servidor con nuevo callback
            stopMapServer();
            try {
                startMapServer();
            } catch (Exception e) {
                System.err.println("Error al reiniciar servidor con nuevo callback: " + e.getMessage());
            }
        }
    }
    
    /**
     * Inicia el servidor web del mapa si no está activo
     * @return true si se inició correctamente
     */
    public boolean startMapServer() {
        if (webServer == null) {
            try {
                webServer = new MapWebServer(coordinatesCallback);
                webServer.start();
                return true;
            } catch (Exception e) {
                System.err.println("Error al iniciar servidor de mapas: " + e.getMessage());
                return false;
            }
        }
        return true;
    }
    
    /**
     * Detiene el servidor web del mapa
     */
    public void stopMapServer() {
        if (webServer != null) {
            webServer.stop();
            webServer = null;
        }
    }
    
    /**
     * Abre el mapa en el navegador (versión predeterminada para direcciones)
     */
    public void openMapInBrowser() {
        openMapInBrowser(MapType.ADDRESS);
    }
    
    /**
     * Abre un tipo específico de mapa en el navegador
     * @param mapType tipo de mapa a abrir
     */
    public void openMapInBrowser(MapType mapType) {
        if (webServer != null) {
            webServer.openInBrowser(mapType);
        }
    }
    
    /**
     * Enum para especificar el tipo de mapa a abrir
     */
    public enum MapType {
        ADDRESS("address-map.html", "Selección de Dirección"),
        DELIVERER("deliverer-map.html", "Ubicación de Repartidor"),
        TRACKING("tracking-map.html", "Seguimiento en Tiempo Real"),
        DEFAULT("index.html", "Mapa General"); // El mapa original con origen/destino
        
        private final String filename;
        private final String description;
        
        MapType(String filename, String description) {
            this.filename = filename;
            this.description = description;
        }
        
        public String getFilename() {
            return filename;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Convierte coordenadas reales a coordenadas de Grid
     * Útil para mantener compatibilidad con el sistema existente
     * 
     * @param latitude latitud real
     * @param longitude longitud real
     * @return array [gridX, gridY]
     */
    public double[] convertRealToGrid(double latitude, double longitude) {
        return GridCoordinateStrategy.convertRealToGrid(latitude, longitude);
    }
    
    /**
     * Convierte coordenadas de Grid a coordenadas reales
     * 
     * @param gridX coordenada X del grid
     * @param gridY coordenada Y del grid
     * @return array [latitude, longitude]
     */
    public double[] convertGridToReal(double gridX, double gridY) {
        return GridCoordinateStrategy.convertGridToReal(gridX, gridY);
    }
    
    /**
     * Calcula la distancia real entre dos coordenadas GPS
     * 
     * @param origin coordenada de origen
     * @param destination coordenada de destino
     * @return distancia en kilómetros
     */
    public double calculateRealDistance(Coordinates origin, Coordinates destination) {
        return realStrategy.calculateDistance(origin, destination);
    }
    
    /**
     * Encuentra el repartidor más cercano basándose en coordenadas reales
     * 
     * @param deliverers lista de repartidores
     * @param targetLocation ubicación objetivo
     * @return repartidor más cercano que tenga coordenadas reales, o vacío si ninguno las tiene
     */
    public Optional<Deliverer> findNearestDelivererByRealCoords(
            List<Deliverer> deliverers, 
            Coordinates targetLocation) {
        
        return deliverers.stream()
            .filter(Deliverer::hasRealCoordinates)
            .min((d1, d2) -> {
                Coordinates coord1 = new Coordinates(d1.getRealLatitude(), d1.getRealLongitude());
                Coordinates coord2 = new Coordinates(d2.getRealLatitude(), d2.getRealLongitude());
                
                double dist1 = realStrategy.calculateDistance(coord1, targetLocation);
                double dist2 = realStrategy.calculateDistance(coord2, targetLocation);
                
                return Double.compare(dist1, dist2);
            });
    }
    
    /**
     * Sincroniza las coordenadas de un repartidor:
     * Si tiene coordenadas reales, actualiza también las de Grid
     * 
     * @param deliverer repartidor a sincronizar
     */
    public void syncDelivererCoordinates(Deliverer deliverer) {
        if (deliverer.hasRealCoordinates()) {
            double[] gridCoords = convertRealToGrid(
                deliverer.getRealLatitude(), 
                deliverer.getRealLongitude()
            );
            deliverer.updatePosition(gridCoords[0], gridCoords[1]);
        }
    }
    
    /**
     * Verifica si una coordenada real está dentro del área de servicio
     * 
     * @param coordinates coordenada a verificar
     * @return true si está dentro del área de servicio
     */
    public boolean isInServiceArea(Coordinates coordinates) {
        return realStrategy.isInServiceArea(coordinates);
    }
    
    /**
     * Calcula el costo de envío basándose en coordenadas reales
     * 
     * @param origin coordenada de origen
     * @param destination coordenada de destino
     * @return costo en COP
     */
    public double calculateShipmentCost(Coordinates origin, Coordinates destination) {
        double distance = realStrategy.calculateDistance(origin, destination);
        return realStrategy.calculateCost(distance);
    }
    
    /**
     * Calcula el tiempo estimado basándose en coordenadas reales
     * 
     * @param origin coordenada de origen
     * @param destination coordenada de destino
     * @return tiempo en minutos
     */
    public double calculateEstimatedTime(Coordinates origin, Coordinates destination) {
        double distance = realStrategy.calculateDistance(origin, destination);
        return realStrategy.calculateEstimatedTime(distance);
    }
    
    /**
     * Verifica si es posible entrega el mismo día
     * 
     * @param origin coordenada de origen
     * @param destination coordenada de destino
     * @return true si es posible same-day delivery
     */
    public boolean isSameDayDeliveryPossible(Coordinates origin, Coordinates destination) {
        double distance = realStrategy.calculateDistance(origin, destination);
        return realStrategy.isSameDayDeliveryPossible(distance);
    }
}
