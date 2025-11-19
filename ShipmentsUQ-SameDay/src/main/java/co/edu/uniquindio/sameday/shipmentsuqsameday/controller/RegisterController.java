package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.DelivererStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.DelivererService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Controlador para la lógica de negocio relacionada con el registro de usuarios.
 * Actúa como intermediario entre la vista de registro y los servicios de usuario.
 */
public class RegisterController {
    
    private final UserService userService;
    private final DelivererService delivererService;
    
    /**
     * Constructor del controlador de registro.
     * Inicializa los servicios necesarios.
     */
    public RegisterController() {
        this.userService = UserService.getInstance();
        this.delivererService = DelivererService.getInstance();
    }
    
    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * @param name Nombre completo del usuario
     * @param email Correo electrónico del usuario
     * @param phone Número de teléfono del usuario
     * @param password Contraseña del usuario
     * @param city Ciudad de residencia del usuario
     * @return El usuario creado si el registro es exitoso
     * @throws IllegalArgumentException Si los datos son inválidos o el usuario ya existe
     */
    public User registerUser(String name, String email, String phone, String password, String city) {
        validateUserData(name, email, phone, password, city);
        
        if (userService.findByEmail(email.trim().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }
        
        if (userService.findByPhone(phone.trim()).isPresent()) {
            throw new IllegalArgumentException("El número de teléfono ya está registrado");
        }
        
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .name(name.trim())
                .email(email.trim().toLowerCase())
                .phone(phone.trim())
                .password(password)
                .role(UserRole.CLIENT)
                .build();
        
        return userService.getRepository().save(newUser);
    }
    
    /**
     * Valida los datos del usuario antes de registrarlo.
     * 
     * @param name Nombre completo del usuario
     * @param email Correo electrónico del usuario
     * @param phone Número de teléfono del usuario
     * @param password Contraseña del usuario
     * @param city Ciudad de residencia del usuario
     * @throws IllegalArgumentException Si algún dato es inválido
     */
    private void validateUserData(String name, String email, String phone, String password, String city) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("La ciudad no puede estar vacía");
        }
        
        if (!email.trim().contains("@") || !email.trim().contains(".")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido");
        }
        
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
    }
    
    /**
     * Registra un nuevo repartidor en el sistema.
     * 
     * @param name Nombre completo del repartidor
     * @param email Correo electrónico del repartidor
     * @param phone Número de teléfono del repartidor
     * @param password Contraseña del repartidor (se usa el teléfono como contraseña)
     * @param city Ciudad de residencia del repartidor
     * @param document Número de documento del repartidor
     * @param zone Zona de trabajo del repartidor
     * @return El repartidor creado si el registro es exitoso
     * @throws IllegalArgumentException Si los datos son inválidos o el repartidor ya existe
     */
    public Deliverer registerDeliverer(String name, String email, String phone, String password, 
                                       String city, String document, String zone) {
        // Validar datos comunes
        validateUserData(name, email, phone, password, city);
        
        // Validar datos específicos de repartidor
        validateDelivererData(document, zone);
        
        // Verificar si el documento ya está registrado
        if (delivererService.getRepository().findByDocument(document.trim()).isPresent()) {
            throw new IllegalArgumentException("El documento ya está registrado");
        }
        
        // Verificar si el teléfono ya está registrado
        if (delivererService.getRepository().findAll().stream()
                .anyMatch(d -> d.getPhone().equals(phone.trim()))) {
            throw new IllegalArgumentException("El número de teléfono ya está registrado");
        }
        
        // Crear nuevo repartidor
        Deliverer newDeliverer = Deliverer.builder()
                .id(UUID.randomUUID())
                .name(name.trim())
                .phone(phone.trim())
                .document(document.trim())
                .zone(zone.trim())
                .status(DelivererStatus.AVAILABLE)
                .currentShipments(new ArrayList<>())
                .shipmentHistory(new ArrayList<>())
                .totalDeliveries(0)
                .averageRating(0.0)
                .currentX(4.533889) // Coordenadas por defecto Armenia, Quindío
                .currentY(-75.681111)
                .realLatitude(4.533889)
                .realLongitude(-75.681111)
                .build();
        
        return delivererService.getRepository().save(newDeliverer);
    }
    
    /**
     * Valida los datos específicos del repartidor.
     * 
     * @param document Número de documento del repartidor
     * @param zone Zona de trabajo del repartidor
     * @throws IllegalArgumentException Si algún dato es inválido
     */
    private void validateDelivererData(String document, String zone) {
        if (document == null || document.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento no puede estar vacío");
        }
        
        if (zone == null || zone.trim().isEmpty()) {
            throw new IllegalArgumentException("La zona de trabajo no puede estar vacía");
        }
        
        if (document.trim().length() < 6) {
            throw new IllegalArgumentException("El documento debe tener al menos 6 caracteres");
        }
    }
}