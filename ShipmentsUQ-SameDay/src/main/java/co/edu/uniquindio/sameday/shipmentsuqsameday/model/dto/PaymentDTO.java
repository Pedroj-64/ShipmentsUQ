package co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para transferir información básica del pago
 */
@Data
@Builder
public class PaymentDTO {
    private UUID id;
    private UUID shipmentId;
    private UUID userId;
    private double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime creationDate;
    private LocalDateTime processingDate;
    private String paymentReference;
    // No incluye: detalles de transacción, información de tarjetas
}