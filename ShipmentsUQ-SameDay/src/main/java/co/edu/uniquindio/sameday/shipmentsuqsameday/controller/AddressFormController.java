package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.App;

import java.util.Optional;
import java.util.UUID;

/**
 * Controlador para el formulario de direcciones.
 * Maneja la lógica de negocio relacionada con la creación, edición y validación de direcciones.
 */
public class AddressFormController {

    private final UserService userService;
    private UUID currentUserId;
    private Address addressToEdit; // En caso de edición, contiene la dirección a editar
    private boolean editMode = false;

    /**
     * Constructor para el controlador del formulario de direcciones.
     * Inicializa los servicios necesarios y obtiene el ID del usuario actual.
     */
    public AddressFormController() {
        System.out.println("DEBUG: Inicializando AddressFormController");
        userService = UserService.getInstance();
        
        // Obtener el ID del usuario de la sesión actual
        if (App.getCurrentSession() != null) {
            currentUserId = App.getCurrentSession().getUserId();
            System.out.println("DEBUG: ID de usuario obtenido de la sesión: " + currentUserId);
        } else {
            System.out.println("DEBUG: App.getCurrentSession() es null");
            
            // Intentar obtener el usuario desde el UserDashboardController
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
     * Establece el modo de edición y la dirección a editar.
     * @param address La dirección a editar
     */
    public void setAddressToEdit(Address address) {
        this.addressToEdit = address;
        this.editMode = (address != null);
    }
    
    /**
     * Verifica si está en modo edición.
     * @return true si está editando una dirección existente
     */
    public boolean isEditMode() {
        return editMode;
    }
    
    /**
     * Obtiene la dirección que se está editando.
     * @return La dirección en edición o null si es modo creación
     */
    public Address getAddressToEdit() {
        return addressToEdit;
    }
    
    /**
     * Guarda una nueva dirección para el usuario actual.
     * @param alias Alias o nombre de la dirección
     * @param street Calle y número
     * @param zone Zona o sector de la ciudad
     * @param city Ciudad
     * @param zipCode Código postal
     * @param complement Información adicional o complemento
     * @param coordX Coordenada X en el mapa
     * @param coordY Coordenada Y en el mapa
     * @param isDefault Si es la dirección predeterminada
     * @return true si se guardó correctamente, false en caso contrario
     * @throws Exception si ocurre un error durante el proceso
     */
    public boolean saveAddress(String alias, String street, String zone, String city, 
                            String zipCode, String complement, double coordX, double coordY, 
                            boolean isDefault) throws Exception {
        System.out.println("DEBUG: Iniciando saveAddress()");
                            
        if (currentUserId == null) {
            System.out.println("DEBUG: No hay ID de usuario disponible");
            throw new Exception("No hay un usuario con sesión activa.");
        }
        
        // Validaciones básicas
        validateAddressFields(alias, street, city, coordX, coordY);
        
        try {
            // Obtener usuario
            Optional<User> userOptional = userService.findById(currentUserId);
            if (!userOptional.isPresent()) {
                System.out.println("DEBUG: No se encontró el usuario con ID: " + currentUserId);
                throw new Exception("No se pudo encontrar al usuario en el sistema.");
            }
            
            User user = userOptional.get();
            System.out.println("DEBUG: Usuario encontrado: " + user.getName());
            
            // Crear o actualizar dirección
            Address address;
            
            if (editMode && addressToEdit != null) {
                // Actualizar dirección existente
                address = addressToEdit;
                address.setAlias(alias.trim());
                address.setStreet(street.trim());
                address.setZone(zone != null ? zone.trim() : "");
                address.setCity(city.trim());
                address.setZipCode(zipCode != null ? zipCode.trim() : "");
                address.setComplement(complement != null ? complement.trim() : "");
                address.setCoordX(coordX);
                address.setCoordY(coordY);
                address.setDefault(isDefault);
                
                System.out.println("DEBUG: Actualizando dirección existente con ID: " + address.getId());
            } else {
                // Crear nueva dirección
                address = Address.builder()
                    .id(UUID.randomUUID())
                    .alias(alias.trim())
                    .street(street.trim())
                    .zone(zone != null ? zone.trim() : "")
                    .city(city.trim())
                    .zipCode(zipCode != null ? zipCode.trim() : "")
                    .complement(complement != null ? complement.trim() : "")
                    .coordX(coordX)
                    .coordY(coordY)
                    .isDefault(isDefault)
                    .build();
                
                // Añadir la nueva dirección a la lista del usuario
                user.getAddresses().add(address);
                System.out.println("DEBUG: Añadida nueva dirección con ID: " + address.getId());
            }
            
            // Si es la dirección predeterminada, actualizar las demás
            if (isDefault) {
                System.out.println("DEBUG: Estableciendo como predeterminada y actualizando otras direcciones");
                for (Address otherAddress : user.getAddresses()) {
                    if (!otherAddress.getId().equals(address.getId())) {
                        otherAddress.setDefault(false);
                    }
                }
            }
            
            // Guardar cambios en el usuario
            userService.update(user);
            System.out.println("DEBUG: Usuario actualizado correctamente");
            
            return true;
        } catch (Exception e) {
            System.out.println("DEBUG: Error en saveAddress(): " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al guardar la dirección: " + e.getMessage());
        }
    }
    
    /**
     * Valida los campos obligatorios de la dirección.
     * @param alias Alias de la dirección
     * @param street Calle
     * @param city Ciudad
     * @param coordX Coordenada X
     * @param coordY Coordenada Y
     * @throws Exception si algún campo obligatorio está vacío
     */
    private void validateAddressFields(String alias, String street, String city, double coordX, double coordY) throws Exception {
        if (alias == null || alias.trim().isEmpty()) {
            throw new Exception("El alias de la dirección no puede estar vacío.");
        }
        
        if (street == null || street.trim().isEmpty()) {
            throw new Exception("La calle no puede estar vacía.");
        }
        
        if (city == null || city.trim().isEmpty()) {
            throw new Exception("La ciudad no puede estar vacía.");
        }
        
        if (coordX == 0 && coordY == 0) {
            throw new Exception("Debe seleccionar una ubicación en el mapa.");
        }
    }
}