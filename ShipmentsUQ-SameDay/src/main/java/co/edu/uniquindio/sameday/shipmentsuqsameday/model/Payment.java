package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase que representa un pago en el sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment implements Serializable {
    
    /** Constante para la serialización */
    private static final long serialVersionUID = 1L;
    private UUID id;
    private Shipment shipment;
    private User user;
    private double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime creationDate;
    private LocalDateTime processingDate;
    private String paymentReference;
    private String transactionDetails;
    
    @Builder.Default
    private boolean refundable = true;
}