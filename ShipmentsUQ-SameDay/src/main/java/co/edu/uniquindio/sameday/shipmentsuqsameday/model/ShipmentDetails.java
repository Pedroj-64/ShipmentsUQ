package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import lombok.Builder;
import lombok.Data;

/**
 * Clase que contiene los detalles calculados de un envío.
 * para almacenar datos calculados.
 */
@Data
@Builder
public class ShipmentDetails {
    private final double distance;
    private final double baseCost;
    private final double totalCost;
    private final double estimatedDuration;
}