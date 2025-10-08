package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
public class User implements Serializable {
    
    /** Constante para la serialización */
    private static final long serialVersionUID = 1L;
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
}
