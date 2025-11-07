package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

/**
 * Controlador para gestionar el perfil de usuario y sus operaciones
 */
public class UserProfileController {

    private final UserService userService;
    private User currentUser;

    /**
     * Constructor del controlador
     * Inicializa servicios solamente
     */
    public UserProfileController() {
        this.userService = UserService.getInstance();
        
        // Obtendremos el usuario actual a través de un método específico
        // en lugar de obtenerlo en el constructor
        this.currentUser = UserDashboardController.getCurrentUser();
    }

    /**
     * Obtiene los datos del usuario actual
     * @return DTO con los datos no sensibles del usuario
     */
    public UserDTO getCurrentUser() {
        if (currentUser == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(currentUser.getId())
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .phone(currentUser.getPhone())
                .role(currentUser.getRole())
                .build();
    }

    /**
     * Actualiza la información del usuario
     * @param name Nombre nuevo
     * @param email Email nuevo
     * @param phone Teléfono nuevo
     * @return true si la operación fue exitosa
     */
    public boolean updateUserInfo(String name, String email, String phone) {
        if (currentUser == null) {
            return false;
        }
        
        try {
            // Actualizar los datos del usuario
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            
            // Guardar los cambios
            boolean success = userService.updateUser(currentUser);
            
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cambia la contraseña del usuario
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @return true si la operación fue exitosa
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        if (currentUser == null) {
            return false;
        }
        
        try {
            // Validar la contraseña actual
            if (!userService.validatePassword(currentUser.getId(), currentPassword)) {
                return false;
            }
            
            // Cambiar la contraseña
            return userService.changePassword(currentUser.getId(), newPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}