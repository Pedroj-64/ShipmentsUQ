package co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IGridCoordinate;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO para transferir información básica de dirección
 */
@Data
@Builder
public class AddressDTO implements IGridCoordinate {
    private UUID id;
    private String alias;
    private String street;
    private String city;
    private String zone;
    private double coordX;  
    private double coordY;  
    
    /**
     * Implementación de IGridCoordinate para obtener la coordenada X
     * @return coordenada X de la dirección
     */
    @Override
    public double getX() {
        return coordX;
    }
    
    /**
     * Implementación de IGridCoordinate para obtener la coordenada Y
     * @return coordenada Y de la dirección
     */
    @Override
    public double getY() {
        return coordY;
    }
    
    // No incluye: datos personales del residente, códigos de acceso
}