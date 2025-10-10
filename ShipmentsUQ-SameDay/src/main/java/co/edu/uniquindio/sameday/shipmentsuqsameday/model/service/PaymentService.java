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
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de pagos
 */
public class PaymentService implements Service<Payment, PaymentRepository> {
    private final PaymentRepository repository;
    private final PaymentProcessingService processingService;
    private final PaymentAnalyticsService analyticsService;
    
    private static PaymentService instance;
    
    /**
     * Constructor del servicio
     */
    public PaymentService(
            PaymentRepository repository,
            PaymentProcessingService processingService,
            PaymentAnalyticsService analyticsService) {
        this.repository = repository;
        this.processingService = processingService;
        this.analyticsService = analyticsService;
    }
    
    /**
     * Obtiene la instancia única del servicio con los repositorios proporcionados
     * @param repository Repositorio de pagos
     * @param processingService Servicio de procesamiento
     * @param analyticsService Servicio de analíticas
     * @return instancia del servicio
     */
    public static synchronized PaymentService getInstance(
            PaymentRepository repository,
            PaymentProcessingService processingService,
            PaymentAnalyticsService analyticsService) {
        if (instance == null) {
            instance = new PaymentService(repository, processingService, analyticsService);
        }
        return instance;
    }
    
    /**
     * Obtiene la instancia única del servicio
     * Este método es para compatibilidad con código existente.
     * Debería llamarse primero getInstance(repository, ...) para inicializar el servicio.
     * @return instancia del servicio
     */
    public static PaymentService getInstance() {
        if (instance == null) {
            System.err.println("ERROR: PaymentService no ha sido inicializado con repositorios");
            throw new IllegalStateException("PaymentService no ha sido inicializado con repositorios");
        }
        return instance;
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
     * @throws RuntimeException si el pago falla
     */
    public Payment processPayment(Shipment shipment, PaymentMethod paymentMethod) {
        // Crear el nuevo pago en estado pendiente
        Payment payment = Payment.builder()
            .shipment(shipment)
            .user(shipment.getUser())
            .amount(shipment.getCost())
            .paymentMethod(paymentMethod)
            .status(PaymentStatus.PENDING)
            .creationDate(LocalDateTime.now())
            .processingDate(LocalDateTime.now())
            .build();
        
        // Guardar el pago inicial
        create(payment);
        
        // Procesar el pago usando el servicio especializado
        boolean success = processingService.processPayment(payment);
        
        if (!success) {
            throw new RuntimeException("Error al procesar el pago");
        }
        
        return payment;
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
        return analyticsService.calculateIncomeForPeriod(startDate, endDate);
    }
    
    /**
     * Obtiene estadísticas de uso de métodos de pago
     * @return mapa con estadísticas
     */
    public Map<PaymentMethod, Long> getPaymentMethodStatistics() {
        return analyticsService.getPaymentMethodStatistics();
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
        
        return processingService.processRefund(payment, reason);
    }
    
    /**
     * Busca todos los pagos de un usuario específico
     * @param userId ID del usuario
     * @return lista de pagos del usuario
     */
    public List<Payment> findByUser(UUID userId) {
        // Filtrar todos los pagos para encontrar los del usuario específico
        return repository.findAll().stream()
                .filter(payment -> payment.getUser() != null && 
                                   payment.getUser().getId() != null && 
                                   payment.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }
}