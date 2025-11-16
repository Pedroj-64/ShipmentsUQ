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

        try {

            PaymentService.getInstance();
        } catch (IllegalStateException e) {

            PaymentRepository paymentRepository = new PaymentRepository();
            MockPaymentGateway paymentGateway = new MockPaymentGateway();

            PaymentProcessingService processingService = new PaymentProcessingService(paymentRepository, paymentGateway);
            PaymentAnalyticsService analyticsService = new PaymentAnalyticsService(paymentRepository);

            PaymentService.getInstance(paymentRepository, processingService, analyticsService);

            System.out.println("PaymentService inicializado correctamente");
        }

        initializeShipmentServiceWithDecorators();

        initialized = true;
    }

    /**
     * Inicializa el servicio de envíos con los decoradores
     * Este método demuestra la implementación del patrón Decorator
     */
    private static void initializeShipmentServiceWithDecorators() {
        try {

            ShipmentService baseService = ShipmentService.getInstance();

            System.out.println("Aplicando decoradores al servicio de envíos...");

            Service<Shipment, ShipmentRepository> decoratedService =
                    ShipmentServiceDecoratorFactory.createFullyDecoratedService(baseService);

            ShipmentServiceRegistry.setDecoratedService(decoratedService);

            System.out.println("ShipmentService decorado correctamente con validación, logging y notificaciones");
        } catch (Exception e) {
            System.err.println("Error al inicializar el servicio de envíos decorado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
