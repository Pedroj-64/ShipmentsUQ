package co.edu.uniquindio.sameday.shipmentsuqsameday.webapp.api;

import co.edu.uniquindio.sameday.shipmentsuqsameday.controller.PaymentsController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.PaymentDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserPaymentMethodDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Controlador REST para gestión de pagos
 * Reutiliza PaymentsController existente
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {
    
    private final PaymentsController paymentsController;
    
    public PaymentRestController() {
        this.paymentsController = new PaymentsController();
    }
    
    /**
     * Obtener historial de pagos
     * GET /api/payments/history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory() {
        try {
            List<PaymentDTO> payments = paymentsController.getPaymentHistory();
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener historial: " + e.getMessage()));
        }
    }
    
    /**
     * Procesar un pago
     * POST /api/payments/process
     */
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody ProcessPaymentRequest request) {
        try {
            boolean success = paymentsController.processPayment(
                    request.getShipmentId(),
                    request.getAmount(),
                    PaymentMethod.valueOf(request.getPaymentMethod()),
                    request.getDueDate() != null ? LocalDate.parse(request.getDueDate()) : null,
                    request.getReference()
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Pago procesado correctamente"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No se pudo procesar el pago"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar pago: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener métodos de pago guardados
     * GET /api/payments/methods
     */
    @GetMapping("/methods")
    public ResponseEntity<?> getSavedPaymentMethods() {
        try {
            List<UserPaymentMethodDTO> methods = paymentsController.getSavedPaymentMethods();
            return ResponseEntity.ok(methods);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener métodos de pago: " + e.getMessage()));
        }
    }
    
    /**
     * Eliminar método de pago
     * DELETE /api/payments/methods/{id}
     */
    @DeleteMapping("/methods/{id}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable String id) {
        try {
            UUID methodId = UUID.fromString(id);
            boolean success = paymentsController.deletePaymentMethod(methodId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Método de pago eliminado"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Método de pago no encontrado"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar método: " + e.getMessage()));
        }
    }
    
    /**
     * Actualizar alias de método de pago
     * PUT /api/payments/methods/{id}/alias
     */
    @PutMapping("/methods/{id}/alias")
    public ResponseEntity<?> updatePaymentMethodAlias(
            @PathVariable String id,
            @RequestBody UpdateAliasRequest request) {
        try {
            UUID methodId = UUID.fromString(id);
            boolean success = paymentsController.updatePaymentMethodAlias(methodId, request.getAlias());
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Alias actualizado correctamente"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Método de pago no encontrado"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar alias: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener comprobante de pago
     * GET /api/payments/{id}/receipt
     */
    @GetMapping("/{id}/receipt")
    public ResponseEntity<?> getPaymentReceipt(@PathVariable String id) {
        try {
            UUID paymentId = UUID.fromString(id);
            String receiptPath = paymentsController.getPaymentReceipt(paymentId);
            
            if (receiptPath != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "receiptPath", receiptPath
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Comprobante no encontrado"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener comprobante: " + e.getMessage()));
        }
    }
    
    // DTOs
    
    public static class ProcessPaymentRequest {
        private String shipmentId;
        private Double amount;
        private String paymentMethod;
        private String dueDate;
        private String reference;
        
        public String getShipmentId() { return shipmentId; }
        public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
    
    public static class UpdateAliasRequest {
        private String alias;
        
        public String getAlias() { return alias; }
        public void setAlias(String alias) { this.alias = alias; }
    }
}
