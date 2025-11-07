package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;

/**
 * Interfaz para la integración con sistemas de pago
 */
public interface PaymentGateway {
    /**
     * Procesa un pago a través del gateway
     * @param payment información del pago a procesar
     * @return true si el pago fue exitoso
     */
    boolean processPayment(Payment payment);
    
    /**
     * Procesa un reembolso a través del gateway
     * @param payment pago a reembolsar
     * @param reason motivo del reembolso
     * @return true si el reembolso fue exitoso
     */
    boolean processRefund(Payment payment, String reason);
}