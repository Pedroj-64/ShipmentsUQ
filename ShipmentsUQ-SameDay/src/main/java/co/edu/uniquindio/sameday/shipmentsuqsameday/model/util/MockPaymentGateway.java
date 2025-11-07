package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.PaymentGateway;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;

/**
 * Implementación simulada de gateway de pagos para desarrollo y pruebas
 */
public class MockPaymentGateway implements PaymentGateway {
    private static final double SUCCESS_RATE = 0.9; // 90% de éxito, no todo en la vida es perfecto
    
    @Override
    public boolean processPayment(Payment payment) {
        // Simulación simple del proceso de pago a veces no hay plata en nequi
        // se aceptan donaciones de nequi :D
        return Math.random() < SUCCESS_RATE;
    }
    
    @Override
    public boolean processRefund(Payment payment, String reason) {
    
        return payment.getStatus() == PaymentStatus.COMPLETED;
    }
}