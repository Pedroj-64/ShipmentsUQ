package co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO para transferir información básica de dirección
 */
@Data
@Builder
public class AddressDTO {
    private UUID id;
    private String street;
    private String city;
    private String zone;
    private double latitude;
    private double longitude;
    // No incluye: datos personales del residente, códigos de acceso
}