package co.edu.uniquindio.sameday.shipmentsuqsameday.internalController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.decorator.ShipmentServiceDecoratorFactory;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.PaymentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.PaymentAnalyticsService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.PaymentProcessingService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.PaymentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.Service;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.MockPaymentGateway;

/**
 * Clase utilitaria para inicializar servicios
 */
public class ServiceInitializer {
    
    private static boolean initialized = false;
    
    /**
     * Inicializa todos los servicios necesarios
     */
    public static void initializeServices() {
        if (initialized) {
            return;
        }
        
        // Inicializar el PaymentService si no está inicializado
        try {
            // Comprobar si ya está inicializado
            PaymentService.getInstance();
        } catch (IllegalStateException e) {
            // Si no está inicializado, inicializarlo
            PaymentRepository paymentRepository = new PaymentRepository();
            MockPaymentGateway paymentGateway = new MockPaymentGateway();
            
            // Inicializar servicios dependientes
            PaymentProcessingService processingService = new PaymentProcessingService(paymentRepository, paymentGateway);
            PaymentAnalyticsService analyticsService = new PaymentAnalyticsService(paymentRepository);
            
            // Inicializar PaymentService con los servicios necesarios
            PaymentService.getInstance(paymentRepository, processingService, analyticsService);
            
            System.out.println("PaymentService inicializado correctamente");
        }
        
        // Inicializar el ShipmentService con los decoradores
        initializeShipmentServiceWithDecorators();
        
        initialized = true;
    }
    
    /**
     * Inicializa el servicio de envíos con los decoradores
     * Este método demuestra la implementación del patrón Decorator
     */
    private static void initializeShipmentServiceWithDecorators() {
        try {
            // Verificar si ShipmentService ya está inicializado
            ShipmentService baseService = ShipmentService.getInstance();
            
            // Aplicar decoradores al servicio existente
            System.out.println("Aplicando decoradores al servicio de envíos...");
            
            // Creamos un servicio completamente decorado
            Service<Shipment, ShipmentRepository> decoratedService = 
                    ShipmentServiceDecoratorFactory.createFullyDecoratedService(baseService);
            
            // Guardamos una referencia al servicio decorado para su uso en la aplicación
            // Nota: En una implementación real, podríamos reemplazar la instancia singleton
            // o proporcionar un método para obtener el servicio decorado
            ShipmentServiceRegistry.setDecoratedService(decoratedService);
            
            System.out.println("ShipmentService decorado correctamente con validación, logging y notificaciones");
        } catch (Exception e) {
            System.err.println("Error al inicializar el servicio de envíos decorado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}