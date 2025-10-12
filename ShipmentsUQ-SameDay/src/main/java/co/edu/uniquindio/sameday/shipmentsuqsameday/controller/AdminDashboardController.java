package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

/**
 * Controlador para el Panel de Administración
 * Se encarga de la lógica de negocio relacionada con la navegación
 * y funcionalidades del panel administrativo
 */
public class AdminDashboardController {
    
    // Referencia al servicio de usuarios
    private UserService userService;
    
    // Usuario administrador actual
    private User adminUser;
    
    /**
     * Constructor del controlador
     */
    public AdminDashboardController() {
        // Inicializar servicios
        userService = UserService.getInstance();
    }
    
    /**
     * Establece el usuario administrador actual
     * @param user usuario administrador
     */
    public void setAdminUser(User user) {
        this.adminUser = user;
    }
    
    /**
     * Obtiene el usuario administrador actual
     * @return usuario administrador
     */
    public User getAdminUser() {
        return adminUser;
    }
    
    /**
     * Cierra la sesión del usuario administrador y regresa a la pantalla de inicio de sesión
     */
    public void logout() {
        // Utilizar el método de reinicio de la aplicación
        App.restartApp();
    }
    
    /**
     * Actualiza los datos del perfil de administrador
     * @param name nuevo nombre
     * @param email nuevo correo
     * @param phone nuevo teléfono
     * @return true si la actualización fue exitosa
     */
    public boolean updateProfile(String name, String email, String phone) {
        try {
            // Verificar que el usuario administrador esté establecido
            if (adminUser == null) {
                return false;
            }
            
            // Actualizar los campos
            adminUser.setName(name);
            adminUser.setEmail(email);
            adminUser.setPhone(phone);
            
            // Guardar los cambios
            userService.update(adminUser);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cambia la contraseña del administrador
     * @param currentPassword contraseña actual
     * @param newPassword nueva contraseña
     * @return true si el cambio fue exitoso
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        try {
            // Verificar que el usuario administrador esté establecido
            if (adminUser == null) {
                return false;
            }
            
            // Verificar la contraseña actual (en un sistema real, utilizarías encriptación)
            if (!currentPassword.equals(adminUser.getPassword())) {
                return false;
            }
            
            // Establecer la nueva contraseña
            adminUser.setPassword(newPassword);
            
            // Guardar los cambios
            userService.update(adminUser);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
