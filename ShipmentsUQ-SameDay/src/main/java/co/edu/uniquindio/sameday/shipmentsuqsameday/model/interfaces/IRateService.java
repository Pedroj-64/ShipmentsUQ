package co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Rate;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;

/**
 * Interfaz para el servicio de gestión de tarifas
 */
public interface IRateService {
    /**
     * Calcula el costo total de un envío
     */
    double calculateShipmentCost(Shipment shipment);
    
    /**
     * Obtiene la tarifa actual
     */
    Rate getCurrentRate();
    
    /**
     * Establece una nueva tarifa como actual
     */
    void setCurrentRate(Rate rate);
}