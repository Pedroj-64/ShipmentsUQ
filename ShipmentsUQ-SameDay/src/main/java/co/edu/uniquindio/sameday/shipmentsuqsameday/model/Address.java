package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IGridCoordinate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Clase que representa una dirección en el sistema
 * Utiliza coordenadas cartesianas (X, Y) en lugar de coordenadas geográficas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address implements Serializable, IGridCoordinate {

    private static final long serialVersionUID = 1L;
    private UUID id;
    private String alias;
    private String street;
    private String zone;
    private String city;
    private String zipCode;
    private String complement;
    private double coordX;
    private double coordY;
    private boolean isDefault;

    /**
     * Implementación de IGridCoordinate para obtener la coordenada X
     * 
     * @return coordenada X de la dirección
     */
    @Override
    public double getX() {
        return coordX;
    }

    /**
     * Implementación de IGridCoordinate para obtener la coordenada Y
     * 
     * @return coordenada Y de la dirección
     */
    @Override
    public double getY() {
        return coordY;
    }

    /**
     * Obtiene la dirección completa formateada
     * 
     * @return String con la dirección completa
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();

        if (street != null && !street.isEmpty()) {
            sb.append(street);
        }

        if (complement != null && !complement.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(complement);
        }

        if (zone != null && !zone.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(zone);
        }

        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(city);
        }

        if (zipCode != null && !zipCode.isEmpty()) {
            if (sb.length() > 0)
                sb.append(" - ");
            sb.append(zipCode);
        }

        return sb.length() > 0 ? sb.toString() : "Dirección no especificada";
    }
}