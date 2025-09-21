package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IRateCalculator;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Rate;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;

/**
 * Implementación estándar del calculador de tarifas
 */
public class StandardRateCalculator implements IRateCalculator {
    private final Rate rate;

    public StandardRateCalculator(Rate rate) {
        this.rate = rate;
    }

    @Override
    public double calculateBaseRate(double weight, double volume, double distance) {
        double weightCost = weight * rate.getCostPerKg();
        double volumeCost = volume * rate.getCostPerM3();
        double distanceCost = distance * rate.getCostPerKm();
        
        return rate.getBaseRate() + weightCost + volumeCost + distanceCost;
    }

    @Override
    public double calculateSurcharges(double baseRate, boolean hasInsurance, boolean isFragile, double priorityMultiplier) {
        double totalCost = baseRate;
        
        if (hasInsurance) {
            totalCost += baseRate * rate.getInsuranceSurcharge();
        }
        
        if (isFragile) {
            totalCost += baseRate * rate.getFragileSurcharge();
        }
        
        return totalCost * priorityMultiplier;
    }

    /**
     * Calcula el costo total de un envío
     */
    public double calculateTotalCost(Shipment shipment) {
        double baseRate = calculateBaseRate(
            shipment.getWeight(),
            shipment.getVolume(),
            shipment.getDetails().getDistance());
        return calculateSurcharges(
            baseRate,
            shipment.isHasInsurance(),
            shipment.isFragile(),
            shipment.getPriority().getRateMultiplier()
        );
    }
}