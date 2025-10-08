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
    
    /** Constante para la serialización */
    private static final long serialVersionUID = 1L;
    private UUID id;
    
    private LocalDateTime effectiveFrom;    // Fecha desde la que aplica la tarifa
    private LocalDateTime effectiveUntil;   // Fecha hasta la que aplica la tarifa (null si es la actual)
    private boolean isActive;               // Indica si es la tarifa actualmente vigente
    
    @Builder.Default
    private final double baseRate = 5000.0;        // Tarifa base en pesos
    
    @Builder.Default
    private final double costPerKm = 1000.0;       // Costo por kilómetro

    @Builder.Default
    private final double costPerKg = 2000.0;       // Costo por kilogramo

    @Builder.Default
    private final double costPerM3 = 10000.0;      // Costo por metro cúbico

    @Builder.Default
    private final double insuranceSurcharge = 0.1;  // 10% de recargo por seguro

    @Builder.Default
    private final double fragileSurcharge = 0.15;   // 15% de recargo por paquete frágil
}