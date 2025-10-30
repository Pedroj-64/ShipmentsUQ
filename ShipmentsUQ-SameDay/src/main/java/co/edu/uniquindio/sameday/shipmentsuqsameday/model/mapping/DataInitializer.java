package co.edu.uniquindio.sameday.shipmentsuqsameday.model.mapping;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.AddressRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.DelivererRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.UserRepository;

import java.util.UUID;

/**
 * Clase encargada de inicializar datos de prueba para la aplicación
 */
public class DataInitializer {

    /**
     * Inicializa datos de prueba para usuarios
     * @param userRepository Repositorio de usuarios
     */
    public static void initializeUsers(UserRepository userRepository) {
        System.out.println("Inicializando usuarios de prueba...");
        
        // Verificar si ya existen usuarios para evitar duplicados
        if (!userRepository.findAll().isEmpty()) {
            System.out.println("Ya existen usuarios en el sistema. Omitiendo inicialización.");
            return;
        }
        
        // Usuario administrador
        User admin = User.builder()
                .id(UUID.randomUUID())
                .name("Administrador")
                .email("admin@gmail.com")
                .password("1234")
                .phone("3001234567")
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);
        
        // Usuario cliente normal
        User client = User.builder()
                .id(UUID.randomUUID())
                .name("Cliente Prueba")
                .email("cliente@gmail.com")
                .password("1234")
                .phone("3007654321")
                .role(UserRole.CLIENT)
                .build();
        userRepository.save(client);
        
        
        System.out.println("Usuarios de prueba creados: " + userRepository.findAll().size());
    }

    /**
     * Inicializa el repartidor por defecto en posición (0,0)
     * @param delivererRepository Repositorio de repartidores
     */
    public static void initializeDefaultDeliverer(DelivererRepository delivererRepository) {
        System.out.println("Inicializando repartidor por defecto...");
        
        if (delivererRepository.findAll().isEmpty()) {
            Deliverer defaultDeliverer = Deliverer.builder()
                .id(UUID.randomUUID())
                .name("Repartidor Default")
                .document("1234567890")
                .phone("3001234567")
                .status(DelivererStatus.AVAILABLE)
                .zone("Centro")
                .averageRating(5.0)
                .totalDeliveries(0)
                .currentX(0.0)
                .currentY(0.0)
                .build();
            
            delivererRepository.save(defaultDeliverer);
            System.out.println("Repartidor por defecto creado en posición (0,0)");
        } else {
            System.out.println("Ya existe al menos un repartidor en el sistema");
        }
    }

    
    /**
     * Inicializa datos de prueba para direcciones
     * @param addressRepository Repositorio de direcciones
     * @param userRepository Repositorio de usuarios
     */
    public static void initializeAddresses(AddressRepository addressRepository, UserRepository userRepository) {
        System.out.println("Inicializando direcciones de prueba...");
        
        // Verificar si ya existen direcciones para evitar duplicados
        if (!addressRepository.findAll().isEmpty()) {
            System.out.println("Ya existen direcciones en el sistema. Omitiendo inicialización.");
            return;
        }
        
        // Obtener usuarios
        User client = userRepository.findByEmail("cliente@gmail.com").orElse(null);
        if (client == null) {
            System.out.println("No se encontró el usuario cliente para asignar direcciones");
            return;
        }
        
        // Dirección 1 - Predeterminada
        Address address1 = Address.builder()
                .id(UUID.randomUUID())
                .alias("Casa")
                .street("Calle 15 # 23-45")
                .city("Armenia")
                .zone("Centro")
                .zipCode("630001")
                .coordX(10.0)
                .coordY(15.0)
                .isDefault(true)
                .build();
        addressRepository.save(address1);
        
        // Dirección 2
        Address address2 = Address.builder()
                .id(UUID.randomUUID())
                .alias("Oficina")
                .street("Carrera 14 # 7-82")
                .city("Armenia")
                .zone("Norte")
                .zipCode("630002")
                .coordX(12.0)
                .coordY(18.0)
                .isDefault(false)
                .build();
        addressRepository.save(address2);
        
        // Asignar direcciones al cliente
        client.getAddresses().add(address1);
        client.getAddresses().add(address2);
        userRepository.update(client);
        
        System.out.println("Direcciones de prueba creadas: " + addressRepository.findAll().size());
    }
    
    /**
     * Inicializa datos de prueba para repartidores
     * @param delivererRepository Repositorio de repartidores
     */
    public static void initializeDeliverers(DelivererRepository delivererRepository) {
        System.out.println("Inicializando repartidores de prueba...");
        
        // Verificar si ya existen repartidores para evitar duplicados
        if (!delivererRepository.findAll().isEmpty()) {
            System.out.println("Ya existen repartidores en el sistema. Omitiendo inicialización.");
            return;
        }
        
        // Repartidor 1 - Posición (0,0) para pruebas
        Deliverer deliverer1 = Deliverer.builder()
                .id(UUID.randomUUID())
                .name("Juan Pérez")
                .document("1094123456")
                .phone("3001112233")
                .status(DelivererStatus.AVAILABLE)
                .zone("Centro")
                .averageRating(4.8)
                .totalDeliveries(57)
                .currentX(0.0)  // Posición inicial en coordenada X para pruebas
                .currentY(0.0)  // Posición inicial en coordenada Y para pruebas
                .build();
        delivererRepository.save(deliverer1);
        
        // Repartidor 2 - Posición (5,10) para pruebas
        Deliverer deliverer2 = Deliverer.builder()
                .id(UUID.randomUUID())
                .name("Ana Gómez")
                .document("1094654321")
                .phone("3002223344")
                .status(DelivererStatus.AVAILABLE)
                .zone("Norte")
                .averageRating(4.6)
                .totalDeliveries(42)
                .currentX(5.0)
                .currentY(10.0)
                .build();
        delivererRepository.save(deliverer2);
        
        System.out.println("Repartidores de prueba creados: " + delivererRepository.findAll().size());
    }
    
    /**
     * Inicializa todos los datos de prueba
     * @param userRepository Repositorio de usuarios
     * @param addressRepository Repositorio de direcciones
     * @param delivererRepository Repositorio de repartidores
     */
    public static void initializeAllTestData(UserRepository userRepository, AddressRepository addressRepository, DelivererRepository delivererRepository) {
        initializeUsers(userRepository);
        initializeAddresses(addressRepository, userRepository);
        initializeDeliverers(delivererRepository);
    }
}