package co.edu.uniquindio.sameday.shipmentsuqsameday.model.composite;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.UserPaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;

import java.util.UUID;

/**
 * Leaf (Hoja) que representa un método de pago individual del usuario
 * 
 * Patrón de Diseño: COMPOSITE
 * Esta clase representa un elemento individual (hoja) en la estructura composite.
 * Envuelve un objeto UserPaymentMethod para integrarlo en la jerarquía del patrón.
 */
public class PaymentMethodComponent implements IUserComponent {
    
    private static final long serialVersionUID = 1L;
    
    private UserPaymentMethod paymentMethod;
    private boolean isDefault;
    
    public PaymentMethodComponent(UserPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        this.isDefault = false;
    }
    
    public PaymentMethodComponent(UserPaymentMethod paymentMethod, boolean isDefault) {
        this.paymentMethod = paymentMethod;
        this.isDefault = isDefault;
    }
    
    @Override
    public UUID getId() {
        return paymentMethod.getId();
    }
    
    @Override
    public String getDescription() {
        String defaultMarker = isDefault ? " [PREDETERMINADO]" : "";
        String alias = paymentMethod.getAlias() != null ? paymentMethod.getAlias() : "Sin alias";
        
        PaymentMethod type = paymentMethod.getPaymentMethod();
        String typeDesc = getPaymentMethodDescription(type);
        
        return String.format("%s - %s%s", alias, typeDesc, defaultMarker);
    }
    
    @Override
    public boolean isValid() {
        // Un método de pago es válido si tiene los campos mínimos requeridos
        if (paymentMethod == null) {
            return false;
        }
        
        PaymentMethod type = paymentMethod.getPaymentMethod();
        
        // Validaciones específicas según el tipo
        switch (type) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                return paymentMethod.getLastFourDigits() != null 
                    && !paymentMethod.getLastFourDigits().isEmpty();
                
            case NEQUI:
            case DAVIPLATA:
                return paymentMethod.getPhoneNumber() != null 
                    && !paymentMethod.getPhoneNumber().isEmpty();
                
            case PSE:
                return paymentMethod.getBankName() != null 
                    && !paymentMethod.getBankName().isEmpty();
                
            case CASH:
                return true; // Efectivo siempre es válido
                
            default:
                return false;
        }
    }
    
    @Override
    public int count() {
        return 1; // Una hoja siempre cuenta como 1
    }
    
    /**
     * Obtiene el objeto UserPaymentMethod subyacente
     * @return el objeto UserPaymentMethod
     */
    public UserPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    /**
     * Verifica si es el método de pago predeterminado
     * @return true si es predeterminado
     */
    public boolean isDefault() {
        return isDefault;
    }
    
    /**
     * Establece si es el método de pago predeterminado
     * @param isDefault true para marcar como predeterminado
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    /**
     * Obtiene el tipo de método de pago
     * @return tipo de método
     */
    public PaymentMethod getType() {
        return paymentMethod.getPaymentMethod();
    }
    
    /**
     * Obtiene el alias del método de pago
     * @return alias
     */
    public String getAlias() {
        return paymentMethod.getAlias();
    }
    
    /**
     * Verifica si es una tarjeta (crédito o débito)
     * @return true si es tarjeta
     */
    public boolean isCard() {
        PaymentMethod type = paymentMethod.getPaymentMethod();
        return type == PaymentMethod.CREDIT_CARD || type == PaymentMethod.DEBIT_CARD;
    }
    
    /**
     * Verifica si es un método digital (Nequi, Daviplata)
     * @return true si es método digital
     */
    public boolean isDigital() {
        PaymentMethod type = paymentMethod.getPaymentMethod();
        return type == PaymentMethod.NEQUI || type == PaymentMethod.DAVIPLATA;
    }
    
    /**
     * Obtiene una descripción legible del tipo de método de pago
     */
    private String getPaymentMethodDescription(PaymentMethod type) {
        switch (type) {
            case CREDIT_CARD:
                return "Tarjeta de Crédito **** " + getLastFourDigits();
            case DEBIT_CARD:
                return "Tarjeta de Débito **** " + getLastFourDigits();
            case NEQUI:
                return "Nequi " + maskPhoneNumber();
            case DAVIPLATA:
                return "Daviplata " + maskPhoneNumber();
            case PSE:
                return "PSE - " + paymentMethod.getBankName();
            case CASH:
                return "Efectivo";
            default:
                return type.toString();
        }
    }
    
    /**
     * Obtiene los últimos 4 dígitos de la tarjeta
     */
    private String getLastFourDigits() {
        String lastFour = paymentMethod.getLastFourDigits();
        if (lastFour != null && !lastFour.isEmpty()) {
            return lastFour;
        }
        return "****";
    }
    
    /**
     * Enmascara el número de teléfono
     */
    private String maskPhoneNumber() {
        String phone = paymentMethod.getPhoneNumber();
        if (phone != null && phone.length() >= 4) {
            return "****" + phone.substring(phone.length() - 4);
        }
        return "****";
    }
}
