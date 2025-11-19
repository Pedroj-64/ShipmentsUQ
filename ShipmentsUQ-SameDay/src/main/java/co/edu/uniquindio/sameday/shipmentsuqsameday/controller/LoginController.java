package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.UserRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.DelivererService;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la lógica de negocio relacionada con la autenticación.
 * Maneja tanto usuarios como repartidores de forma unificada.
 * Actúa como intermediario entre la vista de login y los servicios.
 */
public class LoginController {
    
    private final UserService userService;
    private final DelivererService delivererService;
    
    /**
     * Constructor del controlador de login.
     * Inicializa los servicios necesarios.
     */
    public LoginController() {
        this.userService = UserService.getInstance();
        this.delivererService = DelivererService.getInstance();
    }
    
    /**
     * Autentica a un usuario verificando sus credenciales.
     * 
     * @param email El correo electrónico del usuario
     * @param password La contraseña del usuario
     * @return El objeto User si la autenticación es exitosa, null si falla
     */
    public User authenticateUser(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("El correo y la contraseña no pueden estar vacíos");
        }
        
        email = email.trim().toLowerCase();
        
        return userService.authenticate(email, password)
            .orElse(null); // Convertir Optional<User> a User o null
    }
    
    /**
     * Autentica a un repartidor verificando sus credenciales.
     * Nota: La contraseña del repartidor es su teléfono.
     * 
     * @param document El documento del repartidor
     * @param password La contraseña del repartidor (teléfono)
     * @return El objeto Deliverer si la autenticación es exitosa, null si falla
     */
    public Deliverer authenticateDeliverer(String document, String password) {
        if (document == null || password == null || document.trim().isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("El documento y la contraseña no pueden estar vacíos");
        }
        
        document = document.trim();
        
        // Buscar repartidor por documento
        Optional<Deliverer> delivererOpt = delivererService.getRepository().findByDocument(document);
        
        // Verificar contraseña (el teléfono es la contraseña)
        if (delivererOpt.isPresent() && delivererOpt.get().getPhone().equals(password)) {
            return delivererOpt.get();
        }
        
        return null;
    }
    
    /**
     * Intenta autenticar como usuario o repartidor según el identificador proporcionado.
     * Si el identificador es un email, intenta autenticar como usuario.
     * Si no es email, intenta autenticar como repartidor usando documento.
     * 
     * @param identifier Email o documento
     * @param password Contraseña
     * @return Un objeto Object que puede ser User o Deliverer, o null si falla
     */
    public Object authenticateAny(String identifier, String password) {
        if (identifier == null || password == null || identifier.trim().isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("El identificador y la contraseña no pueden estar vacíos");
        }
        
        identifier = identifier.trim();
        
        // Detectar si es email (contiene @)
        if (identifier.contains("@")) {
            // Intentar autenticar como usuario
            return authenticateUser(identifier, password);
        } else {
            // Intentar autenticar como repartidor
            return authenticateDeliverer(identifier, password);
        }
    }
    
    /**
     * Recupera la contraseña del usuario por email.
     * 
     * @param email El correo electrónico del usuario que olvidó su contraseña
     * @return La contraseña del usuario o null si el usuario no existe
     */
    public String recoverPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo no puede estar vacío");
        }
        
        email = email.trim().toLowerCase();
        
        return userService.findByEmail(email)
            .map(User::getPassword)
            .orElse(null);
    }
    
    /**
     * Busca a qué correo pertenece una contraseña dada.
     * Útil para el sistema de recuperación de contraseña.
     * 
     * @param password La contraseña a buscar
     * @return El correo electrónico del usuario que tiene esa contraseña, o null si no se encuentra
     */
    public String findEmailByPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        // Buscar en todos los usuarios
        UserRepository repo = userService.getRepository();
        List<User> allUsers = repo.findAll();
        
        // Buscar el primer usuario que tenga esa contraseña
        return allUsers.stream()
            .filter(user -> password.equals(user.getPassword()))
            .map(User::getEmail)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Método de depuración para mostrar todos los usuarios disponibles en el sistema.
     * Este método es útil para diagnosticar problemas de login.
     */
    public void showAllUsersForDebug() {
        try {
            System.out.println("DEBUG: Verificando estado de UserService y repositorio...");
            
            UserRepository repo = userService.getRepository();
            System.out.println("DEBUG: Repositorio de UserService: " + (repo != null ? "disponible" : "null"));
            
            List<User> allUsers = repo.findAll();
            System.out.println("Total de usuarios disponibles: " + allUsers.size());
            
            if (allUsers.isEmpty()) {
                System.out.println("No hay usuarios disponibles en el sistema.");
            } else {
                System.out.println("Lista de usuarios disponibles:");
                allUsers.forEach(user -> {
                    System.out.println("ID: " + user.getId() + 
                                       " | Nombre: " + user.getName() + 
                                       " | Email: " + user.getEmail() + 
                                       " | Rol: " + user.getRole());
                });
            }
            
            // Mostrar repartidores disponibles
            System.out.println("\n=== REPARTIDORES DISPONIBLES ===");
            List<Deliverer> allDeliverers = delivererService.getRepository().findAll();
            System.out.println("Total de repartidores disponibles: " + allDeliverers.size());
            
            if (allDeliverers.isEmpty()) {
                System.out.println("No hay repartidores disponibles en el sistema.");
            } else {
                System.out.println("Lista de repartidores disponibles:");
                allDeliverers.forEach(deliverer -> {
                    System.out.println("ID: " + deliverer.getId() + 
                                       " | Nombre: " + deliverer.getName() + 
                                       " | Documento: " + deliverer.getDocument() + 
                                       " | Teléfono (Password): " + deliverer.getPhone() + 
                                       " | Estado: " + deliverer.getStatus() + 
                                       " | Zona: " + deliverer.getZone());
                });
            }
            
        } catch (Exception e) {
            System.err.println("ERROR al obtener usuarios/repartidores para debug: " + e.getMessage());
            e.printStackTrace();
        }
    }
}