package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.mapping.Coordinates;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Clase que contiene los detalles calculados de un envío.
 * Soporta tanto coordenadas de Grid como coordenadas GPS reales
 */
@Data
@Builder
public class ShipmentDetails implements Serializable {

    private static final long serialVersionUID = 2L; // Incrementado para nueva versión
    
    // Campos existentes (mantener compatibilidad)
    private final double distance;
    private final double baseCost;
    private final double totalCost;
    private final double estimatedDuration;
    
    // Nuevos campos para coordenadas reales (opcionales)
    // Si son null, solo se usan coordenadas de Grid
    private final Coordinates originCoordinates;
    private final Coordinates destinationCoordinates;
    
    // Indica qué sistema de coordenadas se usó para los cálculos
    private final String coordinateSystem; // "Grid" o "Real GPS"
    
    /**
     * Verifica si este envío usa coordenadas reales
     * @return true si usa GPS real
     */
    public boolean usesRealCoordinates() {
        return originCoordinates != null && destinationCoordinates != null;
    }
}