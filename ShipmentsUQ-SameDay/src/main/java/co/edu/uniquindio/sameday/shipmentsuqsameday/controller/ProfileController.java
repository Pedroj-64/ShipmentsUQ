package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

import java.util.Optional;

/**
 * Controlador para la gestión del perfil de usuario.
 * Maneja la lógica relacionada con la actualización y consulta de información personal.
 */
public class ProfileController {

    private final UserService userService;
    
    /**
     * Constructor del controlador de perfil.
     * Inicializa los servicios necesarios.
     */
    public ProfileController() {
        this.userService = UserService.getInstance();
    }
    
    /**
     * Obtiene los datos del usuario en sesión
     * @return DTO con los datos del usuario
     * @throws IllegalStateException si no hay un usuario en sesión
     */
    public UserDTO getCurrentUser() {
        User currentUser = UserDashboardController.getCurrentUser();
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
     * Obtiene la dirección predeterminada del usuario
     * @return DTO con los datos de la dirección o null si no tiene
     * @throws IllegalStateException si no hay un usuario en sesión
     */
    public AddressDTO getDefaultAddress() {
        User currentUser = UserDashboardController.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("No hay un usuario en sesión");
        }
        
        Optional<Address> defaultAddress = currentUser.getAddresses().stream()
            .filter(Address::isDefault)
            .findFirst();
            
        if (defaultAddress.isEmpty()) {
            return null;
        }
        
        Address address = defaultAddress.get();
        return AddressDTO.builder()
            .id(address.getId())
            .street(address.getStreet())
            .city(address.getCity())
            .zone(address.getZone())
            .coordX(address.getCoordX())
            .coordY(address.getCoordY())
            .build();
    }
    
    /**
     * Actualiza la información personal del usuario
     * @param name Nuevo nombre
     * @param email Nuevo email
     * @param phone Nuevo teléfono
     * @return true si la actualización fue exitosa
     * @throws IllegalStateException si no hay un usuario en sesión
     */
    public boolean updateUserInfo(String name, String email, String phone) {
        User currentUser = UserDashboardController.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("No hay un usuario en sesión");
        }
        
        try {
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            
            userService.getRepository().update(currentUser);
            
            UserDashboardController.setCurrentUser(currentUser);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}