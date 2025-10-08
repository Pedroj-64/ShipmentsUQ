package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import java.io.IOException;
import java.util.UUID;

import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.*;

/**
 * Administrador de datos para cargar y guardar el estado de la aplicación.
 * Esta clase coordina la persistencia entre sesiones.
 */
public class DataManager {
    
    private static final String APP_STATE_FILE = "app_state";
    
    // Instancia única (Singleton)
    private static DataManager instance;
    
    // Estado de la aplicación
    private AppState appState;
    
    // Repositorios
    private UserRepository userRepository;
    private DelivererRepository delivererRepository;
    private ShipmentRepository shipmentRepository;
    private AddressRepository addressRepository;
    private PaymentRepository paymentRepository;
    private RateRepository rateRepository;
    private IncidentRepository incidentRepository;
    
    // Servicios - No necesitamos declarar servicios aquí porque trabajamos directamente con los repositorios
    
    /**
     * Constructor privado para el patrón Singleton.
     */
    private DataManager() {
        // Inicializar repositorios y servicios
        initRepositoriesAndServices();
        
        // Cargar el estado guardado o crear uno nuevo
        loadOrCreateState();
    }
    
    /**
     * Obtiene la instancia única del DataManager.
     * 
     * @return La instancia del DataManager
     */
    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    /**
     * Inicializa los repositorios y servicios.
     */
    private void initRepositoriesAndServices() {
        // Inicializar repositorios
        userRepository = new UserRepository();
        delivererRepository = new DelivererRepository();
        shipmentRepository = new ShipmentRepository();
        addressRepository = new AddressRepository();
        paymentRepository = new PaymentRepository();
        rateRepository = new RateRepository();
        incidentRepository = new IncidentRepository();
        
        // No necesitamos inicializar servicios aquí porque trabajamos directamente con los repositorios
    }
    
    /**
     * Carga el estado guardado o crea uno nuevo si no existe.
     */
    private void loadOrCreateState() {
        try {
            if (Serializer.existeArchivo(APP_STATE_FILE)) {
                // Cargar estado desde el archivo
                appState = (AppState) Serializer.cargarEstado(APP_STATE_FILE);
                System.out.println("Estado de la aplicación cargado exitosamente.");
                
                // Cargar datos en los repositorios
                loadRepositories();
            } else {
                // Crear un nuevo estado
                appState = new AppState();
                System.out.println("Se ha creado un nuevo estado para la aplicación.");
                
                // Aquí podrías inicializar datos de prueba
                initializeTestData();
            }
        } catch (Exception e) {
            // Si ocurre un error al cargar, crear un nuevo estado
            appState = new AppState();
            System.err.println("Error al cargar el estado de la aplicación: " + e.getMessage());
            e.printStackTrace();
            
            // Inicializar datos de prueba
            initializeTestData();
        }
    }
    
    /**
     * Carga los datos desde AppState a los repositorios.
     */
    private void loadRepositories() {
        userRepository.loadEntities(appState.getUsers());
        delivererRepository.loadEntities(appState.getDeliverers());
        shipmentRepository.loadEntities(appState.getShipments());
        addressRepository.loadEntities(appState.getAddresses());
        paymentRepository.loadEntities(appState.getPayments());
        rateRepository.loadEntities(appState.getRates());
        incidentRepository.loadEntities(appState.getIncidents());
    }
    
    /**
     * Inicializa datos de prueba para la aplicación.
     * Este método se llama cuando no hay datos previos guardados.
     */
    private void initializeTestData() {
        System.out.println("Inicializando datos de prueba...");
        
        try {
            // Crear usuarios de prueba
            User adminUser = User.builder()
                .id(UUID.randomUUID())
                .name("Administrador Principal")
                .email("admin@shipmentsuq.com")
                .phone("1234567890")
                .password("admin")
                .role(UserRole.ADMIN)
                .build();
            
            User clienteUser = User.builder()
                .id(UUID.randomUUID())
                .name("Cliente Ejemplo")
                .email("cliente@correo.com")
                .phone("3001234567")
                .password("cliente")
                .role(UserRole.CLIENT)
                .build();
                    
            User repartidorUser = User.builder()
                .id(UUID.randomUUID())
                .name("Repartidor Ejemplo")
                .email("repartidor@correo.com")
                .phone("3007654321")
                .password("repartidor")
                .role(UserRole.DELIVERER)
                .build();
            
            // Guardar los usuarios en el repositorio
            userRepository.save(adminUser);
            userRepository.save(clienteUser);
            userRepository.save(repartidorUser);
            
            System.out.println("Datos de prueba creados exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al crear datos de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Guarda el estado actual de la aplicación.
     */
    public void saveState() {
        try {
            // Actualizar el estado con los datos actuales de los repositorios
            updateState();
            
            // Guardar el estado en el archivo
            Serializer.guardarEstado(appState, APP_STATE_FILE);
            System.out.println("Estado de la aplicación guardado exitosamente.");
        } catch (IOException e) {
            AppUtils.showError("Error al guardar", "No se pudo guardar el estado de la aplicación: " + e.getMessage());
            System.err.println("Error al guardar el estado de la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Actualiza el estado de la aplicación con los datos de los repositorios.
     */
    private void updateState() {
        appState.setUsers(userRepository.getEntitiesAsList());
        appState.setDeliverers(delivererRepository.getEntitiesAsList());
        appState.setShipments(shipmentRepository.getEntitiesAsList());
        appState.setAddresses(addressRepository.getEntitiesAsList());
        appState.setPayments(paymentRepository.getEntitiesAsList());
        appState.setRates(rateRepository.getEntitiesAsList());
        appState.setIncidents(incidentRepository.getEntitiesAsList());
    }
}