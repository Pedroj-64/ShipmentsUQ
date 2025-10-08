package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase que representa un repartidor en el sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deliverer implements Serializable {
    
    /** Constante para la serialización */
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String name;
    private String document;
    private String phone;
    private DelivererStatus status;
    private String zone;
    private double averageRating;
    private int totalDeliveries;
    
    @Builder.Default
    private List<Shipment> currentShipments = new ArrayList<>();
    
    @Builder.Default
    private List<Shipment> shipmentHistory = new ArrayList<>();
}