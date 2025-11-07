package co.edu.uniquindio.sameday.shipmentsuqsameday.model.decorator;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Decorador que añade validaciones adicionales al servicio de envíos
 * Implementa el patrón Decorator para extender la funcionalidad sin modificar la clase original
 */
public class ValidationShipmentDecorator extends ServiceDecorator<Shipment, ShipmentRepository> {
    
    /**
     * Constructor del decorador
     * @param decoratedService servicio a decorar
     */
    public ValidationShipmentDecorator(Service<Shipment, ShipmentRepository> decoratedService) {
        super(decoratedService);
    }
    
    /**
     * Crea un nuevo envío con validaciones adicionales
     * @param entity envío a crear
     * @return envío creado
     * @throws IllegalArgumentException si el envío no pasa las validaciones
     */
    @Override
    public Shipment create(Shipment entity) {
        validateForCreation(entity);
        return decoratedService.create(entity);
    }
    
    /**
     * Actualiza un envío con validaciones adicionales
     * @param entity envío a actualizar
     * @return envío actualizado
     * @throws IllegalArgumentException si el envío no pasa las validaciones
     */
    @Override
    public Shipment update(Shipment entity) {
        validateForUpdate(entity);
        return decoratedService.update(entity);
    }
    
    /**
     * Elimina un envío con validaciones adicionales
     * @param id identificador del envío a eliminar
     * @throws IllegalStateException si el envío no puede ser eliminado
     */
    @Override
    public void delete(UUID id) {
        validateForDeletion(id);
        decoratedService.delete(id);
    }
    
    /**
     * Valida un envío antes de su creación
     * @param shipment envío a validar
     * @throws IllegalArgumentException si el envío no pasa las validaciones
     */
    private void validateForCreation(Shipment shipment) {
        if (shipment.getOrigin() == null) {
            throw new IllegalArgumentException("La dirección de origen no puede ser nula");
        }
        
        if (shipment.getDestination() == null) {
            throw new IllegalArgumentException("La dirección de destino no puede ser nula");
        }
        
        if (shipment.getUser() == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        
        if (shipment.getOrigin() != null && shipment.getDestination() != null && 
            shipment.getOrigin().equals(shipment.getDestination())) {
            throw new IllegalArgumentException("La dirección de origen y destino no pueden ser iguales");
        }
        
        if (shipment.getCreationDate() == null) {
            // Asignar una fecha de creación por defecto si no se proporcionó
            shipment.setCreationDate(LocalDateTime.now());
        }
    }
    
    /**
     * Valida un envío antes de su actualización
     * @param shipment envío a validar
     * @throws IllegalArgumentException si el envío no pasa las validaciones
     * @throws IllegalStateException si el envío no está en un estado válido para actualización
     */
    private void validateForUpdate(Shipment shipment) {
        if (shipment.getId() == null) {
            throw new IllegalArgumentException("El ID del envío no puede ser nulo");
        }
        
        Optional<Shipment> existingShipment = decoratedService.findById(shipment.getId());
        if (existingShipment.isEmpty()) {
            throw new IllegalArgumentException("El envío con ID " + shipment.getId() + " no existe");
        }
        
        if (existingShipment.get().getStatus() == ShipmentStatus.CANCELLED) {
            throw new IllegalStateException("No se puede actualizar un envío cancelado");
        }
        
        if (existingShipment.get().getStatus() == ShipmentStatus.DELIVERED) {
            throw new IllegalStateException("No se puede actualizar un envío que ya ha sido entregado");
        }
    }
    
    /**
     * Valida un envío antes de su eliminación
     * @param id identificador del envío a validar
     * @throws IllegalStateException si el envío no puede ser eliminado
     */
    private void validateForDeletion(UUID id) {
        Optional<Shipment> existingShipment = decoratedService.findById(id);
        if (existingShipment.isEmpty()) {
            return; // Si no existe, no hay problema en "eliminarlo"
        }
        
        ShipmentStatus status = existingShipment.get().getStatus();
        if (status != ShipmentStatus.PENDING && status != ShipmentStatus.CANCELLED) {
            throw new IllegalStateException(
                "No se puede eliminar un envío que ya está en proceso o entregado. Estado actual: " + status);
        }
    }
}