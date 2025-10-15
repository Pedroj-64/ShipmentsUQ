package co.edu.uniquindio.sameday.shipmentsuqsameday.model.decorator;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.Service;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;

/**
 * Fábrica de decoradores para el servicio de envíos
 * Esta clase facilita la creación y configuración de decoradores para el servicio de envíos
 */
public class ShipmentServiceDecoratorFactory {
    
    /**
     * Método privado para evitar instanciación
     */
    private ShipmentServiceDecoratorFactory() {
        // Constructor privado para evitar instanciación
    }
    
    /**
     * Crea un servicio de envíos decorado con validación
     * @param service servicio base a decorar
     * @return servicio decorado
     */
    public static Service<Shipment, ShipmentRepository> createValidationDecorator(
            Service<Shipment, ShipmentRepository> service) {
        return new ValidationShipmentDecorator(service);
    }
    
    /**
     * Crea un servicio de envíos decorado con notificaciones
     * @param service servicio base a decorar
     * @return servicio decorado
     */
    public static Service<Shipment, ShipmentRepository> createNotificationDecorator(
            Service<Shipment, ShipmentRepository> service) {
        return new NotificationShipmentDecorator(service);
    }
    
    /**
     * Crea un servicio de envíos decorado con registro de operaciones
     * @param service servicio base a decorar
     * @return servicio decorado
     */
    public static Service<Shipment, ShipmentRepository> createLoggingDecorator(
            Service<Shipment, ShipmentRepository> service) {
        return new LoggingShipmentDecorator(service);
    }
    
    /**
     * Crea un servicio de envíos completamente decorado con validación, notificación y logging
     * @param baseService servicio base a decorar
     * @return servicio completamente decorado
     */
    public static Service<Shipment, ShipmentRepository> createFullyDecoratedService(
            ShipmentService baseService) {
        // Aplicamos los decoradores en capas (validación -> logging -> notificación)
        Service<Shipment, ShipmentRepository> validatedService = 
                createValidationDecorator(baseService);
        
        Service<Shipment, ShipmentRepository> loggedService = 
                createLoggingDecorator(validatedService);
        
        return createNotificationDecorator(loggedService);
    }
}