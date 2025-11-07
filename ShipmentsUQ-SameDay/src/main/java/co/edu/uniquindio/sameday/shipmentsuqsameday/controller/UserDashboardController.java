package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

import java.time.LocalDateTime;

/**
 * Controlador de negocio para el dashboard del usuario.
 * Maneja la lógica relacionada con las funcionalidades del panel de control.
 */
public class UserDashboardController {
    
    // Servicio de usuario (lo usaremos más adelante para otras funcionalidades)
    private final UserService userService;
    
    // Variable estática para almacenar el usuario en sesión
    private static User currentUser;
    
    /**
     * Constructor del controlador de dashboard.
     * Inicializa los servicios necesarios.
     */
    public UserDashboardController() {
        this.userService = UserService.getInstance();
    }
    
    /**
     * Establece el usuario actual de la sesión
     * @param user Usuario actual
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    /**
     * Obtiene el usuario actual de la sesión
     * @return Usuario actual o null si no hay sesión
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Obtiene los datos del usuario actual en formato DTO
     * @return DTO con los datos del usuario
     * @throws IllegalStateException si no hay un usuario en sesión
     */
    public UserDTO getCurrentUserData() {
        if (currentUser == null) {
            throw new IllegalStateException("No hay un usuario en sesión");
        }
        
        // Convertir el modelo User a UserDTO para la vista
        return UserDTO.builder()
            .id(currentUser.getId())
            .name(currentUser.getName())
            .email(currentUser.getEmail())
            .phone(currentUser.getPhone())
            .role(currentUser.getRole())
            .build();
    }
    
    /**
     * Registra el acceso a un módulo específico
     * @param moduleName Nombre del módulo accedido
     */
    public void logModuleAccess(String moduleName) {
        if (currentUser != null) {
            // Aquí podría registrarse en un log o en una tabla de auditoría
            System.out.println("[" + LocalDateTime.now() + "] Usuario " + 
                currentUser.getName() + " accedió al módulo: " + moduleName);
        }
    }
    
    /**
     * Procesa el cierre de sesión
     */
    public void logout() {
        // Aquí podría registrar la hora de cierre de sesión o limpiar datos temporales
        System.out.println("[" + LocalDateTime.now() + "] Usuario " + 
            (currentUser != null ? currentUser.getName() : "desconocido") + " cerró sesión");
        
        // Limpiar referencia al usuario actual
        currentUser = null;
    }
    
    /**
     * Obtiene el número de envíos activos del usuario
     * @return Número de envíos activos
     */
    public int getActiveShipmentsCount() {
        if (currentUser == null) {
            return 0;
        }
        
        // Contar los envíos activos (esto podría implementarse mejor con un servicio específico)
        return (int) currentUser.getShipmentHistory().stream()
            .filter(s -> !s.getStatus().toString().equals("DELIVERED") && 
                         !s.getStatus().toString().equals("CANCELLED"))
            .count();
    }
}
