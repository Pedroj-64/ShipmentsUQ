package co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;

/**
 * Tipos de prioridad para los envíos
 */
public enum ShipmentPriority {
    STANDARD(1.0),    // Entrega normal sin prioridad especial
    PRIORITY(1.5),    // Entrega prioritaria con recargo del 50%
    URGENT(2.0);      // Entrega urgente con recargo del 100%

    private final double rateMultiplier;

    ShipmentPriority(double rateMultiplier) {
        this.rateMultiplier = rateMultiplier;
    }

    public double getRateMultiplier() {
        return rateMultiplier;
    }
}