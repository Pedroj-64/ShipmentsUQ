package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para la gestión de pagos
 */
public class PaymentService implements Service<Payment, PaymentRepository> {
    private final PaymentRepository repository;
    
    // Constructor privado para Singleton
    private PaymentService() {
        this.repository = new PaymentRepository();
    }
    
    // Holder estático para instancia única
    private static class SingletonHolder {
        private static final PaymentService INSTANCE = new PaymentService();
    }
    
    /**
     * Obtiene la instancia única del servicio
     * @return instancia del servicio
     */
    public static PaymentService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    @Override
    public PaymentRepository getRepository() {
        return repository;
    }
    
    /**
     * Procesa un nuevo pago
     * @param shipment envío asociado
     * @param paymentMethod método de pago
     * @return pago procesado
     */
    public Payment processPayment(Shipment shipment, PaymentMethod paymentMethod) {
        Payment payment = Payment.builder()
            .shipment(shipment)
            .user(shipment.getUser())
            .amount(shipment.getCost())
            .paymentMethod(paymentMethod)
            .status(PaymentStatus.PENDING)
            .creationDate(LocalDateTime.now())
            .build();
        
        create(payment);
        
        if (payment.processPayment()) {
            update(payment);
            return payment;
        } else {
            throw new RuntimeException("Error processing payment");
        }
    }
    
    /**
     * Busca pagos por usuario
     * @param user usuario que realizó los pagos
     * @return lista de pagos del usuario
     */
    public List<Payment> findByUser(User user) {
        return repository.findByUser(user);
    }
    
    /**
     * Busca pagos por método de pago
     * @param paymentMethod método de pago
     * @return lista de pagos con el método especificado
     */
    public List<Payment> findByPaymentMethod(PaymentMethod paymentMethod) {
        return repository.findByPaymentMethod(paymentMethod);
    }
    
    /**
     * Busca pagos por envío
     * @param shipment envío asociado
     * @return lista de pagos del envío
     */
    public List<Payment> findByShipment(Shipment shipment) {
        return repository.findByShipment(shipment);
    }
    
    /**
     * Busca un pago por referencia
     * @param reference referencia del pago
     * @return pago encontrado o empty si no existe
     */
    public Optional<Payment> findByPaymentReference(String reference) {
        return repository.findByPaymentReference(reference);
    }
    
    /**
     * Calcula los ingresos en un periodo
     * @param startDate fecha inicial
     * @param endDate fecha final
     * @return total de ingresos
     */
    public double calculateIncomeForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.calculateIncomeForPeriod(startDate, endDate);
    }
    
    /**
     * Obtiene estadísticas de uso de métodos de pago
     * @return mapa con estadísticas
     */
    public Map<PaymentMethod, Long> getPaymentMethodStatistics() {
        return repository.getPaymentMethodStatistics();
    }
    
    /**
     * Valida si un pago puede ser reembolsado
     * @param payment pago a validar
     * @return true si el pago puede ser reembolsado
     */
    public boolean canBeRefunded(Payment payment) {
        return payment != null && payment.isRefundable() && payment.getStatus() == PaymentStatus.COMPLETED;
    }
    
    /**
     * Procesa el reembolso de un pago
     * @param payment pago a reembolsar
     * @param reason motivo del reembolso
     * @return true si el reembolso fue exitoso
     */
    public boolean processRefund(Payment payment, String reason) {
        if (!canBeRefunded(payment)) {
            return false;
        }
        
        boolean refunded = payment.processRefund(reason);
        if (refunded) {
            update(payment);
        }
        return refunded;
    }
}