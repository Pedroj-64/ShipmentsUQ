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
    
    // Contador estático para rastrear cuántas ventanas están usando el servidor
    private static int activeWindows = 0;
    private static MapWebServer sharedServer = null;
    private static MapWebServer.CoordinatesCallback sharedCallback = null;
    
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
        // El callback se establecerá cuando se inicie el servidor
        // No reiniciamos el servidor aquí para evitar interrumpir otras ventanas
    }
    
    /**
     * Inicia el servidor web del mapa si no está activo
     * @return true si se inició correctamente o ya estaba activo
     */
    public boolean startMapServer() {
        synchronized (RealMapService.class) {
            // Incrementar contador de ventanas activas
            activeWindows++;
            System.out.println("[INFO] Ventana de mapa abierta. Ventanas activas: " + activeWindows);
            
            // Si el servidor compartido ya existe y está activo, usarlo
            if (sharedServer != null && sharedServer.isRunning()) {
                System.out.println("[INFO] Reutilizando servidor compartido existente");
                this.webServer = sharedServer;
                return true;
            }
            
            // Si no hay servidor o no está activo, intentar limpiar puerto ocupado
            if (sharedServer == null || !sharedServer.isRunning()) {
                System.out.println("[INFO] Verificando disponibilidad del puerto 8080...");
                boolean portCleared = clearPort8080IfNeeded();
                if (portCleared) {
                    System.out.println("[INFO] Puerto 8080 liberado exitosamente");
                }
            }
            
            // Crear nuevo servidor compartido
            try {
                sharedServer = new MapWebServer(coordinatesCallback);
                sharedServer.start();
                this.webServer = sharedServer;
                
                // Verificar si realmente se inició o está usando uno existente
                if (sharedServer.isRunning()) {
                    System.out.println("[SUCCESS] Servidor de mapas compartido iniciado correctamente");
                } else {
                    System.out.println("[WARN] No se pudo iniciar servidor - intentando abrir navegador de todas formas");
                }
                return true;
            } catch (Exception e) {
                System.err.println("[ERROR] Error inesperado al iniciar servidor: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }
    
    /**
     * Intenta liberar el puerto 8080 si está ocupado por un proceso zombie
     * @return true si se liberó o ya estaba libre
     */
    private boolean clearPort8080IfNeeded() {
        try {
            // Intentar en Windows
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                return clearPort8080Windows();
            }
            return false;
        } catch (Exception e) {
            System.err.println("[WARN] No se pudo verificar/liberar puerto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Libera el puerto 8080 en Windows
     */
    private boolean clearPort8080Windows() {
        try {
            // Encontrar proceso usando puerto 8080
            Process findProcess = Runtime.getRuntime().exec("netstat -ano | findstr :8080");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(findProcess.getInputStream())
            );
            
            String line;
            java.util.Set<String> pidsToKill = new java.util.HashSet<>();
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("LISTENING") || line.contains("ESTABLISHED")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 0) {
                        String pid = parts[parts.length - 1];
                        if (pid.matches("\\d+")) {
                            pidsToKill.add(pid);
                        }
                    }
                }
            }
            reader.close();
            findProcess.waitFor();
            
            // Matar procesos encontrados
            if (!pidsToKill.isEmpty()) {
                System.out.println("[INFO] Procesos encontrados en puerto 8080: " + pidsToKill);
                for (String pid : pidsToKill) {
                    try {
                        System.out.println("[INFO] Terminando proceso zombie con PID: " + pid);
                        Process killProcess = Runtime.getRuntime().exec("taskkill /F /PID " + pid);
                        killProcess.waitFor();
                        Thread.sleep(500); // Esperar a que el puerto se libere
                    } catch (Exception e) {
                        System.err.println("[WARN] No se pudo terminar proceso " + pid + ": " + e.getMessage());
                    }
                }
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("[WARN] Error al intentar liberar puerto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Detiene el servidor web del mapa solo si no hay otras ventanas usándolo
     */
    public void stopMapServer() {
        synchronized (RealMapService.class) {
            // Decrementar contador de ventanas activas
            if (activeWindows > 0) {
                activeWindows--;
                System.out.println("[INFO] Ventana de mapa cerrada. Ventanas activas: " + activeWindows);
            }
            
            // Solo detener el servidor si no hay más ventanas activas
            if (activeWindows == 0 && sharedServer != null) {
                System.out.println("[INFO] No hay más ventanas activas. Deteniendo servidor compartido...");
                sharedServer.stop();
                sharedServer = null;
                webServer = null;
            } else if (activeWindows > 0) {
                System.out.println("[INFO] Manteniendo servidor activo para " + activeWindows + " ventana(s)");
            }
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
