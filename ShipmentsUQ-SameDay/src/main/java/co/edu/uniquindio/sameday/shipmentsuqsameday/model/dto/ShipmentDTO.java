package co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentPriority;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para transferir información pública del envío
 */
@Data
@Builder
public class ShipmentDTO {
    private UUID id;
    private UUID userId;
    private UUID delivererId;
    private AddressDTO origin;
    private AddressDTO destination;
    private ShipmentStatus status;
    private ShipmentPriority priority;
    private double weight;
    private double volume;
    private double cost;
    private LocalDateTime creationDate;
    private LocalDateTime assignmentDate;
    private LocalDateTime deliveryDate;
    private String trackingNumber;
    // No incluye: información personal del remitente/destinatario, detalles de pago
}