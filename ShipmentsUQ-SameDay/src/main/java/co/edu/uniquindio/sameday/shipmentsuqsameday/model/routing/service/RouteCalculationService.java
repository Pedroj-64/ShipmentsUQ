package co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Route;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.strategy.DijkstraRoutingStrategy;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.strategy.OSRMRoutingStrategy;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.strategy.RoutingException;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.strategy.RoutingStrategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio principal para cálculo de rutas
 * Maneja múltiples estrategias y sistema de cache
 */
public class RouteCalculationService {
    
    private static RouteCalculationService instance;
    
    private final List<RoutingStrategy> strategies;
    private final Map<String, Route> cache;
    private final long CACHE_TTL_MILLIS = 3600000; // 1 hora
    
    /**
     * Constructor privado (Singleton)
     */
    private RouteCalculationService() {
        this.strategies = new ArrayList<>();
        this.cache = new ConcurrentHashMap<>();
        
        // Registrar estrategias por orden de prioridad
        registerStrategy(new OSRMRoutingStrategy());
        registerStrategy(new DijkstraRoutingStrategy());
        
        // Ordenar por prioridad descendente
        this.strategies.sort((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()));
        
        System.out.println("[RouteService] Inicializado con " + strategies.size() + " estrategias");
        strategies.forEach(s -> 
            System.out.println("  - " + s.getStrategyName() + " (prioridad: " + s.getPriority() + ")")
        );
    }
    
    /**
     * Obtiene la instancia única del servicio (Singleton)
     */
    public static synchronized RouteCalculationService getInstance() {
        if (instance == null) {
            instance = new RouteCalculationService();
        }
        return instance;
    }
    
    /**
     * Registra una nueva estrategia de ruteo
     */
    public void registerStrategy(RoutingStrategy strategy) {
        if (!strategies.contains(strategy)) {
            strategies.add(strategy);
            strategies.sort((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()));
            System.out.println("[RouteService] Estrategia registrada: " + strategy.getStrategyName());
        }
    }
    
    /**
     * Calcula la ruta óptima entre dos puntos
     * Intenta con cada estrategia en orden de prioridad hasta que una tenga éxito
     * 
     * @param origin coordenadas de origen
     * @param destination coordenadas de destino
     * @return ruta calculada
     * @throws RoutingException si ninguna estrategia puede calcular la ruta
     */
    public Route calculateOptimalRoute(Coordinates origin, Coordinates destination) throws RoutingException {
        // 1. Verificar cache
        String cacheKey = generateCacheKey(origin, destination);
        Route cachedRoute = getFromCache(cacheKey);
        if (cachedRoute != null) {
            System.out.println("[RouteService] Ruta encontrada en cache");
            return cachedRoute;
        }
        
        // 2. Intentar con cada estrategia disponible
        List<String> failedStrategies = new ArrayList<>();
        
        for (RoutingStrategy strategy : strategies) {
            // Verificar disponibilidad
            if (!strategy.isAvailable()) {
                System.out.println("[RouteService] " + strategy.getStrategyName() + " no está disponible, saltando...");
                failedStrategies.add(strategy.getStrategyName() + " (no disponible)");
                continue;
            }
            
            try {
                System.out.println("[RouteService] Intentando calcular con: " + strategy.getStrategyName());
                
                Route route = strategy.calculateRoute(origin, destination);
                
                if (route != null && route.isValid()) {
                    // Guardar en cache
                    putInCache(cacheKey, route);
                    
                    System.out.println("[RouteService] ✓ Ruta calculada exitosamente con: " + strategy.getStrategyName());
                    return route;
                }
                
            } catch (RoutingException e) {
                System.err.println("[RouteService] ✗ " + strategy.getStrategyName() + " falló: " + e.getMessage());
                failedStrategies.add(strategy.getStrategyName() + " (" + e.getMessage() + ")");
            }
        }
        
        // 3. Si ninguna estrategia funcionó, lanzar excepción
        String errorMessage = "No se pudo calcular la ruta con ninguna estrategia disponible.\n" +
                "Estrategias intentadas: " + String.join(", ", failedStrategies);
        
        throw new RoutingException("RouteCalculationService", errorMessage);
    }
    
    /**
     * Calcula una ruta usando una estrategia específica
     */
    public Route calculateRouteWithStrategy(Coordinates origin, Coordinates destination, 
                                           String strategyName) throws RoutingException {
        RoutingStrategy strategy = findStrategyByName(strategyName);
        
        if (strategy == null) {
            throw new RoutingException("RouteCalculationService", 
                "Estrategia no encontrada: " + strategyName);
        }
        
        if (!strategy.isAvailable()) {
            throw new RoutingException(strategyName, 
                "La estrategia no está disponible actualmente");
        }
        
        return strategy.calculateRoute(origin, destination);
    }
    
    /**
     * Busca una estrategia por nombre
     */
    private RoutingStrategy findStrategyByName(String name) {
        return strategies.stream()
                .filter(s -> s.getStrategyName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Genera una clave única para el cache basada en coordenadas
     */
    private String generateCacheKey(Coordinates origin, Coordinates destination) {
        return String.format("route_%.6f_%.6f_to_%.6f_%.6f",
                origin.getLatitude(), origin.getLongitude(),
                destination.getLatitude(), destination.getLongitude());
    }
    
    /**
     * Obtiene una ruta del cache si existe y no ha expirado
     */
    private Route getFromCache(String key) {
        Route route = cache.get(key);
        
        if (route == null) {
            return null;
        }
        
        // Verificar si ha expirado
        long ageMillis = System.currentTimeMillis() - 
            route.getCalculatedAt().atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        
        if (ageMillis > CACHE_TTL_MILLIS) {
            cache.remove(key);
            return null;
        }
        
        return route;
    }
    
    /**
     * Guarda una ruta en el cache
     */
    private void putInCache(String key, Route route) {
        cache.put(key, route);
        System.out.println("[RouteService] Ruta guardada en cache (total: " + cache.size() + ")");
    }
    
    /**
     * Limpia el cache completo
     */
    public void clearCache() {
        int size = cache.size();
        cache.clear();
        System.out.println("[RouteService] Cache limpiado (" + size + " rutas eliminadas)");
    }
    
    /**
     * Obtiene estadísticas del servicio
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("strategiesCount", strategies.size());
        stats.put("cacheSize", cache.size());
        stats.put("availableStrategies", strategies.stream()
                .filter(RoutingStrategy::isAvailable)
                .map(RoutingStrategy::getStrategyName)
                .toList());
        return stats;
    }
    
    /**
     * Verifica el estado de todas las estrategias
     */
    public void checkStrategiesHealth() {
        System.out.println("[RouteService] Verificando estado de estrategias:");
        for (RoutingStrategy strategy : strategies) {
            boolean available = strategy.isAvailable();
            String status = available ? "✓ DISPONIBLE" : "✗ NO DISPONIBLE";
            System.out.println("  " + strategy.getStrategyName() + ": " + status);
        }
    }
}
