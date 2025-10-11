package co.edu.uniquindio.sameday.shipmentsuqsameday.internalController;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.PaymentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.PaymentAnalyticsService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.PaymentProcessingService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.PaymentService;
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
        
        initialized = true;
    }
}