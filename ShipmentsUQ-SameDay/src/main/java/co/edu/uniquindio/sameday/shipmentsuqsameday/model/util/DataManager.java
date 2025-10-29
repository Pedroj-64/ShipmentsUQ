package co.edu.uniquindio.sameday.shipmentsuqsameday.model.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.AppUtils;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.UserPaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.mapping.DataInitializer;
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

    // Getters para los repositorios
    public UserRepository getUserRepository() {
        return userRepository;
    }

    public DelivererRepository getDelivererRepository() {
        return delivererRepository;
    }

    public ShipmentRepository getShipmentRepository() {
        return shipmentRepository;
    }

    public AddressRepository getAddressRepository() {
        return addressRepository;
    }

    public PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }

    public RateRepository getRateRepository() {
        return rateRepository;
    }

    public IncidentRepository getIncidentRepository() {
        return incidentRepository;
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
        
        // Inicializar servicios con los repositorios correctos
        try {
            // Inicializar UserService con nuestro repositorio de usuarios
            co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService.getInstance(userRepository);
            System.out.println("Servicios inicializados correctamente con los repositorios");
        } catch (Exception e) {
            System.err.println("Error al inicializar servicios: " + e.getMessage());
            e.printStackTrace();
        }
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
                
                // Verificar si los datos se cargaron correctamente
                if (appState.getUsers() == null || appState.getUsers().isEmpty()) {
                    System.out.println("ADVERTENCIA: No se encontraron datos en el estado cargado. Inicializando datos por defecto.");
                    // Inicializar datos por defecto
                    DataInitializer.initializeUsers(userRepository);
                    DataInitializer.initializeDefaultDeliverer(delivererRepository);
                    // Eliminar el archivo corrupto
                    Serializer.eliminarArchivo(APP_STATE_FILE);
                    
                    // Crear un nuevo estado
                    appState = new AppState();
                    
                    // Inicializar datos de prueba
                    initializeTestData();
                } else {
                    System.out.println("Usuarios encontrados en el estado cargado: " + appState.getUsers().size());
                    // Cargar datos en los repositorios
                    loadRepositories();
                }
            } else {
                // Crear un nuevo estado
                appState = new AppState();
                System.out.println("Se ha creado un nuevo estado para la aplicación.");
                
                // Inicializar datos de prueba
                initializeTestData();
            }
        } catch (Exception e) {
            // Si ocurre un error al cargar, crear un nuevo estado
            appState = new AppState();
            System.err.println("Error al cargar el estado de la aplicación: " + e.getMessage());
            e.printStackTrace();
            
            // Eliminar archivo corrupto si existe
            Serializer.eliminarArchivo(APP_STATE_FILE);
            
            // Inicializar datos de prueba
            initializeTestData();
        }
    }
    
    /**
     * Carga los datos desde AppState a los repositorios.
     */
    private void loadRepositories() {
        System.out.println("Cargando datos desde AppState a los repositorios...");
        System.out.println("Usuarios en AppState: " + (appState.getUsers() != null ? appState.getUsers().size() : "null"));
        
        if (appState.getUsers() != null && !appState.getUsers().isEmpty()) {
            System.out.println("Detalle de usuarios a cargar:");
            appState.getUsers().forEach(user -> {
                System.out.println("  - ID: " + user.getId() + ", Nombre: " + user.getName() + ", Email: " + user.getEmail());
            });
        }
        
        userRepository.loadEntities(appState.getUsers());
        delivererRepository.loadEntities(appState.getDeliverers());
        shipmentRepository.loadEntities(appState.getShipments());
        addressRepository.loadEntities(appState.getAddresses());
        paymentRepository.loadEntities(appState.getPayments());
        rateRepository.loadEntities(appState.getRates());
        incidentRepository.loadEntities(appState.getIncidents());
        
        System.out.println("Después de cargar - Usuarios en repositorio: " + userRepository.findAll().size());
    }
    
    /**
     * Inicializa datos de prueba para la aplicación.
     * Este método se llama cuando no hay datos previos guardados.
     */
    private void initializeTestData() {
        System.out.println("Inicializando datos de prueba...");
        
        try {
            // Inicializar usuarios y repartidores
            DataInitializer.initializeUsers(userRepository);
            DataInitializer.initializeDefaultDeliverer(delivererRepository);
            
            // Usar el DataInitializer para crear otros datos de prueba
                DataInitializer.initializeAllTestData(
                userRepository, 
                addressRepository,
                delivererRepository
            );
            
            // Actualizar el estado con los datos recién creados
            updateState();
            
            // Guardar los datos inmediatamente después de crearlos
            saveState();
            
            System.out.println("Datos de prueba creados y guardados exitosamente.");
            
            // Imprimir usuarios para depuración
            System.out.println("Usuarios disponibles:");
            userRepository.findAll().forEach(user -> {
                System.out.println("Usuario: " + user.getName() + ", Email: " + user.getEmail() + ", Rol: " + user.getRole());
            });
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
            
            // Verificar que hay datos para guardar
            if (appState.getUsers() == null || appState.getUsers().isEmpty()) {
                System.err.println("ADVERTENCIA: No hay usuarios para guardar en el estado. No se guardará un estado vacío.");
                return;
            }
            
            // Guardar el estado en el archivo
            Serializer.guardarEstado(appState, APP_STATE_FILE);
            System.out.println("Estado de la aplicación guardado exitosamente.");
            
            // Verificar que el archivo se guardó correctamente
            if (Serializer.existeArchivo(APP_STATE_FILE)) {
                System.out.println("Archivo de estado verificado: existe en disco.");
            } else {
                System.err.println("ERROR: El archivo de estado no se guardó correctamente.");
            }
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
        // Guardar todos los datos en el estado
        List<User> userList = userRepository.getEntitiesAsList();
        System.out.println("Actualizando estado - Total de usuarios a guardar: " + userList.size());
        
        appState.setUsers(userList);
        appState.setDeliverers(delivererRepository.getEntitiesAsList());
        appState.setShipments(shipmentRepository.getEntitiesAsList());
        appState.setAddresses(addressRepository.getEntitiesAsList());
        appState.setPayments(paymentRepository.getEntitiesAsList());
        appState.setRates(rateRepository.getEntitiesAsList());
        appState.setIncidents(incidentRepository.getEntitiesAsList());
        
        // Extraer los métodos de pago de todos los usuarios
        List<UserPaymentMethod> allPaymentMethods = new ArrayList<>();
        for (User user : userList) {
            allPaymentMethods.addAll(user.getPaymentMethods());
        }
        appState.setPaymentMethods(allPaymentMethods);
        
        // Verificación final
        if (appState.getUsers().isEmpty()) {
            System.err.println("ADVERTENCIA: La lista de usuarios está vacía después de updateState()");
        }
    }
}