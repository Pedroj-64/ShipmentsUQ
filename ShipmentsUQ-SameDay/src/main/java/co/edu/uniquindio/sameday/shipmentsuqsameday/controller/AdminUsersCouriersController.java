package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.GridMapViewController;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.DelivererRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.UserRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.DelivererService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * Controlador para la gestión de usuarios y repartidores
 * Se encarga de la lógica de negocio relacionada con el registro,
 * edición y eliminación de usuarios y repartidores
 */
public class AdminUsersCouriersController implements Initializable {

    // Referencia al View Controller para interacciones con la UI
    private viewController viewController;

    // Colecciones para almacenar usuarios y repartidores
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private ObservableList<Deliverer> deliverersList = FXCollections.observableArrayList();

    // Servicios para acceso a datos
    private UserService userService;
    private DelivererService delivererService;

    // Modo actual: "users" o "couriers"
    private String currentMode = "users";

    // Controlador para el mapa de zonas
    private GridMapViewController gridMapController;
    
    /**
     * Obtiene el modo actual de gestión
     * @return "users" o "couriers"
     */
    public String getMode() {
        return currentMode;
    }

    /**
     * Inicializa el controlador y configura las colecciones de datos
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Esta inicialización se completa cuando se establece el ViewController
    }

    /**
     * Establece el ViewController asociado y completa la inicialización
     * @param viewController la referencia al ViewController
     */
    public void setViewController(viewController viewController) {
        this.viewController = viewController;
        
        // Inicializar datos (en una aplicación real, estos datos vendrían de un repositorio/servicio)
        loadMockData();
    }

    /**
     * Inicializa el controlador del mapa de zonas
     * @param mapContainer el contenedor para el mapa
     */
    public GridMapViewController getGridMapController() {
        return gridMapController;
    }

    public void initializeGridMap(Pane mapContainer) {
        gridMapController = new GridMapViewController(
            mapContainer.getPrefWidth(), 
            mapContainer.getPrefHeight(), 
            20); // Tamaño de celda: 20px
        
        gridMapController.initialize(mapContainer);
        
        // Configurar listener para la selección de coordenadas en el mapa
        gridMapController.setCoordinateListener((x, y) -> {
            // Cuando se selecciona una coordenada en el mapa, actualizar los campos
            viewController.updateZoneCoordinates(x, y);
        });
    }

    /**
     * Cambia el modo de visualización entre usuarios y repartidores
     * @param mode el modo a establecer ("users" o "couriers")
     */
    public void setMode(String mode) {
        this.currentMode = mode;
        
        // Actualizar la interfaz según el modo seleccionado
        if (viewController != null) {
            viewController.updateUIForMode(mode);
            
            // Actualizar la tabla con los datos correspondientes
            if ("users".equals(mode)) {
                viewController.loadTableData(usersList);
            } else {
                viewController.loadTableData(deliverersList);
            }
        }
    }

