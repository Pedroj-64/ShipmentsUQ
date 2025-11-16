package co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.strategy;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Route;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.RouteStatistics;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.routing.Waypoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Estrategia de ruteo usando OSRM (Open Source Routing Machine)
 * API gratuita y de código abierto para calcular rutas reales
 * 
 * Documentación: http://project-osrm.org/docs/v5.24.0/api/
 */
public class OSRMRoutingStrategy implements RoutingStrategy {
    
    private static final String OSRM_PUBLIC_URL = "http://router.project-osrm.org";
    private static final String PROFILE = "driving"; // driving, walking, cycling
    private static final int TIMEOUT_SECONDS = 10;
    
    private final OkHttpClient httpClient;
    private final String baseUrl;
    
    /**
     * Constructor por defecto usa el servidor público de OSRM
     */
    public OSRMRoutingStrategy() {
        this(OSRM_PUBLIC_URL);
    }
    
    /**
     * Constructor que permite especificar servidor OSRM personalizado
     * @param baseUrl URL base del servidor OSRM
     */
    public OSRMRoutingStrategy(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }
    
    @Override
    public Route calculateRoute(Coordinates origin, Coordinates destination) throws RoutingException {
        try {
            // 1. Construir URL de la API
            String url = buildRouteUrl(origin, destination);
            System.out.println("[OSRM] Calculando ruta: " + url);
            
            // 2. Hacer request HTTP
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            Response response = httpClient.newCall(request).execute();
            
            if (!response.isSuccessful()) {
                throw new RoutingException(getStrategyName(), 
                    "Error HTTP: " + response.code());
            }
            
            String jsonResponse = response.body().string();
            
            // 3. Parsear respuesta JSON
            Route route = parseOSRMResponse(jsonResponse, origin, destination);
            
            System.out.println("[OSRM] Ruta calculada exitosamente: " + route.getSummary());
            
            return route;
            
        } catch (IOException e) {
            throw new RoutingException(getStrategyName(), 
                "Error de conexión con OSRM: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RoutingException(getStrategyName(), 
                "Error al procesar respuesta OSRM: " + e.getMessage(), e);
        }
    }
    
    /**
     * Construye la URL para el request de OSRM
     */
    private String buildRouteUrl(Coordinates origin, Coordinates destination) {
        // OSRM usa formato: longitude,latitude (al revés de lo convencional)
        return String.format(
            "%s/route/v1/%s/%f,%f;%f,%f?overview=full&geometries=geojson&steps=true",
            baseUrl,
            PROFILE,
            origin.getLongitude(), origin.getLatitude(),
            destination.getLongitude(), destination.getLatitude()
        );
    }
    
    /**
     * Parsea la respuesta JSON de OSRM y construye un objeto Route
     */
    private Route parseOSRMResponse(String jsonResponse, Coordinates origin, Coordinates destination) 
            throws RoutingException {
        
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            // Verificar código de respuesta
            String code = root.get("code").getAsString();
            if (!"Ok".equals(code)) {
                throw new RoutingException(getStrategyName(), 
                    "OSRM retornó código: " + code);
            }
            
            // Obtener primera ruta (OSRM puede retornar múltiples rutas alternativas)
            JsonArray routes = root.getAsJsonArray("routes");
            if (routes.size() == 0) {
                throw new RoutingException(getStrategyName(), 
                    "OSRM no encontró ninguna ruta");
            }
            
            JsonObject routeData = routes.get(0).getAsJsonObject();
            
            // Extraer estadísticas
            double distanceMeters = routeData.get("distance").getAsDouble();
            double durationSeconds = routeData.get("duration").getAsDouble();
            
            // Extraer geometría (lista de coordenadas)
            JsonObject geometry = routeData.getAsJsonObject("geometry");
            JsonArray coordinates = geometry.getAsJsonArray("coordinates");
            
            // Convertir geometría a waypoints
            List<Waypoint> waypoints = parseGeometryToWaypoints(
                coordinates, distanceMeters, durationSeconds
            );
            
            // Calcular velocidad promedio
            double averageSpeedKmh = (distanceMeters / 1000.0) / (durationSeconds / 3600.0);
            
            // Construir estadísticas
            RouteStatistics statistics = RouteStatistics.builder()
                    .totalDistanceMeters(distanceMeters)
                    .estimatedDuration(Duration.ofSeconds((long) durationSeconds))
                    .averageSpeedKmh(averageSpeedKmh)
                    .waypointCount(waypoints.size())
                    .valid(true)
                    .calculationMethod(getStrategyName())
                    .build();
            
            // Construir ruta completa
            return Route.builder()
                    .id(UUID.randomUUID())
                    .origin(origin)
                    .destination(destination)
                    .waypoints(waypoints)
                    .statistics(statistics)
                    .calculatedAt(LocalDateTime.now())
                    .calculationStrategy(getStrategyName())
                    .geoJsonGeometry(geometry.toString())
                    .valid(true)
                    .build();
            
        } catch (Exception e) {
            throw new RoutingException(getStrategyName(), 
                "Error parseando JSON de OSRM: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convierte la geometría GeoJSON de OSRM a lista de Waypoints
     */
    private List<Waypoint> parseGeometryToWaypoints(JsonArray coordinates, 
                                                     double totalDistance, 
                                                     double totalDuration) {
        List<Waypoint> waypoints = new ArrayList<>();
        int totalPoints = coordinates.size();
        
        for (int i = 0; i < totalPoints; i++) {
            JsonArray point = coordinates.get(i).getAsJsonArray();
            double lon = point.get(0).getAsDouble();
            double lat = point.get(1).getAsDouble();
            
            // Calcular progreso (distancia y tiempo acumulado)
            double progress = (double) i / (totalPoints - 1);
            double distanceFromStart = totalDistance * progress;
            long secondsFromStart = (long) (totalDuration * progress);
            
            Waypoint waypoint = Waypoint.builder()
                    .sequence(i)
                    .coordinates(new Coordinates(lat, lon))
                    .distanceFromStart(distanceFromStart)
                    .timeFromStart(Duration.ofSeconds(secondsFromStart))
                    .reached(false)
                    .build();
            
            waypoints.add(waypoint);
        }
        
        return waypoints;
    }
    
    @Override
    public String getStrategyName() {
        return "OSRM";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Hacer un request simple para verificar conectividad
            String testUrl = baseUrl + "/route/v1/driving/-75.681111,4.533889;-75.670000,4.540000";
            Request request = new Request.Builder()
                    .url(testUrl)
                    .get()
                    .build();
            
            Response response = httpClient.newCall(request).execute();
            boolean available = response.isSuccessful();
            response.close();
            
            return available;
            
        } catch (Exception e) {
            System.err.println("[OSRM] No disponible: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public int getPriority() {
        return 10; // Máxima prioridad - OSRM es preciso y rápido
    }
    
    @Override
    public boolean requiresInternet() {
        return true;
    }
}
