package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Rate;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.IRateCalculator;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.RateRepository;

/**
 * Servicio para el cálculo de tarifas.
 * Delega los cálculos a un IRateCalculator y gestiona las tarifas.
 */
public class RateService implements Service<Rate, RateRepository> {
    private final RateRepository repository;
    private final IRateCalculator calculator;
    private final ShipmentService shipmentService;
    
    public RateService(
            RateRepository repository, 
            IRateCalculator calculator,
            ShipmentService shipmentService) {
        this.repository = repository;
        this.calculator = calculator;
        this.shipmentService = shipmentService;
    }
    
    @Override
    public RateRepository getRepository() {
        return repository;
    }
    
    /**
     * Calcula el costo base de un envío usando el calculador
     * @param weight peso del paquete en kg
     * @param volume volumen del paquete en m³
     * @param distance distancia del envío en km
     * @return costo base calculado
     */
    public double calculateBaseRate(double weight, double volume, double distance) {
        return calculator.calculateBaseRate(weight, volume, distance);
    }

    /**
     * Calcula los recargos adicionales usando el calculador
     * @param baseRate costo base del envío
     * @param hasInsurance si incluye seguro
     * @param isFragile si es un paquete frágil
     * @param priorityMultiplier multiplicador de prioridad
     * @return costo total con recargos
     */
    public double calculateSurcharges(double baseRate, boolean hasInsurance, boolean isFragile, double priorityMultiplier) {
        return calculator.calculateSurcharges(baseRate, hasInsurance, isFragile, priorityMultiplier);
    }
    
    /**
     * Calcula el costo total de un envío
     * @param shipment envío a calcular
     * @return costo total calculado
     */
    public double calculateShipmentCost(Shipment shipment) {
        double distance = shipmentService.calculateShipmentDistance(shipment);
        
        double baseRate = calculator.calculateBaseRate(
            shipment.getWeight(),
            shipment.getVolume(),
            distance
        );
        
        return calculator.calculateSurcharges(
            baseRate,
            shipment.isHasInsurance(),
            shipment.isFragile(),
            shipment.getPriority().getRateMultiplier()
        );
    }
    
    /**
     * Actualiza la tarifa actual en el sistema
     * @param rate nueva tarifa a establecer
     */
    public void updateCurrentRate(Rate rate) {
        repository.setCurrentRate(rate);
        create(rate);
    }
    
    /**
     * Obtiene la tarifa actual del sistema
     * @return tarifa actual vigente
     */
    public Rate getCurrentRate() {
        return repository.getCurrentRate();
    }
}