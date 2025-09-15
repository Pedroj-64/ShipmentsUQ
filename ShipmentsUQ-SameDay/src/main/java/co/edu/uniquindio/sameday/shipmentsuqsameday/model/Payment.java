package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase que representa un pago en el sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
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
    
    /**
     * Procesa el pago y actualiza su estado
     * @return true si el pago fue exitoso
     */
    public boolean processPayment() {
        this.processingDate = LocalDateTime.now();
        
        // Aquí iría la lógica de integración con el sistema de pagos
        boolean paymentSuccess = simulatePaymentProcess();
        
        if (paymentSuccess) {
            this.status = PaymentStatus.COMPLETED;
        } else {
            this.status = PaymentStatus.REJECTED;
        }
        
        return paymentSuccess;
    }
    
    /**
     * Simula el proceso de pago (en un sistema real, esto se conectaría con un gateway de pagos)
     * @return true si el pago fue exitoso
     */
    private boolean simulatePaymentProcess() {
        // Simulación simple: 90% de probabilidad de éxito
        return Math.random() < 0.9;
    }
    
    /**
     * Procesa un reembolso
     * @param reason motivo del reembolso
     * @return true si el reembolso fue exitoso
     */
    public boolean processRefund(String reason) {
        if (!refundable || status != PaymentStatus.COMPLETED) {
            return false;
        }
        
        this.status = PaymentStatus.REFUNDED;
        this.transactionDetails = "Refund: " + reason;
        return true;
    }
    
    @Builder.Default
    private String generatePaymentReference = generateInitialPaymentReference();
    
    /**
     * Genera un identificador único para la referencia de pago
     */
    private static String generateInitialPaymentReference() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}