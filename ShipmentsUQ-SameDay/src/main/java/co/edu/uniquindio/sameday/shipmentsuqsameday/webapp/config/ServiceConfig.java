package co.edu.uniquindio.sameday.shipmentsuqsameday.webapp.config;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.*;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.*;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.DataManager;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.ServiceInitializer;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Spring para inicializar servicios del dominio
 * Esta clase se encarga de inicializar todos los servicios Singleton
 * antes de que los controladores REST intenten usarlos.
 */
@Configuration
public class ServiceConfig {
    
    public ServiceConfig() {
        initializeServices();
    }
    
    private void initializeServices() {
        System.out.println("🔧 Inicializando servicios del dominio...");
        
        try {
            // Inicializar DataManager para cargar el estado de la aplicación
            DataManager dataManager = DataManager.getInstance();
            System.out.println("✅ DataManager inicializado");
            
            // Obtener repositorios del estado de la aplicación
            UserRepository userRepository = new UserRepository();
            DelivererRepository delivererRepository = new DelivererRepository();
            ShipmentRepository shipmentRepository = new ShipmentRepository();
            AddressRepository addressRepository = new AddressRepository();
            IncidentRepository incidentRepository = new IncidentRepository();
            
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
}
