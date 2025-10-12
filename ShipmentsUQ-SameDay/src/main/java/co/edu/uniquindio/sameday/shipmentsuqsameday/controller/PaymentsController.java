package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.ServiceInitializer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Payment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.HtmlGenerator;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.UserPaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.PaymentDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserPaymentMethodDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.PaymentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar pagos en el sistema
 */
public class PaymentsController {

    private final PaymentService paymentService;
    private final ShipmentService shipmentService;
    private final UserService userService;
    private User currentUser;

    /**
     * Constructor del controlador
     */
    public PaymentsController() {
        // Inicializamos los servicios antes de usarlos
        ServiceInitializer.initializeServices();
        
        this.paymentService = PaymentService.getInstance();
        this.shipmentService = ShipmentService.getInstance();
        this.userService = UserService.getInstance();
        
        // Obtenemos el usuario actual
        this.currentUser = UserDashboardController.getCurrentUser();
    }

    /**
     * Obtiene los métodos de pago guardados del usuario actual
     * @return Lista de métodos de pago como DTO
     */
    public List<UserPaymentMethodDTO> getSavedPaymentMethods() {
        if (currentUser == null) {
            return List.of();
        }
        
        return currentUser.getPaymentMethods().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte un método de pago a DTO
     * @param method Método de pago
     * @return DTO del método de pago
     */
    private UserPaymentMethodDTO convertToDTO(UserPaymentMethod method) {
        return UserPaymentMethodDTO.builder()
                .id(method.getId())
                .paymentMethod(method.getPaymentMethod())
                .alias(method.getAlias())
                .lastFourDigits(method.getLastFourDigits())
                .cardType(method.getCardType())
                .isDefault(method.isDefault())
                .phoneNumber(method.getPhoneNumber())
                .bankName(method.getBankName())
                .accountType(method.getAccountType())
                .build();
    }
    
    /**
     * Obtiene el historial de pagos del usuario actual
     * @return Lista de pagos como DTO
     */
    public List<PaymentDTO> getPaymentHistory() {
        if (currentUser == null) {
            return List.of();
        }
        
        List<Payment> payments = paymentService.findByUser(currentUser.getId());
        return payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte un pago a DTO
     * @param payment Pago
     * @return DTO del pago
     */
    private PaymentDTO convertToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .shipmentId(payment.getShipment() != null ? payment.getShipment().getId() : null)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .creationDate(payment.getCreationDate())
                .processingDate(payment.getProcessingDate())
                .paymentReference(payment.getPaymentReference())
                .build();
    }
    
    /**
     * Filtra pagos por fecha y estado
     * @param date Fecha de filtrado (opcional)
     * @param status Estado de filtrado (opcional)
     * @return Lista de pagos filtrados
     */
    public List<PaymentDTO> filterPayments(LocalDate date, PaymentStatus status) {
        if (currentUser == null) {
            return List.of();
        }
        
        List<Payment> payments = paymentService.findByUser(currentUser.getId());
        
        // Aplicar filtros si están presentes
        if (date != null) {
            payments = payments.stream()
                    .filter(p -> p.getCreationDate().toLocalDate().equals(date))
                    .collect(Collectors.toList());
        }
        
        if (status != null) {
            payments = payments.stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        return payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Procesa un nuevo pago
     * @param shipmentId ID del envío
     * @param amount Monto del pago
     * @param paymentMethod Método de pago
     * @param dueDate Fecha límite de pago
     * @param reference Referencia o comentario
     * @return true si el pago fue exitoso
     */
    public boolean processPayment(String shipmentId, double amount, PaymentMethod paymentMethod, 
            LocalDate dueDate, String reference) {
        try {
            if (currentUser == null) {
                return false;
            }
            
            // Validar que el envío exista
            Optional<Shipment> shipmentOpt = shipmentService.findById(UUID.fromString(shipmentId));
            if (shipmentOpt.isEmpty()) {
                return false;
            }
            
            Shipment shipment = shipmentOpt.get();
            
            // Crear y procesar el pago
            Payment payment = Payment.builder()
                    .shipment(shipment)
                    .user(currentUser)
                    .amount(amount)
                    .paymentMethod(paymentMethod)
                    .status(PaymentStatus.PENDING)
                    .creationDate(LocalDateTime.now())
                    .transactionDetails(reference)
                    .build();
            
            Payment processedPayment = paymentService.processPayment(shipment, paymentMethod);
            
            boolean paymentSuccess = processedPayment.getStatus() == PaymentStatus.COMPLETED;
            
            // Si el pago fue exitoso, intentar asignar un repartidor
            if (paymentSuccess) {
                try {
                    // Intentar asignar repartidor
                    boolean assignmentSuccess = shipmentService.tryAssignDeliverer(shipment.getId());
                    
                    if (assignmentSuccess) {
                        System.out.println("Pago exitoso y repartidor asignado automáticamente para el envío: " 
                            + shipment.getId());
                    } else {
                        System.out.println("Pago exitoso pero no se pudo asignar un repartidor automáticamente para el envío: " 
                            + shipment.getId());
                    }
                    
                    // Guardar el estado actualizado después del pago y asignación
                    co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.DataManager.getInstance().saveState();
                } catch (Exception ex) {
                    System.err.println("Error al intentar asignar repartidor después del pago: " + ex.getMessage());
                    // No afectamos el resultado del pago si falla la asignación
                }
            }
            
            return paymentSuccess;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene un comprobante de pago y lo guarda como HTML en la carpeta de descargas
     * @param paymentId ID del pago
     * @return Ruta completa al archivo de comprobante generado, o null si hay error
     */
    public String getPaymentReceipt(UUID paymentId) {
        try {
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return null;
            }
            
            Payment payment = paymentOpt.get();
            
            // Generar el comprobante HTML utilizando la clase HtmlGenerator
            return HtmlGenerator.generatePaymentReceipt(payment);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Genera un reporte de pagos y lo guarda como HTML en la carpeta de descargas
     * @return Ruta completa al archivo del reporte generado, o null si hay error
     */
    public String generatePaymentReport() {
        try {
            if (currentUser == null) {
                return null;
            }
            
            // Obtener la lista de pagos del usuario actual
            List<Payment> payments = paymentService.findByUser(currentUser.getId());
            
            // Generar el reporte HTML utilizando la clase HtmlGenerator
            return HtmlGenerator.generatePaymentReport(currentUser, payments);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene el monto pendiente de pago para un envío específico
     * @param shipmentId El ID del envío
     * @return El monto a pagar o null si no se encuentra
     */
    public Double getShipmentAmount(UUID shipmentId) {
        try {
            if (shipmentId == null) {
                return null;
            }
            
            // Buscar el envío
            Optional<Shipment> shipmentOpt = shipmentService.findById(shipmentId);
            if (shipmentOpt.isEmpty()) {
                return null;
            }
            
            Shipment shipment = shipmentOpt.get();
            
            // Verificar si el usuario actual es el dueño del envío
            if (currentUser == null || !currentUser.getId().equals(shipment.getUser().getId())) {
                return null;
            }
            
            // Verificar si el envío ya está pagado
            List<Payment> payments = paymentService.findByShipmentId(shipmentId);
            boolean isPaid = payments.stream()
                    .anyMatch(p -> p.getStatus() == PaymentStatus.COMPLETED);
            
            if (isPaid) {
                return 0.0; // Ya está pagado
            }
            
            // Devolver el costo del envío
            return shipment.getCost();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}