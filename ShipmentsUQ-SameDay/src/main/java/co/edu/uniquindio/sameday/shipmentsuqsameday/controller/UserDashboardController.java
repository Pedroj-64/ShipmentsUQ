package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;

import java.time.LocalDateTime;

/**
 * Controlador de negocio para el dashboard del usuario.
 * Maneja la lógica relacionada con las funcionalidades del panel de control.
 */
public class UserDashboardController {

    private static User currentUser;

    /**
     * Constructor del controlador de dashboard
     */
    public UserDashboardController() {
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

            System.out.println("[" + LocalDateTime.now() + "] Usuario " +
                currentUser.getName() + " accedió al módulo: " + moduleName);
        }
    }

    /**
     * Procesa el cierre de sesión
     */
    public void logout() {

        System.out.println("[" + LocalDateTime.now() + "] Usuario " +
            (currentUser != null ? currentUser.getName() : "desconocido") + " cerró sesión");

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

        return (int) currentUser.getShipmentHistory().stream()
            .filter(s -> !s.getStatus().toString().equals("DELIVERED") &&
                         !s.getStatus().toString().equals("CANCELLED"))
            .count();
    }
}
