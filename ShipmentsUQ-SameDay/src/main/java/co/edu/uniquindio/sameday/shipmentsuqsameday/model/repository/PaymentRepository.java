package co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repositorio para la gestión de pagos
 */
public class PaymentRepository extends BaseRepository<Payment> {
    
    @Override
    protected UUID getEntityId(Payment payment) {
        return payment.getId();
    }
    
    @Override
    protected void setEntityId(Payment payment, UUID id) {
        payment.setId(id);
    }
    
    /**
     * Busca pagos por usuario
     * @param user usuario que realizó los pagos
     * @return lista de pagos del usuario
     */
    public List<Payment> findByUser(User user) {
        return findAll().stream()
                .filter(p -> p.getUser().equals(user))
                .collect(Collectors.toList());
    }

    /**
     * Busca pagos por método de pago
     * @param paymentMethod método de pago utilizado
     * @return lista de pagos con el método especificado
     */
    public List<Payment> findByPaymentMethod(PaymentMethod paymentMethod) {
        return findAll().stream()
                .filter(p -> p.getPaymentMethod().equals(paymentMethod))
                .collect(Collectors.toList());
    }

    /**
     * Busca pagos por rango de fechas
     * @param startDate fecha inicial
     * @param endDate fecha final
     * @return lista de pagos en el rango de fechas
     */
    public List<Payment> findByCreationDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return findAll().stream()
                .filter(p -> !p.getCreationDate().isBefore(startDate) && !p.getCreationDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    /**
     * Busca un pago por referencia
     * @param reference referencia del pago
     * @return pago encontrado o empty si no existe
     */
    public Optional<Payment> findByPaymentReference(String reference) {
        return findAll().stream()
                .filter(p -> p.getPaymentReference().equals(reference))
                .findFirst();
    }

    /**
     * Busca pagos por envío
     * @param shipment envío asociado al pago
     * @return lista de pagos del envío
     */
    public List<Payment> findByShipment(Shipment shipment) {
        return findAll().stream()
                .filter(p -> p.getShipment().equals(shipment))
                .collect(Collectors.toList());
    }
    
    /**
     * Busca pagos por el ID de un envío
     * @param shipmentId ID del envío
     * @return lista de pagos asociados al envío
     */
    public List<Payment> findByShipmentId(UUID shipmentId) {
        return findAll().stream()
                .filter(p -> p.getShipment() != null && 
                           p.getShipment().getId() != null && 
                           p.getShipment().getId().equals(shipmentId))
                .collect(Collectors.toList());
    }

    /**
     * Calcula el total de ingresos en un rango de fechas
     * @param startDate fecha inicial
     * @param endDate fecha final
     * @return total de ingresos
     */
    public double calculateIncomeForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return findByCreationDateBetween(startDate, endDate).stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    /**
     * Obtiene las estadísticas de uso de métodos de pago
     * @return mapa con el método de pago y la cantidad de usos
     */
    public java.util.Map<PaymentMethod, Long> getPaymentMethodStatistics() {
        return findAll().stream()
                .collect(Collectors.groupingBy(
                        Payment::getPaymentMethod,
                        Collectors.counting()
                ));
    }
}