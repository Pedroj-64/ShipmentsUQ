package co.edu.uniquindio.sameday.shipmentsuqsameday.internalController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.Service;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;

/**
 * Registro para mantener referencias a los servicios decorados
 * Esta clase permite acceder al servicio de envíos decorado desde cualquier parte de la aplicación
 */
public class ShipmentServiceRegistry {
    
    // Instancia sin decoradores (servicio base)
    private static ShipmentService baseService;
    
    // Instancia decorada del servicio
    private static Service<Shipment, ShipmentRepository> decoratedService;
    
    /**
     * Constructor privado para evitar instanciación
     */
    private ShipmentServiceRegistry() {
        // Constructor privado para evitar instanciación
    }
    
    /**
     * Establece el servicio base
     * @param service servicio base sin decoradores
     */
    public static void setBaseService(ShipmentService service) {
        baseService = service;
    }
    
    /**
     * Establece el servicio decorado
     * @param service servicio decorado
     */
    public static void setDecoratedService(Service<Shipment, ShipmentRepository> service) {
        decoratedService = service;
    }
    
    /**
     * Obtiene el servicio base sin decoradores
     * @return servicio base
     */
    public static ShipmentService getBaseService() {
        return baseService;
    }
    
    /**
     * Obtiene el servicio decorado
     * @return servicio decorado con todas las funcionalidades adicionales
     */
    public static Service<Shipment, ShipmentRepository> getDecoratedService() {
        return decoratedService;
    }
}