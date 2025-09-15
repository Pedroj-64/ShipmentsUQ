package co.edu.uniquindio.sameday.shipmentsuqsameday.model;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IRateCalculator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la estructura de tarifas y cálculos de costos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rate implements IRateCalculator {
    @Builder.Default
    private double baseRate = 5000.0;        // Tarifa base en pesos
    
    @Builder.Default
    private double costPerKm = 1000.0;       // Costo por kilómetro
    
    @Builder.Default
    private double costPerKg = 2000.0;       // Costo por kilogramo
    
    @Builder.Default
    private double costPerM3 = 10000.0;      // Costo por metro cúbico
    
    @Builder.Default
    private double insuranceSurcharge = 0.1;  // 10% de recargo por seguro
    
    @Builder.Default
    private double fragileSurcharge = 0.15;   // 15% de recargo por paquete frágil

    @Override
    public double calculateBaseRate(double weight, double volume, double distance) {
        double weightCost = weight * costPerKg;
        double volumeCost = volume * costPerM3;
        double distanceCost = distance * costPerKm;
        
        return baseRate + weightCost + volumeCost + distanceCost;
    }

    @Override
    public double calculateSurcharges(double baseRate, boolean hasInsurance, boolean isFragile, double priorityMultiplier) {
        double totalCost = baseRate;
        
        if (hasInsurance) {
            totalCost += baseRate * insuranceSurcharge;
        }
        
        if (isFragile) {
            totalCost += baseRate * fragileSurcharge;
        }
        
        return totalCost * priorityMultiplier;
    }
    
    /**
     * Calcula el costo total de un envío
     * @param shipment envío a calcular
     * @return costo total calculado
     */
    public double calculateTotalCost(Shipment shipment) {
        double baseRate = calculateBaseRate(
            shipment.getWeight(),
            shipment.getVolume(),
            shipment.getDistance()
        );
        
        return calculateSurcharges(
            baseRate,
            shipment.isHasInsurance(),
            shipment.isFragile(),
            shipment.getPriority().getRateMultiplier()
        );
    }
}