package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Rate;

/**
 * Servicio para el cálculo de tarifas
 */
public class RateService {
    private final Rate rate;
    
    // Constructor privado para Singleton
    private RateService() {
        this.rate = Rate.builder()
            .baseRate(5000.0)
            .costPerKm(1000.0)
            .costPerKg(2000.0)
            .costPerM3(10000.0)
            .insuranceSurcharge(0.1)
            .fragileSurcharge(0.15)
            .build();
    }
    
    // Holder estático para instancia única
    private static class SingletonHolder {
        private static final RateService INSTANCE = new RateService();
    }
    
    /**
     * Obtiene la instancia única del servicio
     * @return instancia del servicio
     */
    public static RateService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    /**
     * Calcula el costo de un envío
     * @param shipment envío a calcular
     * @return costo calculado
     */
    public double calculateShipmentCost(Shipment shipment) {
        return rate.calculateTotalCost(shipment);
    }
    
    /**
     * Obtiene la tarifa actual
     * @return tarifa actual
     */
    public Rate getRate() {
        return rate;
    }
}