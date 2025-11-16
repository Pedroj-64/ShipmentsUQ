package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.UserDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

/**
 * Controlador para la gestión de perfil y direcciones de usuario.
 * Maneja la lógica de negocio relacionada con la actualización de datos 
 * personales y gestión de direcciones frecuentes.
 */
public class ProfileAndAddressesController {

    private UserService userService;
    private UUID currentUserId;

    /**
     * Constructor para el controlador de perfiles y direcciones.
     * Inicializa los servicios necesarios y obtiene el ID del usuario actual.
     */
    public ProfileAndAddressesController() {
        System.out.println("DEBUG: Inicializando ProfileAndAddressesController");
        userService = UserService.getInstance();
        
        if (App.getCurrentSession() != null) {
            currentUserId = App.getCurrentSession().getUserId();
            System.out.println("DEBUG: ID de usuario obtenido de la sesión: " + currentUserId);
        } else {
            System.out.println("DEBUG: App.getCurrentSession() es null");
            
            User dashboardUser = UserDashboardController.getCurrentUser();
            if (dashboardUser != null) {
                currentUserId = dashboardUser.getId();
                System.out.println("DEBUG: ID de usuario obtenido del UserDashboardController: " + currentUserId);
            } else {
                System.out.println("DEBUG: UserDashboardController.getCurrentUser() también es null");
            }
        }
    }
    
    /**
     * Obtiene los datos del usuario actual.
     * @return DTO con los datos del usuario
     * @throws Exception si no hay usuario logeado o ocurre un error al recuperar los datos
     */
    public UserDTO getCurrentUserData() throws Exception {
        System.out.println("DEBUG: Iniciando getCurrentUserData()");
        
        if (App.getCurrentSession() == null) {
            System.out.println("DEBUG: App.getCurrentSession() es null");
        } else {
            System.out.println("DEBUG: App.getCurrentSession() está presente");
            if (App.getCurrentSession().getUserId() == null) {
                System.out.println("DEBUG: App.getCurrentSession().getUserId() es null");
            } else {
                System.out.println("DEBUG: ID de usuario en sesión: " + App.getCurrentSession().getUserId());
                currentUserId = App.getCurrentSession().getUserId();
            }
        }
        
        if (currentUserId == null) {
            User dashboardUser = UserDashboardController.getCurrentUser();
            if (dashboardUser != null) {
                System.out.println("DEBUG: Usando el usuario del UserDashboardController");
                currentUserId = dashboardUser.getId();
            } else {
                System.out.println("DEBUG: No se pudo obtener el usuario (ni de session ni de UserDashboardController)");
                throw new Exception("No hay un usuario con sesión activa.");
            }
        }
        
        System.out.println("DEBUG: Buscando usuario con ID: " + currentUserId);
        
        Optional<User> userOptional = userService.findById(currentUserId);
        if (!userOptional.isPresent()) {
            System.out.println("DEBUG: No se encontró al usuario con ID: " + currentUserId);
            throw new Exception("No se pudo encontrar al usuario en el sistema.");
        }
        
        User user = userOptional.get();
        System.out.println("DEBUG: Usuario encontrado: " + user.getName() + " (Email: " + user.getEmail() + ")");
        
        UserDTO dto = UserDTO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .role(user.getRole())
            .build();
            
        System.out.println("DEBUG: DTO creado correctamente");
        return dto;
    }
    
