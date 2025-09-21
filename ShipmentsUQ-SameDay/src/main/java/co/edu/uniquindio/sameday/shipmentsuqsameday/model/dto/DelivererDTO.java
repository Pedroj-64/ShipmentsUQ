package co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO para transferir información pública del repartidor
 */
@Data
@Builder
public class DelivererDTO {
    private UUID id;
    private String name;
    private DelivererStatus status;
    private double averageRating;
    private int totalDeliveries;
    private String vehicleType;
    // No incluye: documentos personales, licencia, información bancaria
}