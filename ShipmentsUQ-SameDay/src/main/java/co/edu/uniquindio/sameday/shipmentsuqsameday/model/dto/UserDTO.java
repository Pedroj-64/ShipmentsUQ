package co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO para transferir información no sensible del usuario
 */
@Data
@Builder
public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    // No incluye: password, documentos de identidad, información bancaria
}