package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.ShipmentDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentPriority;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador para la funcionalidad de cotización y creación de envíos.
 * Maneja la lógica de negocio relacionada con la generación de cotizaciones y creación de envíos.
 */
public class QuoteShipmentController {
    
    private final ShipmentService shipmentService;
    private final UserService userService;
    private UUID currentUserId;
    
    /**
     * Constructor del controlador de cotización
     */
    public QuoteShipmentController() {
        this.shipmentService = ShipmentService.getInstance();
        this.userService = UserService.getInstance();
        
        if (App.getCurrentSession() != null) {
            currentUserId = App.getCurrentSession().getUserId();
        } else {
            User dashboardUser = UserDashboardController.getCurrentUser();
            if (dashboardUser != null) {
                currentUserId = dashboardUser.getId();
            }
        }
        
        if (currentUserId == null) {
            throw new IllegalStateException("No hay un usuario con sesión activa");
        }
    }
    
    /**
     * Obtiene la lista de direcciones del usuario actual
     * @return lista de DTO de direcciones
     */
    public List<AddressDTO> getUserAddresses() {
        Optional<User> userOptional = userService.findById(currentUserId);
        if (!userOptional.isPresent()) {
            throw new IllegalStateException("No se pudo encontrar el usuario actual");
        }
        
        User user = userOptional.get();
        return user.getAddresses().stream()
                .map(this::convertAddressToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica si una dirección es la predeterminada del usuario
     * @param addressDTO la dirección a verificar
     * @return true si es la dirección predeterminada
     */
    public boolean isDefaultAddress(AddressDTO addressDTO) {
        Optional<User> userOptional = userService.findById(currentUserId);
        if (!userOptional.isPresent()) {
            return false;
        }
        
        User user = userOptional.get();
        return user.getAddresses().stream()
                .filter(Address::isDefault)
                .anyMatch(addr -> addr.getId().equals(addressDTO.getId()));
    }
    
    /**
     * Calcula la tarifa estimada para un envío
     * @param origin origen del envío
     * @param destination destino del envío
     * @param weight peso en kg
     * @param dimensions dimensiones en formato "largo x ancho x alto"
     * @param priority prioridad del envío
     * @return tarifa calculada
     */
    public double calculateShipmentRate(AddressDTO origin, AddressDTO destination, 
                                        double weight, String dimensions, ShipmentPriority priority) {
        Address originAddress = findAddressByDTO(origin);
        Address destinationAddress = findAddressByDTO(destination);
        
        if (originAddress == null || destinationAddress == null) {
            throw new IllegalArgumentException("No se encontraron las direcciones especificadas");
        }
        
        return shipmentService.calculateShippingRate(originAddress, destinationAddress, weight, priority);
    }
    
    /**
     * Crea un nuevo envío a partir de los datos de cotización
     * @param shipmentDTO datos del envío cotizado
     * @return el envío creado
     */
    public Shipment createShipment(ShipmentDTO shipmentDTO) {
        if (shipmentDTO == null) {
            throw new IllegalArgumentException("Los datos del envío no pueden ser nulos");
        }
        
        Optional<User> userOptional = userService.findById(currentUserId);
        if (!userOptional.isPresent()) {
            throw new IllegalStateException("No se pudo encontrar el usuario actual");
        }
        User user = userOptional.get();
        
        Address originAddress = findAddressByDTO(shipmentDTO.getOriginAddress());
        Address destinationAddress = findAddressByDTO(shipmentDTO.getDestinationAddress());
        
        if (originAddress == null || destinationAddress == null) {
            throw new IllegalArgumentException("No se encontraron las direcciones especificadas");
        }
        
        return shipmentService.createShipment(
                user,
                originAddress,
                destinationAddress,
                shipmentDTO.getWeight(),
                shipmentDTO.getDimensions(),
                shipmentDTO.getPriority());
    }
    
    /**
     * Convierte una entidad Address a su DTO
     * @param address la dirección a convertir
     * @return DTO de la dirección
     */
    private AddressDTO convertAddressToDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .alias(address.getAlias())
                .street(address.getStreet())
                .city(address.getCity())
                .zone(address.getZone())
                .coordX(address.getCoordX())
                .coordY(address.getCoordY())
                .build();
    }
    
    /**
     * Busca una dirección por su DTO
     * @param addressDTO el DTO de la dirección a buscar
     * @return la dirección encontrada o null
     */
    private Address findAddressByDTO(AddressDTO addressDTO) {
        Optional<User> userOptional = userService.findById(currentUserId);
        if (!userOptional.isPresent()) {
            return null;
        }
        
        User user = userOptional.get();
        return user.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressDTO.getId()))
                .findFirst()
                .orElse(null);
    }
}