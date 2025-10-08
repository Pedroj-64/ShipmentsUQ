package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.UserPaymentMethod;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * Servicio para gestionar la lógica de negocio relacionada con usuarios
 */
public class UserService implements Service<User, UserRepository> {
    private final UserRepository repository;

    // Constructor privado para Singleton
    private UserService() {
        this.repository = new UserRepository();
    }
    
    // Holder estático para instancia única
    private static class SingletonHolder {
        private static final UserService INSTANCE = new UserService();
    }
    
    /**
     * Obtiene la instancia única del servicio
     * @return instancia del servicio
     */
    public static UserService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    /**
     * Registra un nuevo usuario en el sistema
     * @param name nombre completo del usuario
     * @param password contraseña
     * @param email correo electrónico
     * @param phone teléfono
     * @param role rol del usuario (ADMIN, CLIENT, DELIVERER)
     * @return el usuario creado
     */
    public User registerUser(String name, String password, String email, String phone, UserRole role) {
        
        User newUser = User.builder()
            .name(name)
            .password(password)
            .email(email)
            .phone(phone)
            .role(role)
            .build();
            
        return repository.save(newUser);
    }

    @Override
    public UserRepository getRepository() {
        return repository;
    }

    /**
     * Agrega una dirección al usuario
     * @param userId ID del usuario
     * @param address dirección a agregar
     * @return usuario actualizado
     * @throws IllegalArgumentException si el usuario no existe
     */
    public User addAddress(UUID userId, Address address) {
        User user = repository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        user.getAddresses().add(address);
        return repository.update(user);
    }

    /**
     * Establece una dirección como predeterminada
     * @param userId ID del usuario
     * @param addressId ID de la dirección
     * @return usuario actualizado
     * @throws IllegalArgumentException si el usuario o la dirección no existen
     */
    public User setDefaultAddress(UUID userId, UUID addressId) {
        User user = repository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
        Address address = user.getAddresses().stream()
            .filter(a -> a.getId().equals(addressId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada"));
            
        user.getAddresses().forEach(a -> a.setDefault(false));
        address.setDefault(true);
        
        return repository.update(user);
    }

    /**
     * Obtiene la dirección predeterminada del usuario
     * @param userId ID del usuario
     * @return dirección predeterminada o null si no existe
     * @throws IllegalArgumentException si el usuario no existe
     */
    public Address getDefaultAddress(UUID userId) {
        User user = repository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
        return user.getAddresses().stream()
            .filter(Address::isDefault)
            .findFirst()
            .orElse(null);
    }

    /**
     * Agrega un método de pago al usuario
     * @param userId ID del usuario
     * @param paymentMethod método de pago a agregar
     * @return usuario actualizado
     * @throws IllegalArgumentException si el usuario no existe
     */
    public User addPaymentMethod(UUID userId, UserPaymentMethod paymentMethod) {
        User user = repository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
        user.getPaymentMethods().add(paymentMethod);
        return repository.update(user);
    }

    /**
     * Establece un método de pago como predeterminado
     * @param userId ID del usuario
     * @param paymentMethodId ID del método de pago
     * @return usuario actualizado
     * @throws IllegalArgumentException si el usuario o el método de pago no existen
     */
    public User setDefaultPaymentMethod(UUID userId, UUID paymentMethodId) {
        User user = repository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
        UserPaymentMethod paymentMethod = user.getPaymentMethods().stream()
            .filter(pm -> pm.getId().equals(paymentMethodId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado"));
            
        user.getPaymentMethods().forEach(pm -> pm.setDefault(false));
        paymentMethod.setDefault(true);
        
        return repository.update(user);
    }

    /**
     * Obtiene el método de pago predeterminado del usuario
     * @param userId ID del usuario
     * @return método de pago predeterminado o null si no existe
     * @throws IllegalArgumentException si el usuario no existe
     */
    public UserPaymentMethod getDefaultPaymentMethod(UUID userId) {
        User user = repository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
        return user.getPaymentMethods().stream()
            .filter(UserPaymentMethod::isDefault)
            .findFirst()
            .orElse(null);
    }

    /**
     * Agrega un envío al historial del usuario
     * @param userId ID del usuario
     * @param shipment envío a agregar
     * @return usuario actualizado
     * @throws IllegalArgumentException si el usuario no existe
     */
    public User addToShipmentHistory(UUID userId, Shipment shipment) {
        User user = repository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
        user.getShipmentHistory().add(shipment);
        return repository.update(user);
    }

    /**
     * Busca usuarios por zona
     * @param zone zona a buscar
     * @return lista de usuarios en esa zona
     */
    public List<User> findByZone(String zone) {
        return repository.findByZone(zone);
    }

    /**
     * Busca un usuario por su email
     * @param email email del usuario
     * @return usuario encontrado o vacío si no existe
     */
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    /**
     * Busca un usuario por su teléfono
     * @param phone teléfono del usuario
     * @return usuario encontrado o vacío si no existe
     */
    public Optional<User> findByPhone(String phone) {
        return repository.findByPhone(phone);
    }
    
    /**
     * Autentica un usuario con su email y contraseña
     * @param email email del usuario
     * @param password contraseña del usuario
     * @return usuario autenticado o vacío si las credenciales son inválidas
     */
    public Optional<User> authenticate(String email, String password) {
        return findByEmail(email)
            .filter(user -> user.getPassword().equals(password));
    }
}
