package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.App;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Address;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.internalController.ShipmentServiceRegistry;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.command.CommandManager;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.AddressDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.ShipmentDTO;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.DelivererService;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.Service;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.ShipmentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de envíos del usuario.
 * Maneja la lógica de negocio relacionada con la visualización y gestión de envíos.
 */
public class UserShipmentsController {

    private final ShipmentService baseShipmentService;
    
    private final Service<Shipment, ShipmentRepository> decoratedShipmentService;
    
    private final DelivererService delivererService;
    private UUID currentUserId;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Constructor del controlador de envíos del usuario
     */
    public UserShipmentsController() {
        // Obtener el servicio base
        this.baseShipmentService = ShipmentService.getInstance();
        
        // Obtener el servicio decorado del registro
        this.decoratedShipmentService = ShipmentServiceRegistry.getDecoratedService();
        
        this.delivererService = DelivererService.getInstance();
        
        // Obtener el ID del usuario actual
        if (App.getCurrentSession() != null) {
            currentUserId = App.getCurrentSession().getUserId();
        } else {
            // Intentar obtener el usuario desde el UserDashboardController como fallback
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
     * Obtiene los envíos del usuario actual
     * @return lista de DTOs de envío
     */
    public List<ShipmentDTO> getUserShipments() {
        // Verificar que hay un usuario activo
        if (currentUserId == null) {
            throw new IllegalStateException("No hay un usuario con sesión activa");
        }
        
        try {
            // Obtener envíos del usuario desde el servicio
            List<Shipment> shipments = baseShipmentService.findByUserId(currentUserId);
            
            // Convertir a DTOs para la vista
            return shipments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener los envíos del usuario: " + e.getMessage());
        }
    }

    /**
     * Cancela un envío
     * @param shipmentId ID del envío a cancelar
     * @return true si se canceló correctamente
     */
    public boolean cancelShipment(UUID shipmentId) {
        if (shipmentId == null) {
            return false;
        }
        
        try {
            Optional<Shipment> shipmentOpt = decoratedShipmentService.findById(shipmentId);
            if (!shipmentOpt.isPresent()) {
                return false;
            }
            
            Shipment shipment = shipmentOpt.get();
            
            if (!shipment.getUser().getId().equals(currentUserId)) {
                throw new IllegalStateException("No tiene permiso para cancelar este envío");
            }
            
            if (shipment.getStatus() != ShipmentStatus.PENDING && 
                shipment.getStatus() != ShipmentStatus.ASSIGNED) {
                throw new IllegalStateException("Este envío no puede ser cancelado en su estado actual");
            }
            
            return baseShipmentService.cancelShipment(shipmentId);
            
        } catch (Exception e) {
            System.err.println("Error al cancelar envío: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al cancelar el envío: " + e.getMessage());
        }
    }

    /**
     * Obtiene información de rastreo para un envío
     * @param shipmentId ID del envío a rastrear
     * @return información de rastreo como texto
     */
    public String getTrackingInfo(UUID shipmentId) {
        if (shipmentId == null) {
            return "ID de envío no proporcionado";
        }
        
        try {
            // Obtener el envío (usamos el decorado para tener logging)
            Optional<Shipment> shipmentOpt = decoratedShipmentService.findById(shipmentId);
            if (!shipmentOpt.isPresent()) {
                return "Envío no encontrado";
            }
            
            Shipment shipment = shipmentOpt.get();
            
            // Construir información de rastreo
            StringBuilder info = new StringBuilder();
            
            info.append("ID de envío: ").append(shipment.getId()).append("\n\n");
            
            // Estado actual
            info.append("Estado actual: ").append(shipment.getStatus()).append("\n\n");
            
            // Fechas importantes
            info.append("Creado: ").append(formatDateTime(shipment.getCreationDate())).append("\n");
            
            if (shipment.getAssignmentDate() != null) {
                info.append("Asignado: ").append(formatDateTime(shipment.getAssignmentDate())).append("\n");
            }
            
            if (shipment.getDeliveryDate() != null) {
                info.append("Entregado: ").append(formatDateTime(shipment.getDeliveryDate())).append("\n");
            }
            
            info.append("\n");
            
            // Información del repartidor
            Deliverer deliverer = shipment.getDeliverer();
            if (deliverer != null) {
                info.append("Repartidor: ").append(deliverer.getName()).append("\n");
                info.append("Contacto: ").append(deliverer.getPhone()).append("\n");
                info.append("Calificación: ").append(String.format("%.1f/5", deliverer.getAverageRating())).append("\n\n");
            } else {
                info.append("Repartidor: No asignado\n\n");
            }
            
            // Origen y destino
            Address origin = shipment.getOrigin();
            Address destination = shipment.getDestination();
            
            info.append("Origen: ").append(origin != null ? origin.getFullAddress() : "No especificado").append("\n");
            info.append("Destino: ").append(destination != null ? destination.getFullAddress() : "No especificado").append("\n\n");
            
            // Información adicional según estado
            switch(shipment.getStatus()) {
                case PENDING:
                    info.append("Su envío está pendiente de asignación a un repartidor.");
                    break;
                case ASSIGNED:
                    info.append("Su envío ha sido asignado y será recogido pronto.");
                    break;
                case IN_TRANSIT:
                    info.append("Su envío está en camino hacia el destino.");
                    break;
                case DELIVERED:
                    info.append("Su envío ha sido entregado. ¡Gracias por confiar en nosotros!");
                    break;
                case CANCELLED:
                    info.append("Este envío ha sido cancelado.");
                    break;
                case INCIDENT:
                    info.append("Se ha reportado un incidente con su envío. Estamos trabajando para resolverlo.");
                    break;
                case PENDING_REASSIGNMENT:
                    info.append("Su envío está en proceso de reasignación a otro repartidor.");
                    break;
            }
            
            return info.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al obtener información de rastreo: " + e.getMessage();
        }
    }

    /**
     * Obtiene el nombre del repartidor por su ID
     * @param delivererId ID del repartidor
     * @return nombre del repartidor o texto indicando que no está asignado
     */
    public String getDelivererName(UUID delivererId) {
        if (delivererId == null) {
            return "No asignado";
        }
        
        try {
            Optional<Deliverer> delivererOpt = delivererService.findById(delivererId);
            return delivererOpt.isPresent() ? delivererOpt.get().getName() : "No asignado";
        } catch (Exception e) {
            return "No disponible";
        }
    }

    /**
     * Convierte una entidad Shipment a su DTO para la vista
     * @param shipment la entidad a convertir
     * @return DTO del envío
     */
    private ShipmentDTO convertToDTO(Shipment shipment) {
        if (shipment == null) {
            return null;
        }
        
        // Convertir direcciones a DTOs
        AddressDTO originDTO = convertAddressToDTO(shipment.getOrigin());
        AddressDTO destinationDTO = convertAddressToDTO(shipment.getDestination());
        
        return ShipmentDTO.builder()
                .id(shipment.getId())
                .originAddress(originDTO)
                .destinationAddress(destinationDTO)
                .status(shipment.getStatus())
                .priority(shipment.getPriority())
                .weight(shipment.getWeight())
                .dimensions(shipment.getSpecialInstructions()) // Usamos specialInstructions para almacenar dimensiones
                .cost(shipment.getCost())
                .creationDate(shipment.getCreationDate())
                .delivererId(shipment.getDeliverer() != null ? shipment.getDeliverer().getId() : null)
                .build();
    }

    /**
     * Convierte una entidad Address a su DTO
     * @param address la dirección a convertir
     * @return DTO de la dirección
     */
    private AddressDTO convertAddressToDTO(Address address) {
        if (address == null) {
            return null;
        }
        
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
     * Formatea una fecha para mostrarla en la información de rastreo
     * @param dateTime la fecha a formatear
     * @return fecha formateada como texto
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(dateTimeFormatter) : "No disponible";
    }
    
    /**
     * Deshace la última operación realizada
     * @return true si se deshizo correctamente, false en caso contrario
     */
    public boolean undoLastOperation() {
        try {
            return baseShipmentService.undoLastOperation();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Rehace la última operación deshecha
     * @return true si se rehizo correctamente, false en caso contrario
     */
    public boolean redoLastOperation() {
        try {
            return baseShipmentService.redoLastOperation();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verifica si hay operaciones para deshacer
     * @return true si hay operaciones para deshacer, false en caso contrario
     */
    public boolean canUndo() {
        try {
            return CommandManager.getInstance().canUndo();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica si hay operaciones para rehacer
     * @return true si hay operaciones para rehacer, false en caso contrario
     */
    public boolean canRedo() {
        try {
            return CommandManager.getInstance().canRedo();
        } catch (Exception e) {
            return false;
        }
    }
}