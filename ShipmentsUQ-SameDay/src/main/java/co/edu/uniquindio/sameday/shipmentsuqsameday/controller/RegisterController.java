package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.UserRole;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

import java.util.UUID;

/**
 * Controlador para la lógica de negocio relacionada con el registro de usuarios.
 * Actúa como intermediario entre la vista de registro y los servicios de usuario.
 */
public class RegisterController {
    
    private final UserService userService;
    
    /**
     * Constructor del controlador de registro.
     * Inicializa los servicios necesarios.
     */
    public RegisterController() {
        this.userService = UserService.getInstance();
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
        // Validaciones básicas
        validateUserData(name, email, phone, password, city);
        
        // Verificar si el correo electrónico ya está registrado
        if (userService.findByEmail(email.trim().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }
        
        // Verificar si el teléfono ya está registrado
        if (userService.findByPhone(phone.trim()).isPresent()) {
            throw new IllegalArgumentException("El número de teléfono ya está registrado");
        }
        
        // Crear el nuevo usuario
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .name(name.trim())
                .email(email.trim().toLowerCase())
                .phone(phone.trim())
                .password(password)
                .role(UserRole.CLIENT) // Por defecto, todos los nuevos usuarios son clientes
                .build();
        
        // Guardar el usuario en el repositorio
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
        // Validar que los campos no estén vacíos
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
        
        // Validar formato de correo electrónico (básico)
        if (!email.trim().contains("@") || !email.trim().contains(".")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido");
        }
        
        // Validar longitud mínima de la contraseña
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
    }
}