    /**
     * Actualiza los datos personales del usuario.
     * @param name Nuevo nombre completo del usuario
     * @param email Nuevo correo electrónico
     * @param phone Nuevo número de teléfono
     * @return true si se actualizó correctamente, false en caso contrario
     * @throws Exception si no hay usuario logeado o ocurre un error durante la actualización
     */
    public boolean updateUserProfile(String name, String email, String phone) throws Exception {
        if (currentUserId == null) {
            throw new Exception("No hay un usuario con sesión activa.");
        }
        
        // Validaciones básicas
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("El nombre no puede estar vacío.");
        }
        
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new Exception("El correo electrónico no es válido.");
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            throw new Exception("El teléfono no puede estar vacío.");
        }
        
        try {
            // Recuperar el usuario actual
            Optional<User> userOptional = userService.findById(currentUserId);
            if (!userOptional.isPresent()) {
                throw new Exception("No se pudo encontrar al usuario en el sistema.");
            }
            
            User user = userOptional.get();
            // Actualizar los datos
            user.setName(name.trim());
            user.setEmail(email.trim());
            user.setPhone(phone.trim());
            
            // Guardar los cambios
            userService.update(user);
            
            return true;
        } catch (Exception e) {
            throw new Exception("Error al actualizar el perfil: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene la lista de direcciones del usuario actual.
     * @return Lista de DTOs con las direcciones del usuario
     * @throws Exception si no hay usuario logeado o ocurre un error al recuperar las direcciones
     */
    public List<AddressDTO> getUserAddresses() throws Exception {
        if (currentUserId == null) {
            System.out.println("DEBUG: currentUserId es null en getUserAddresses()");
            throw new Exception("No hay un usuario con sesión activa.");
        }
        
        System.out.println("DEBUG: Buscando usuario con ID: " + currentUserId);
        
        try {
            Optional<User> userOptional = userService.findById(currentUserId);
            if (!userOptional.isPresent()) {
                System.out.println("DEBUG: No se encontró ningún usuario con el ID: " + currentUserId);
                throw new Exception("No se pudo encontrar al usuario en el sistema.");
            }
            
            User user = userOptional.get();
            System.out.println("DEBUG: Usuario encontrado: " + user.getName() + " (Email: " + user.getEmail() + ")");
            
            List<Address> addresses = user.getAddresses();
            System.out.println("DEBUG: El usuario tiene " + addresses.size() + " direcciones");
            
            List<AddressDTO> addressDTOs = addresses.stream()
                .map(addr -> AddressDTO.builder()
                    .id(addr.getId())
                    .alias(addr.getAlias())
                    .street(addr.getStreet())
                    .city(addr.getCity())
                    .zone(addr.getZone())
                    .coordX(addr.getCoordX())
                    .coordY(addr.getCoordY())
                    .build())
                .toList();
                
            System.out.println("DEBUG: Convertidas " + addressDTOs.size() + " direcciones a DTOs");
            return addressDTOs;
        } catch (Exception e) {
            System.out.println("DEBUG: Error en getUserAddresses(): " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al recuperar las direcciones: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene una dirección por su ID.
     * @param addressId ID de la dirección a buscar
     * @return La dirección encontrada o null si no existe
     * @throws Exception si no hay usuario logeado o ocurre un error durante la búsqueda
     */
    public Address getAddressById(UUID addressId) throws Exception {
        System.out.println("DEBUG: Iniciando getAddressById() con ID: " + addressId);
        
        if (currentUserId == null) {
            System.out.println("DEBUG: No hay usuario con sesión activa");
            throw new Exception("No hay un usuario con sesión activa.");
        }
        
        if (addressId == null) {
            System.out.println("DEBUG: ID de dirección nulo");
            throw new Exception("ID de dirección inválido.");
        }
        
        try {
            // Obtener usuario
            Optional<User> userOptional = userService.findById(currentUserId);
            if (!userOptional.isPresent()) {
                System.out.println("DEBUG: No se encontró el usuario con ID: " + currentUserId);
                throw new Exception("No se pudo encontrar al usuario en el sistema.");
            }
            
            User user = userOptional.get();
            System.out.println("DEBUG: Usuario encontrado: " + user.getName());
            
            for (Address address : user.getAddresses()) {
                if (address.getId().equals(addressId)) {
                    System.out.println("DEBUG: Dirección encontrada: " + address.getAlias());
                    return address;
                }
            }
            
            System.out.println("DEBUG: No se encontró la dirección con ID: " + addressId);
            return null;
        } catch (Exception e) {
            System.out.println("DEBUG: Error en getAddressById(): " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al buscar la dirección: " + e.getMessage());
        }
    }
    
    /**
     * Agrega una nueva dirección para el usuario.
     * @param alias Alias o nombre de la dirección
     * @param street Calle y número
     * @param city Ciudad
     * @param coordX Coordenada X en el mapa
     * @param coordY Coordenada Y en el mapa
     * @return true si se agregó correctamente, false en caso contrario
     * @throws Exception si no hay usuario logeado o ocurre un error durante el proceso
     */
    public boolean addAddress(String alias, String street, String city, double coordX, double coordY) throws Exception {
        if (currentUserId == null) {
            throw new Exception("No hay un usuario con sesión activa.");
        }
        
        if (alias == null || alias.trim().isEmpty()) {
            throw new Exception("El alias de la dirección no puede estar vacío.");
        }
        
        if (street == null || street.trim().isEmpty()) {
            throw new Exception("La calle no puede estar vacía.");
        }
        
        if (city == null || city.trim().isEmpty()) {
            throw new Exception("La ciudad no puede estar vacía.");
        }
        
        try {
            Optional<User> userOptional = userService.findById(currentUserId);
            if (!userOptional.isPresent()) {
                throw new Exception("No se pudo encontrar al usuario en el sistema.");
            }
            
            User user = userOptional.get();
            
            Address address = Address.builder()
                .id(UUID.randomUUID())
                .alias(alias.trim())
                .street(street.trim())
                .city(city.trim())
                .coordX(coordX)
                .coordY(coordY)
                .build();
            
            user.getAddresses().add(address);
            
            userService.update(user);
            
            return true;
        } catch (Exception e) {
            throw new Exception("Error al agregar la dirección: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza una dirección existente.
     * @param addressId ID de la dirección a actualizar
     * @param alias Nuevo alias o nombre de la dirección
     * @param street Nueva calle y número
     * @param city Nueva ciudad
     * @param coordX Nueva coordenada X en el mapa
     * @param coordY Nueva coordenada Y en el mapa
     * @return true si se actualizó correctamente, false en caso contrario
     * @throws Exception si no hay usuario logeado o ocurre un error durante el proceso
     */
    public boolean updateAddress(UUID addressId, String alias, String street, String city, double coordX, double coordY) throws Exception {
        if (currentUserId == null) {
            throw new Exception("No hay un usuario con sesión activa.");
        }
        
        if (addressId == null) {
            throw new Exception("ID de dirección inválido.");
        }
        
        if (alias == null || alias.trim().isEmpty()) {
            throw new Exception("El alias de la dirección no puede estar vacío.");
        }
        
        if (street == null || street.trim().isEmpty()) {
            throw new Exception("La calle no puede estar vacía.");
        }
        
        if (city == null || city.trim().isEmpty()) {
            throw new Exception("La ciudad no puede estar vacía.");
        }
        
        try {
            Optional<User> userOptional = userService.findById(currentUserId);
            if (!userOptional.isPresent()) {
                throw new Exception("No se pudo encontrar al usuario en el sistema.");
            }
            
            User user = userOptional.get();
            
            Address addressToUpdate = null;
            for (Address address : user.getAddresses()) {
                if (address.getId().equals(addressId)) {
                    addressToUpdate = address;
                    break;
                }
            }
            
            if (addressToUpdate == null) {
                throw new Exception("La dirección no existe o no pertenece al usuario.");
            }
            
            addressToUpdate.setAlias(alias.trim());
            addressToUpdate.setStreet(street.trim());
            addressToUpdate.setCity(city.trim());
            addressToUpdate.setCoordX(coordX);
            addressToUpdate.setCoordY(coordY);
            
            userService.update(user);
            
            return true;
        } catch (Exception e) {
            throw new Exception("Error al actualizar la dirección: " + e.getMessage());
        }
    }
    
    /**
     * Elimina una dirección del usuario.
     * @param addressId ID de la dirección a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     * @throws Exception si no hay usuario logeado o ocurre un error durante el proceso
     */
    public boolean deleteAddress(UUID addressId) throws Exception {
        if (currentUserId == null) {
            throw new Exception("No hay un usuario con sesión activa.");
        }
        
        if (addressId == null) {
            throw new Exception("ID de dirección inválido.");
        }
        
        try {
            Optional<User> userOptional = userService.findById(currentUserId);
            if (!userOptional.isPresent()) {
                throw new Exception("No se pudo encontrar al usuario en el sistema.");
            }
            
            User user = userOptional.get();
            
            boolean removed = user.getAddresses().removeIf(address -> address.getId().equals(addressId));
            
            if (!removed) {
                throw new Exception("La dirección no existe o no pertenece al usuario.");
            }
            
            userService.update(user);
            
            return true;
        } catch (Exception e) {
            throw new Exception("Error al eliminar la dirección: " + e.getMessage());
        }
    }
}