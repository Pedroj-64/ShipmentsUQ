package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.UserPaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserPaymentMethodDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de métodos de pago
 */
public class AddPaymentMethodController {

    private final UserService userService;
    private User currentUser;

    /**
     * Constructor del controlador
     */
    public AddPaymentMethodController() {
        this.userService = UserService.getInstance();
        
        // Obtenemos el usuario actual
        this.currentUser = UserDashboardController.getCurrentUser();
    }

    /**
     * Agrega un nuevo método de pago al usuario
     * @param paymentMethod Tipo de método de pago
     * @param alias Alias para identificarlo
     * @param isDefault Si es el método de pago predeterminado
     * @param additionalInfo Información adicional dependiendo del tipo (tarjeta, móvil o banco)
     * @return true si se agregó correctamente
     */
    public boolean addPaymentMethod(
            PaymentMethod paymentMethod,
            String alias,
            boolean isDefault,
            PaymentMethodInfo additionalInfo) {
        
        if (currentUser == null) {
            return false;
        }
        
        try {
            UserPaymentMethod newMethod = UserPaymentMethod.builder()
                    .id(UUID.randomUUID())
                    .user(currentUser)
                    .paymentMethod(paymentMethod)
                    .alias(alias)
                    .isDefault(isDefault)
                    .build();
            
            // Agregar información específica según el tipo de método
            switch (paymentMethod) {
                case CREDIT_CARD:
                case DEBIT_CARD:
                    // Para tarjetas
                    if (additionalInfo instanceof CardInfo) {
                        CardInfo cardInfo = (CardInfo) additionalInfo;
                        newMethod.setLastFourDigits(cardInfo.getLastFourDigits());
                        newMethod.setCardType(cardInfo.getCardType());
                    }
                    break;
                
                case NEQUI:
                case DAVIPLATA:
                    // Para pagos móviles
                    if (additionalInfo instanceof MobileInfo) {
                        MobileInfo mobileInfo = (MobileInfo) additionalInfo;
                        newMethod.setPhoneNumber(mobileInfo.getPhoneNumber());
                    }
                    break;
                
                case PSE:
                    // Para PSE
                    if (additionalInfo instanceof PseInfo) {
                        PseInfo pseInfo = (PseInfo) additionalInfo;
                        newMethod.setBankName(pseInfo.getBankName());
                        newMethod.setAccountType(pseInfo.getAccountType());
                    }
                    break;
                
                default:
                    // Otros métodos no necesitan información adicional
            }
            
            // Si es predeterminado, desmarcar los demás
            if (isDefault) {
                currentUser.getPaymentMethods().forEach(pm -> pm.setDefault(false));
            }
            
            // Agregar el método de pago al usuario
            return userService.addPaymentMethod(currentUser.getId(), newMethod) != null;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene todos los métodos de pago registrados
     * @return Lista de métodos de pago
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
     * Interfaz base para información adicional de métodos de pago
     */
    public interface PaymentMethodInfo {
        // Interfaz marcadora
    }
    
    /**
     * Información para tarjetas
     */
    public static class CardInfo implements PaymentMethodInfo {
        private String cardNumber;
        private String cardType;
        private String expiryDate;
        private String cvv;
        
        public CardInfo(String cardNumber, String cardType, String expiryDate, String cvv) {
            this.cardNumber = cardNumber;
            this.cardType = cardType;
            this.expiryDate = expiryDate;
            this.cvv = cvv;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public String getCardType() {
            return cardType;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public String getCvv() {
            return cvv;
        }
        
        public String getLastFourDigits() {
            if (cardNumber != null && cardNumber.length() >= 4) {
                return cardNumber.substring(cardNumber.length() - 4);
            }
            return "****";
        }
    }
    
    /**
     * Información para pagos móviles
     */
    public static class MobileInfo implements PaymentMethodInfo {
        private String phoneNumber;
        private String platform;
        
        public MobileInfo(String phoneNumber, String platform) {
            this.phoneNumber = phoneNumber;
            this.platform = platform;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getPlatform() {
            return platform;
        }
    }
    
    /**
     * Información para PSE
     */
    public static class PseInfo implements PaymentMethodInfo {
        private String bankName;
        private String accountType;
        
        public PseInfo(String bankName, String accountType) {
            this.bankName = bankName;
            this.accountType = accountType;
        }

        public String getBankName() {
            return bankName;
        }

        public String getAccountType() {
            return accountType;
        }
    }
}