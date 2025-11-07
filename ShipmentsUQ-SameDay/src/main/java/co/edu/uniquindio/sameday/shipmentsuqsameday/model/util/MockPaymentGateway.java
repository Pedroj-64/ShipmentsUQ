package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.PaymentGateway;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;

/**
 * Implementación simulada de gateway de pagos para desarrollo y pruebas
 */
public class MockPaymentGateway implements PaymentGateway {
    private static final double SUCCESS_RATE = 0.9; // 90% de éxito
    
    @Override
    public boolean processPayment(Payment payment) {
        // Simulación simple del proceso de pago
        return Math.random() < SUCCESS_RATE;
    }
    
    @Override
    public boolean processRefund(Payment payment, String reason) {
    
        return payment.getStatus() == PaymentStatus.COMPLETED;
    }
}