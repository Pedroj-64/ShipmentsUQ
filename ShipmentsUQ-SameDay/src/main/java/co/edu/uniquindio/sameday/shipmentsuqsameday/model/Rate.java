package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase que representa la estructura de tarifas y cálculos de costos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rate implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID id;

    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveUntil;
    private boolean isActive;

    @Builder.Default
    private final double baseRate = 5000.0; 

    @Builder.Default
    private final double costPerKm = 1000.0; 

    @Builder.Default
    private final double costPerKg = 2000.0; 

    @Builder.Default
    private final double costPerM3 = 10000.0; 

    @Builder.Default
    private final double insuranceSurcharge = 0.1; // 10% de IVA por seguro

    @Builder.Default
    private final double fragileSurcharge = 0.15; // 15% de IVA por paquete frágil
}