package co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO para transferir información segura de métodos de pago
 */
@Data
@Builder
public class UserPaymentMethodDTO {
    private UUID id;
    private UUID userId;
    private PaymentMethod paymentMethod;
    private String alias;
    private String lastFourDigits;
    private String cardType;
    private boolean isDefault;
    private String maskedPhoneNumber; // Últimos 4 dígitos del teléfono
    private String bankName;
    private String accountType;
    // No incluye: números completos de tarjeta, CVV, fechas de expiración
}