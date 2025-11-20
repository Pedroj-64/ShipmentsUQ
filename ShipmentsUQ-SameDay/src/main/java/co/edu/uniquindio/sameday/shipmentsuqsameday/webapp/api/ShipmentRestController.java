package co.edu.uniquindio.sameday.shipmentsuqsameday.webapp.api;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de envíos
 * Expone las operaciones CRUD y consultas de envíos
 */
@RestController
@RequestMapping("/api/shipments")
public class ShipmentRestController {
    
    private final ShipmentService shipmentService;
    
    public ShipmentRestController() {
        this.shipmentService = ShipmentService.getInstance();
    }
    
    /**
     * Obtener todos los envíos de un usuario
     * GET /api/shipments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserShipments(@PathVariable String userId) {
        try {
            UUID userUUID = UUID.fromString(userId);
            List<Shipment> shipments = shipmentService.findByUserId(userUUID);
            
            List<Map<String, Object>> response = shipments.stream()
                    .map(this::convertShipmentToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "ID de usuario inválido"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener envíos: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener un envío por ID
     * GET /api/shipments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getShipmentById(@PathVariable String id) {
        try {
            UUID shipmentUUID = UUID.fromString(id);
            Optional<Shipment> shipmentOpt = shipmentService.getRepository().findById(shipmentUUID);
            
            if (shipmentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Envío no encontrado"));
            }
            
            return ResponseEntity.ok(convertShipmentToMap(shipmentOpt.get()));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "ID de envío inválido"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener envío: " + e.getMessage()));
        }
    }
    
    /**
     * Crear un nuevo envío (cotización)
     * POST /api/shipments/quote
     */
    @PostMapping("/quote")
    public ResponseEntity<?> createQuote(@RequestBody CreateShipmentRequest request) {
        try {
            // Aquí se integraría con QuoteShipmentController
            // Por ahora retornamos un estimado
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("estimatedCost", calculateEstimatedCost(request));
            response.put("estimatedTime", "30-45 minutos");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al calcular cotización: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener envíos del repartidor
     * GET /api/shipments/deliverer/{delivererId}
     */
    @GetMapping("/deliverer/{delivererId}")
    public ResponseEntity<?> getDelivererShipments(@PathVariable String delivererId) {
        try {
            UUID delivererUUID = UUID.fromString(delivererId);
            
            // Filtrar envíos por deliverer ID
            List<Shipment> shipments = shipmentService.getRepository().findAll().stream()
                    .filter(s -> s.getDeliverer() != null && s.getDeliverer().getId().equals(delivererUUID))
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> response = shipments.stream()
                    .map(this::convertShipmentToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "ID de repartidor inválido"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener envíos: " + e.getMessage()));
        }
    }
    
    /**
     * Actualizar estado de un envío
     * PUT /api/shipments/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateShipmentStatus(
            @PathVariable String id,
            @RequestBody UpdateStatusRequest request) {
        try {
            UUID shipmentUUID = UUID.fromString(id);
            Optional<Shipment> shipmentOpt = shipmentService.getRepository().findById(shipmentUUID);
            
            if (shipmentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Envío no encontrado"));
            }
            
            Shipment shipment = shipmentOpt.get();
            ShipmentStatus newStatus = ShipmentStatus.valueOf(request.getStatus());
            shipment.setStatus(newStatus);
            
            shipmentService.getRepository().update(shipment);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Estado actualizado correctamente",
                    "shipment", convertShipmentToMap(shipment)
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Datos inválidos: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar estado: " + e.getMessage()));
        }
    }
    
    // DTOs
    
    public static class CreateShipmentRequest {
        private String originAddress;
        private String destinationAddress;
        private String description;
        private Double weight;
        private String packageType;
        
        // Getters y Setters
        public String getOriginAddress() { return originAddress; }
        public void setOriginAddress(String originAddress) { this.originAddress = originAddress; }
        public String getDestinationAddress() { return destinationAddress; }
        public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        public String getPackageType() { return packageType; }
        public void setPackageType(String packageType) { this.packageType = packageType; }
    }
    
    public static class UpdateStatusRequest {
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    // Métodos helper
    
    private Map<String, Object> convertShipmentToMap(Shipment shipment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", shipment.getId().toString());
        map.put("status", shipment.getStatus().toString());
        map.put("cost", shipment.getCost());
        map.put("weight", shipment.getWeight());
        map.put("volume", shipment.getVolume());
        
        if (shipment.getCreationDate() != null) {
            map.put("creationDate", shipment.getCreationDate().toString());
        }
        
        if (shipment.getSpecialInstructions() != null) {
            map.put("specialInstructions", shipment.getSpecialInstructions());
        }
        
        if (shipment.getOrigin() != null) {
            map.put("originAddress", shipment.getOrigin().getFullAddress());
        }
        
        if (shipment.getDestination() != null) {
            map.put("destinationAddress", shipment.getDestination().getFullAddress());
        }
        
        if (shipment.getDeliverer() != null) {
            Map<String, String> deliverer = new HashMap<>();
            deliverer.put("id", shipment.getDeliverer().getId().toString());
            deliverer.put("name", shipment.getDeliverer().getName());
            map.put("deliverer", deliverer);
        }
        
        if (shipment.getUser() != null) {
            Map<String, String> user = new HashMap<>();
            user.put("id", shipment.getUser().getId().toString());
            user.put("name", shipment.getUser().getName());
            map.put("user", user);
        }
        
        return map;
    }
    
    private double calculateEstimatedCost(CreateShipmentRequest request) {
        // Lógica simple de estimación
        double baseCost = 5000;
        double weightCost = (request.getWeight() != null) ? request.getWeight() * 1000 : 0;
        return baseCost + weightCost;
    }
}
