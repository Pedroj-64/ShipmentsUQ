package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation.DeliverySimulation;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.simulation.DeliverySimulator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Controlador REST para el API de tracking en tiempo real.
 * Proporciona datos de envíos para el mapa web de tracking.
 */
public class TrackingApiController implements HttpHandler {
    
    private final ShipmentService shipmentService;
    private final DeliverySimulator deliverySimulator;
    private final Gson gson;
    
    public TrackingApiController() {
        this.shipmentService = ShipmentService.getInstance();
        this.deliverySimulator = DeliverySimulator.getInstance();
        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configurar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        String response = "";
        int statusCode = 200;
        
        try {
            if (path.matches("/api/tracking/active")) {
                // Obtener todos los envíos en tránsito con sus ubicaciones
                response = getActiveShipments();
            } else if (path.matches("/api/tracking/shipment/[^/]+")) {
                // Obtener un envío específico por ID
                String shipmentId = path.substring(path.lastIndexOf('/') + 1);
                response = getShipmentById(shipmentId);
            } else {
                statusCode = 404;
                response = "{\"error\": \"Endpoint no encontrado\"}";
            }
        } catch (Exception e) {
            System.err.println("[TrackingAPI] Error: " + e.getMessage());
            e.printStackTrace();
            statusCode = 500;
            response = "{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}";
        }
        
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
    
    /**
     * Obtiene todos los envíos activos (en tránsito) con sus ubicaciones
     */
    private String getActiveShipments() {
        List<Map<String, Object>> activeShipments = new ArrayList<>();
        
        // Buscar todos los envíos en tránsito
        List<Shipment> shipments = shipmentService.findAll();
        
        for (Shipment shipment : shipments) {
            if (shipment.getDeliverer() != null && shipment.getDestination() != null) {
                Map<String, Object> trackingData = buildTrackingData(shipment);
                if (trackingData != null) {
                    activeShipments.add(trackingData);
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", activeShipments.size());
        result.put("shipments", activeShipments);
        result.put("timestamp", System.currentTimeMillis());
        
        return gson.toJson(result);
    }
    
    /**
     * Obtiene un envío específico por ID
     */
    private String getShipmentById(String shipmentId) {
        try {
            UUID id = UUID.fromString(shipmentId);
            Optional<Shipment> shipmentOpt = shipmentService.findById(id);
            
            if (shipmentOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Envío no encontrado");
                return gson.toJson(error);
            }
            
            Shipment shipment = shipmentOpt.get();
            Map<String, Object> trackingData = buildTrackingData(shipment);
            
            if (trackingData == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "No hay datos de tracking disponibles");
                return gson.toJson(error);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("shipment", trackingData);
            result.put("timestamp", System.currentTimeMillis());
            
            return gson.toJson(result);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "ID de envío inválido");
            return gson.toJson(error);
        }
    }
    
    /**
     * Construye el objeto de datos de tracking para un envío
     */
    private Map<String, Object> buildTrackingData(Shipment shipment) {
        Deliverer deliverer = shipment.getDeliverer();
        Address origin = shipment.getOrigin();
        Address destination = shipment.getDestination();
        
        if (deliverer == null || destination == null) {
            return null;
        }
        
        // Log de depuración
        System.out.println("[TrackingAPI] Construyendo datos para envío: " + shipment.getId());
        System.out.println("[TrackingAPI]   Repartidor: " + deliverer.getName() + 
                         " (Grid: " + deliverer.getCurrentX() + "," + deliverer.getCurrentY() + 
                         " | GPS: " + (deliverer.hasRealCoordinates() ? deliverer.getRealLatitude() + "," + deliverer.getRealLongitude() : "N/A") + ")");
        if (destination != null) {
            System.out.println("[TrackingAPI]   Destino: " + destination.getStreet() + ", " + destination.getCity() +
                             " (Grid: " + destination.getCoordX() + "," + destination.getCoordY() + 
                             " | GPS: " + (destination.hasGpsCoordinates() ? destination.getGpsLatitude() + "," + destination.getGpsLongitude() : "N/A") + ")");
        }
        
        Map<String, Object> data = new HashMap<>();
        
        // Información básica del envío
        data.put("shipmentId", shipment.getId().toString());
        data.put("status", shipment.getStatus().name());
        data.put("trackingCode", shipment.getId().toString()); // El ID es el código de seguimiento
        
        // Información del repartidor
        Map<String, Object> delivererData = new HashMap<>();
        delivererData.put("id", deliverer.getId().toString());
        delivererData.put("name", deliverer.getName());
        delivererData.put("phone", deliverer.getPhone());
        // vehicleType no existe en el modelo Deliverer
        
        // Sincronizar coordenadas GPS del repartidor si no las tiene
        if (!deliverer.hasRealCoordinates()) {
            System.out.println("[TrackingAPI] Sincronizando coordenadas del repartidor " + deliverer.getName() + "...");
            deliverer.syncCoordinates();
        }
        
        // Ubicación actual del repartidor
        Map<String, Object> delivererLocation = new HashMap<>();
        delivererLocation.put("type", "gps");
        delivererLocation.put("lat", deliverer.getRealLatitude());
        delivererLocation.put("lng", deliverer.getRealLongitude());
        
        System.out.println("[TrackingAPI] → Enviando al frontend: Repartidor en GPS (" + 
                         deliverer.getRealLatitude() + "," + deliverer.getRealLongitude() + ")");
        
        delivererData.put("currentLocation", delivererLocation);
        data.put("deliverer", delivererData);
        
        // Origen (si existe)
        if (origin != null) {
            // Sincronizar coordenadas GPS del origen si no las tiene
            if (!origin.hasGpsCoordinates()) {
                System.out.println("[TrackingAPI] Sincronizando coordenadas del origen...");
                origin.syncCoordinates();
            }
            
            Map<String, Object> originData = new HashMap<>();
            originData.put("street", origin.getStreet());
            originData.put("city", origin.getCity());
            originData.put("alias", origin.getAlias());
            
            Map<String, Object> originLocation = new HashMap<>();
            originLocation.put("type", "gps");
            originLocation.put("lat", origin.getGpsLatitude());
            originLocation.put("lng", origin.getGpsLongitude());
            
            System.out.println("[TrackingAPI] → Enviando al frontend: Origen en GPS (" + 
                             origin.getGpsLatitude() + "," + origin.getGpsLongitude() + ")");
            
            originData.put("location", originLocation);
            data.put("origin", originData);
        }
        
        // Destino
        // Sincronizar coordenadas GPS del destino si no las tiene
        if (!destination.hasGpsCoordinates()) {
            System.out.println("[TrackingAPI] Sincronizando coordenadas del destino...");
            destination.syncCoordinates();
        }
        
        Map<String, Object> destinationData = new HashMap<>();
        destinationData.put("street", destination.getStreet());
        destinationData.put("city", destination.getCity());
        destinationData.put("alias", destination.getAlias());
        
        Map<String, Object> destLocation = new HashMap<>();
        destLocation.put("type", "gps");
        destLocation.put("lat", destination.getGpsLatitude());
        destLocation.put("lng", destination.getGpsLongitude());
        
        System.out.println("[TrackingAPI] → Enviando al frontend: Destino en GPS (" + 
                         destination.getGpsLatitude() + "," + destination.getGpsLongitude() + ")");
        destinationData.put("location", destLocation);
        data.put("destination", destinationData);
        
        // Datos de simulación (si hay simulación activa)
        Optional<DeliverySimulation> simulationOpt = deliverySimulator.getSimulation(shipment.getId());
        if (simulationOpt.isPresent()) {
            DeliverySimulation simulation = simulationOpt.get();
            Map<String, Object> simData = new HashMap<>();
            simData.put("active", true);
            simData.put("progress", simulation.getProgressPercentage());
            simData.put("distanceTraveled", simulation.getDistanceTraveled());
            simData.put("remainingDistance", simulation.getRemainingDistance());
            simData.put("eta", simulation.getEstimatedArrival().toString());
            data.put("simulation", simData);
        } else {
            Map<String, Object> simData = new HashMap<>();
            simData.put("active", false);
            data.put("simulation", simData);
        }
        
        return data;
    }
}
