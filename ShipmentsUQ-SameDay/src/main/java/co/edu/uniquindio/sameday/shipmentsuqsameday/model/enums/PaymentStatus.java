package co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;

/**
 * Estados posibles de un pago en el sistema
 */
public enum PaymentStatus {
    PENDING,         // Pago creado pero no procesado
    PROCESSING,      // Pago en proceso de validación
    COMPLETED,       // Pago exitoso
    REJECTED,        // Pago rechazado por el método de pago
    REFUNDED        // Pago reembolsado al usuario
}