package co.edu.uniquindio.sameday.shipmentsuqsameday.webapp;

import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.ServiceInitializer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.*;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.*;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.DataManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Aplicación Spring Boot para la versión web de ShipmentsUQ
 * Esta aplicación expone una REST API que reutiliza toda la lógica de negocio existente
 * 
 * @author MargaDev-Society
 */
@SpringBootApplication
@ComponentScan(basePackages = "co.edu.uniquindio.sameday.shipmentsuqsameday")
public class WebApplication {
    
    static {
        // Inicializar servicios ANTES de que Spring cree los beans
        initializeServices();
    }
    
    private static void initializeServices() {
        System.out.println("🔧 Inicializando servicios del dominio...");
        
        try {
            // Inicializar DataManager para cargar el estado de la aplicación
            DataManager.getInstance();
            System.out.println("✅ DataManager inicializado");
            
            // Obtener repositorios del estado de la aplicación
            UserRepository userRepository = new UserRepository();
            DelivererRepository delivererRepository = new DelivererRepository();
            ShipmentRepository shipmentRepository = new ShipmentRepository();
            
            // Inicializar servicios en el orden correcto de dependencias
            
            // 1. Servicios sin dependencias
            UserService.getInstance(userRepository);
            System.out.println("✅ UserService inicializado");
            
            DelivererService.getInstance(delivererRepository);
            System.out.println("✅ DelivererService inicializado");
            
            IncidentService.getInstance();
            System.out.println("✅ IncidentService inicializado");
            
            AddressService.getInstance();
            System.out.println("✅ AddressService inicializado");
            
            // 2. ShipmentService con dependencias
            ShipmentService.getInstance(
                shipmentRepository,
                DelivererService.getInstance(),
                IncidentService.getInstance()
            );
            System.out.println("✅ ShipmentService inicializado");
            
            // 3. Inicializar servicios adicionales (PaymentService con decoradores, etc.)
            ServiceInitializer.initializeServices();
            System.out.println("✅ Servicios adicionales inicializados");
            
            System.out.println("🎉 Todos los servicios inicializados correctamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar servicios: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudieron inicializar los servicios", e);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Iniciando ShipmentsUQ Web Server...");
        SpringApplication.run(WebApplication.class, args);
        System.out.println("✅ Servidor web iniciado en http://localhost:8080");
        System.out.println("📚 Documentación API: http://localhost:8080/api/docs");
    }
}
