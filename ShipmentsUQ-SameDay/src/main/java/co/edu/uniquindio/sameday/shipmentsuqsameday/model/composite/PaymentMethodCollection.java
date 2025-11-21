package co.edu.uniquindio.sameday.shipmentsuqsameday.model.composite;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.UserPaymentMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Composite que representa una colección de métodos de pago del usuario
 * 
 * Patrón de Diseño: COMPOSITE
 * Problema que resuelve: El usuario tiene múltiples métodos de pago (tarjetas, PSE, Nequi, etc.)
 * y necesitamos tratarlos de manera uniforme, tanto individualmente como en conjunto.
 * 
 * Beneficio: Simplifica el manejo de múltiples métodos de pago, permite operaciones
 * sobre toda la colección y valida todos los métodos de una vez.
 */
public class PaymentMethodCollection implements IUserComponent {
    
    private static final long serialVersionUID = 1L;
    
    private UUID id;
    private String name; // ej: "Mis métodos de pago", "Tarjetas corporativas"
    private List<PaymentMethodComponent> paymentMethods;
    
    // Constructor sin parámetros (para Lombok @Builder)
    public PaymentMethodCollection() {
        this.id = UUID.randomUUID();
        this.name = "Métodos de Pago";
        this.paymentMethods = new ArrayList<>();
    }
    
    public PaymentMethodCollection(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.paymentMethods = new ArrayList<>();
    }
    
    // Getter para compatibilidad con User
    public List<PaymentMethodComponent> getComponents() {
        return new ArrayList<>(paymentMethods);
    }
    
    // Método de validación para User
    public boolean validate() {
        return isValid();
    }
    
    @Override
    public UUID getId() {
        return id;
    }
    
    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(name).append(" (").append(paymentMethods.size()).append(" métodos):\n");
        for (PaymentMethodComponent method : paymentMethods) {
            desc.append("  - ").append(method.getDescription()).append("\n");
        }
        return desc.toString();
    }
    
    @Override
    public boolean isValid() {
        // Una colección es válida si tiene al menos un método válido
        return !paymentMethods.isEmpty() && paymentMethods.stream().anyMatch(IUserComponent::isValid);
    }
    
    @Override
    public void add(IUserComponent component) {
        if (component instanceof PaymentMethodComponent) {
            paymentMethods.add((PaymentMethodComponent) component);
        } else {
            throw new IllegalArgumentException("Solo se pueden agregar PaymentMethodComponent a esta colección");
        }
    }
    
    @Override
    public void remove(IUserComponent component) {
        paymentMethods.remove(component);
    }
    
    @Override
    public IUserComponent getChild(int index) {
        if (index >= 0 && index < paymentMethods.size()) {
            return paymentMethods.get(index);
        }
        throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
    }
    
    @Override
    public List<IUserComponent> getChildren() {
        return new ArrayList<>(paymentMethods);
    }
    
    @Override
    public int count() {
        return paymentMethods.size();
    }
    
    /**
     * Obtiene el método de pago predeterminado
     * @return PaymentMethodComponent predeterminado o null si no hay
     */
    public PaymentMethodComponent getDefaultPaymentMethod() {
        return paymentMethods.stream()
                .filter(PaymentMethodComponent::isDefault)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Establece un método de pago como predeterminado
     * @param methodId ID del método a marcar como predeterminado
     */
    public void setDefaultPaymentMethod(UUID methodId) {
        // Desmarcar todos
        paymentMethods.forEach(method -> method.setDefault(false));
        
        // Marcar el seleccionado
        paymentMethods.stream()
                .filter(method -> method.getId().equals(methodId))
                .findFirst()
                .ifPresent(method -> method.setDefault(true));
    }
    
    /**
     * Obtiene todos los métodos de pago subyacentes
     * @return lista de objetos UserPaymentMethod
     */
    public List<UserPaymentMethod> getAllPaymentMethods() {
        return paymentMethods.stream()
                .map(PaymentMethodComponent::getPaymentMethod)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca un método de pago por ID
     * @param id ID del método
     * @return PaymentMethodComponent o null si no existe
     */
    public PaymentMethodComponent findById(UUID id) {
        return paymentMethods.stream()
                .filter(method -> method.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Busca métodos de pago por tipo
     * @param paymentMethod tipo de método (CREDIT_CARD, NEQUI, etc.)
     * @return lista de métodos de ese tipo
     */
    public List<PaymentMethodComponent> findByType(co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod paymentMethod) {
        return paymentMethods.stream()
                .filter(method -> method.getPaymentMethod().getPaymentMethod() == paymentMethod)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene solo las tarjetas (crédito y débito)
     * @return lista de métodos que son tarjetas
     */
    public List<PaymentMethodComponent> getCards() {
        return paymentMethods.stream()
                .filter(method -> {
                    var type = method.getPaymentMethod().getPaymentMethod();
                    return type == co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod.CREDIT_CARD 
                        || type == co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod.DEBIT_CARD;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene métodos de pago digital (Nequi, Daviplata)
     * @return lista de métodos digitales
     */
    public List<PaymentMethodComponent> getDigitalMethods() {
        return paymentMethods.stream()
                .filter(method -> {
                    var type = method.getPaymentMethod().getPaymentMethod();
                    return type == co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod.NEQUI 
                        || type == co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.PaymentMethod.DAVIPLATA;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica si la colección tiene un método predeterminado
     * @return true si hay un método predeterminado
     */
    public boolean hasDefaultPaymentMethod() {
        return getDefaultPaymentMethod() != null;
    }
    
    /**
     * Cuenta métodos de pago válidos
     * @return número de métodos válidos
     */
    public int countValid() {
        return (int) paymentMethods.stream()
                .filter(IUserComponent::isValid)
                .count();
    }
}
