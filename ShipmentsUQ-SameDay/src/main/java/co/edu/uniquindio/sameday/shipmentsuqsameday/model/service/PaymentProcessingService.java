package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.PaymentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.PaymentGateway;

/**
 * Servicio para el procesamiento de pagos
 */
public class PaymentProcessingService {
    private final PaymentRepository repository;
    private final PaymentGateway paymentGateway;

    public PaymentProcessingService(PaymentRepository repository, PaymentGateway paymentGateway) {
        this.repository = repository;
        this.paymentGateway = paymentGateway;
    }

    /**
     * Procesa un pago a través del gateway
     */
    public boolean processPayment(Payment payment) {
        boolean success = paymentGateway.processPayment(payment);
        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
        } else {
            payment.setStatus(PaymentStatus.REJECTED);
        }
        repository.update(payment);
        return success;
    }

    /**
     * Procesa un reembolso
     */
    public boolean processRefund(Payment payment, String reason) {
        if (!payment.isRefundable() || payment.getStatus() != PaymentStatus.COMPLETED) {
            return false;
        }

        boolean success = paymentGateway.processRefund(payment, reason);
        if (success) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setTransactionDetails("Reembolso: " + reason);
            repository.update(payment);
        }
        return success;
    }
}