    /**
     * Carga datos desde los servicios
     */
    private void loadMockData() {
        try {
            // Obtener los repositorios del DataManager
            UserRepository userRepository = co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.DataManager.getInstance().getUserRepository();
            DelivererRepository delivererRepository = co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.DataManager.getInstance().getDelivererRepository();

            // Inicializar los servicios con sus repositorios
            userService = UserService.getInstance(userRepository);
            delivererService = DelivererService.getInstance(delivererRepository);
            
            // Cargar usuarios desde el servicio
            usersList.clear();
            usersList.addAll(userService.findAll());
            
            // Si no hay usuarios en el servicio (primera ejecución), mostrar mensaje
            if (usersList.isEmpty()) {
                System.out.println("No se encontraron usuarios en el repositorio");
            }
            
            // Cargar repartidores desde el servicio
            deliverersList.clear();
            deliverersList.addAll(delivererService.findAll());
            
            // Si no hay repartidores en el servicio (primera ejecución), mostrar mensaje
            if (deliverersList.isEmpty()) {
                System.out.println("No se encontraron repartidores en el repositorio");
            }
            
            // Si el ViewController está configurado, cargar los datos en la tabla
            if (viewController != null) {
                if ("users".equals(currentMode)) {
                    viewController.loadTableData(usersList);
                } else {
                    viewController.loadTableData(deliverersList);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar datos: " + e.getMessage());
            e.printStackTrace();
            
            // Notificar al usuario sobre el problema
            if (viewController != null) {
                viewController.showStatusMessage("Error al cargar datos: " + e.getMessage(), "error");
            }
        }
    }

    /**
     * Agrega un nuevo usuario
     * @param name nombre
     * @param email correo electrónico
     * @param phone teléfono
     * @return true si la operación fue exitosa
     */
    public boolean addUser(String name, String email, String phone) {
        try {
            // Verificar si ya existe un usuario con el mismo email o teléfono
            if (userService.findByEmail(email).isPresent()) {
                if (viewController != null) {
                    viewController.showStatusMessage("Ya existe un usuario con ese correo", "warning");
                }
                return false;
            }
            
            // Crear el usuario
            User newUser = User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .phone(phone)
                .role(UserRole.CLIENT)
                .build();
                
            // Guardar en el servicio
            userService.create(newUser);
                
            // Actualizar la lista observable
            usersList.add(newUser);
            return true;
        } catch (Exception e) {
            System.err.println("Error al agregar usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Agrega un nuevo repartidor
     * @param name nombre
     * @param document documento de identidad
     * @param phone teléfono
     * @param zone zona asignada
     * @param coordX coordenada X
     * @param coordY coordenada Y
     * @return true si la operación fue exitosa
     */
    public boolean addDeliverer(String name, String document, String phone, String zone, double coordX, double coordY) {
        try {
            // Verificar si ya existe un repartidor con el mismo documento
            // En una implementación real, deberíamos usar un método del servicio que verifique esto
            boolean existeRepartidor = deliverersList.stream()
                .anyMatch(d -> d.getDocument().equals(document));
                
            if (existeRepartidor) {
                if (viewController != null) {
                    viewController.showStatusMessage("Ya existe un repartidor con ese documento", "warning");
                }
                return false;
            }
            
            // Crear el repartidor
            Deliverer newDeliverer = Deliverer.builder()
                .id(UUID.randomUUID())
                .name(name)
                .document(document)
                .phone(phone)
                .status(DelivererStatus.AVAILABLE)
                .zone(zone)
                .currentX(coordX)
                .currentY(coordY)
                .build();
                
            // Guardar en el servicio
            delivererService.create(newDeliverer);
                
            // Actualizar la lista observable
            deliverersList.add(newDeliverer);
            return true;
        } catch (Exception e) {
            System.err.println("Error al agregar repartidor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Edita un usuario existente
     * @param selectedUser usuario seleccionado
     * @param name nuevo nombre
     * @param email nuevo correo
     * @param phone nuevo teléfono
     * @return true si la operación fue exitosa
     */
    public boolean editUser(User selectedUser, String name, String email, String phone) {
        try {
            selectedUser.setName(name);
            selectedUser.setEmail(email);
            selectedUser.setPhone(phone);
            
            // Actualizar la lista para reflejar los cambios
            int index = usersList.indexOf(selectedUser);
            if (index >= 0) {
                usersList.set(index, selectedUser);
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Edita un repartidor existente
     * @param selectedDeliverer repartidor seleccionado
     * @param name nuevo nombre
     * @param document nuevo documento
     * @param phone nuevo teléfono
     * @param zone nueva zona
     * @param status nuevo estado
     * @param coordX nueva coordenada X
     * @param coordY nueva coordenada Y
     * @return true si la operación fue exitosa
     */
    public boolean editDeliverer(Deliverer selectedDeliverer, String name, String document, 
                               String phone, String zone, DelivererStatus status,
                               double coordX, double coordY) {
        try {
            selectedDeliverer.setName(name);
            selectedDeliverer.setDocument(document);
            selectedDeliverer.setPhone(phone);
            selectedDeliverer.setZone(zone);
            selectedDeliverer.setStatus(status);
            selectedDeliverer.updatePosition(coordX, coordY);
            
            // Actualizar la lista para reflejar los cambios
            int index = deliverersList.indexOf(selectedDeliverer);
            if (index >= 0) {
                deliverersList.set(index, selectedDeliverer);
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Elimina un usuario
     * @param user usuario a eliminar
     * @return true si la operación fue exitosa
     */
    public boolean deleteUser(User user) {
        return usersList.remove(user);
    }

    /**
     * Elimina un repartidor
     * @param deliverer repartidor a eliminar
     * @return true si la operación fue exitosa
     */
    public boolean deleteDeliverer(Deliverer deliverer) {
        return deliverersList.remove(deliverer);
    }

    /**
     * Obtiene la lista de usuarios
     * @return lista observable de usuarios
     */
    public ObservableList<User> getUsersList() {
        return usersList;
    }

    /**
     * Obtiene la lista de repartidores
     * @return lista observable de repartidores
     */
    public ObservableList<Deliverer> getDeliverersList() {
        return deliverersList;
    }

    /**
     * Obtiene los datos actuales según el modo
     * @return Lista de usuarios o repartidores según el modo actual
     */
    public List<?> getCurrentData() {
        if ("users".equals(currentMode)) {
            return new ArrayList<>(usersList);
        } else {
            return new ArrayList<>(deliverersList);
        }
    }
    
    /**
     * Interfaz para la comunicación con el ViewController
     */
    public interface viewController {
        void updateUIForMode(String mode);
        void loadTableData(ObservableList<?> data);
        void updateZoneCoordinates(double x, double y);
        void showStatusMessage(String message, String messageType);
    }
}
