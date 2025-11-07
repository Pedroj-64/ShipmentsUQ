package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

/**
 * Clase que representa un método de pago asociado a un usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPaymentMethod implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID id;
    private User user;
    private PaymentMethod paymentMethod;
    private String alias;
    private String lastFourDigits;
    private String cardType;
    private boolean isDefault;
    private String phoneNumber;
    private String bankName;
    private String accountType;
}