package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.Map;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;

/**
 * Servicio para análisis y estadísticas de pagos
 */
public class PaymentAnalyticsService {
    private final PaymentRepository repository;

    public PaymentAnalyticsService(PaymentRepository repository) {
        this.repository = repository;
    }

    /**
     * Calcula los ingresos en un periodo
     */
    public double calculateIncomeForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.calculateIncomeForPeriod(startDate, endDate);
    }

    /**
     * Obtiene estadísticas de uso de métodos de pago
     */
    public Map<PaymentMethod, Long> getPaymentMethodStatistics() {
        return repository.getPaymentMethodStatistics();
    }
}