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

    private static UserService instance;
    
    // Constructor privado para Singleton
    private UserService(UserRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Obtiene la instancia única del servicio con el repositorio proporcionado
     * @param repository El repositorio de usuarios a utilizar
     * @return instancia del servicio
     */
    public static synchronized UserService getInstance(UserRepository repository) {
        if (instance == null) {
            instance = new UserService(repository);
        }
        return instance;
    }
    
    /**
     * Obtiene la instancia única del servicio
     * Este método es para compatibilidad con código existente.
     * Debería llamarse primero getInstance(UserRepository) para inicializar el servicio.
     * @return instancia del servicio
     */
    public static UserService getInstance() {
        if (instance == null) {
            System.err.println("ERROR: UserService no ha sido inicializado con un repositorio");
            throw new IllegalStateException("UserService no ha sido inicializado con un repositorio");
        }
        return instance;
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
        System.out.println("Intentando autenticar usuario con email: " + email);
        System.out.println("Estado del repositorio de usuarios: " + (repository != null ? "disponible" : "null"));
        
        if (repository == null) {
            System.err.println("ERROR: El repositorio de usuarios es nulo. No se puede autenticar.");
            return Optional.empty();
        }
        
        System.out.println("Usuarios totales en repositorio: " + repository.findAll().size());
        
        Optional<User> userOpt = findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Usuario encontrado: " + user.getName() + ", verificando contraseña...");
            
            if (user.getPassword().equals(password)) {
                System.out.println("Autenticación exitosa para: " + email);
                return userOpt;
            } else {
                System.out.println("Contraseña incorrecta para: " + email);
            }
        } else {
            System.out.println("No se encontró usuario con email: " + email);
            // Listar todos los usuarios disponibles para depuración
            System.out.println("Usuarios disponibles:");
            List<User> allUsers = repository.findAll();
            
            if (allUsers.isEmpty()) {
                System.out.println("- No hay usuarios en el repositorio");
            } else {
                allUsers.forEach(u -> {
                    System.out.println("- " + u.getEmail() + " (ID: " + u.getId() + ")");
                });
            }
        }
        
        return Optional.empty();
    }

    /**
     * Valida si la contraseña proporcionada coincide con la almacenada para el usuario
     * @param userId ID del usuario
     * @param password contraseña a validar
     * @return true si la contraseña es válida, false en caso contrario
     */
    public boolean validatePassword(UUID userId, String password) {
        Optional<User> userOpt = repository.findById(userId);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getPassword().equals(password);
        }
        
        return false;
    }
    
    /**
     * Cambia la contraseña de un usuario
     * @param userId ID del usuario
     * @param newPassword nueva contraseña
     * @return true si se cambió correctamente, false en caso contrario
     */
    public boolean changePassword(UUID userId, String newPassword) {
        try {
            Optional<User> userOpt = repository.findById(userId);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(newPassword);
                repository.update(user);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Actualiza la información de un usuario
     * @param user usuario con la información actualizada
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean updateUser(User user) {
        try {
            repository.update(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
