package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.UserRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;
import java.util.List;

/**
 * Controlador para la lógica de negocio relacionada con la autenticación de usuarios.
 * Actúa como intermediario entre la vista de login y los servicios de usuario.
 */
public class LoginController {
    
    private final UserService userService;
    
    /**
     * Constructor del controlador de login.
     * Inicializa los servicios necesarios.
     */
    public LoginController() {
        this.userService = UserService.getInstance();
    }
    
    /**
     * Autentica a un usuario verificando sus credenciales.
     * 
     * @param email El correo electrónico del usuario
     * @param password La contraseña del usuario
     * @return El objeto User si la autenticación es exitosa, null si falla
     */
    public User authenticateUser(String email, String password) {
        // Validaciones básicas
        if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("El correo y la contraseña no pueden estar vacíos");
        }
        
        // Normalizar el email (eliminar espacios y convertir a minúsculas)
        email = email.trim().toLowerCase();
        
        // Intentar autenticar al usuario
        return userService.authenticate(email, password)
            .orElse(null); // Convertir Optional<User> a User o null
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
        
        // Buscar al usuario por email
        return userService.findByEmail(email)
            .map(User::getPassword)
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
        } catch (Exception e) {
            System.err.println("ERROR al obtener usuarios para debug: " + e.getMessage());
            e.printStackTrace();
        }
    }
}