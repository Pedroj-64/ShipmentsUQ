package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase que representa un usuario en el sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String password;
    private UserRole role;
    
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();
    
    @Builder.Default
    private List<UserPaymentMethod> paymentMethods = new ArrayList<>();
    
    @Builder.Default
    private List<Shipment> shipmentHistory = new ArrayList<>();
    
    /**
     * Agrega una nueva dirección al usuario
     * @param address dirección a agregar
     */
    public void addAddress(Address address) {
        this.addresses.add(address);
    }
    
    /**
     * Agrega un nuevo método de pago al usuario
     * @param paymentMethod método de pago a agregar
     */
    public void addPaymentMethod(UserPaymentMethod paymentMethod) {
        this.paymentMethods.add(paymentMethod);
    }
    
    /**
     * Agrega un envío al historial del usuario
     * @param shipment envío a agregar
     */
    public void addToShipmentHistory(Shipment shipment) {
        this.shipmentHistory.add(shipment);
    }
    
    /**
     * Obtiene la dirección predeterminada del usuario
     * @return dirección predeterminada o null si no existe
     */
    public Address getDefaultAddress() {
        return addresses.stream()
            .filter(Address::isDefault)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Establece una dirección como predeterminada
     * @param address dirección a establecer como predeterminada
     */
    public void setDefaultAddress(Address address) {
        if (!addresses.contains(address)) {
            throw new IllegalArgumentException("La dirección no pertenece a este usuario");
        }
        
        addresses.forEach(a -> a.setDefault(false));
        address.setDefault(true);
    }
    
    /**
     * Obtiene el método de pago predeterminado del usuario
     * @return método de pago predeterminado o null si no existe
     */
    public UserPaymentMethod getDefaultPaymentMethod() {
        return paymentMethods.stream()
            .filter(UserPaymentMethod::isDefault)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Establece un método de pago como predeterminado
     * @param paymentMethod método de pago a establecer como predeterminado
     */
    public void setDefaultPaymentMethod(UserPaymentMethod paymentMethod) {
        if (!paymentMethods.contains(paymentMethod)) {
            throw new IllegalArgumentException("El método de pago no pertenece a este usuario");
        }
        
        paymentMethods.forEach(pm -> pm.setDefault(false));
        paymentMethod.setDefault(true);
    }
}
