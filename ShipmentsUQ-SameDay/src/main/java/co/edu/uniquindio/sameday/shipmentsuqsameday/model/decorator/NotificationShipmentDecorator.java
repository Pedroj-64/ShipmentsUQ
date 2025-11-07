package co.edu.uniquindio.sameday.shipmentsuqsameday.model.decorator;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.repository.ShipmentRepository;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.service.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Decorador que añade funcionalidad de notificaciones al servicio de envíos
 * Implementa el patrón Decorator para extender la funcionalidad sin modificar la clase original
 */
public class NotificationShipmentDecorator extends ServiceDecorator<Shipment, ShipmentRepository> {
    
    /**
     * Constructor del decorador
     * @param decoratedService servicio a decorar
     */
    public NotificationShipmentDecorator(Service<Shipment, ShipmentRepository> decoratedService) {
        super(decoratedService);
    }
    
    /**
     * Crea un nuevo envío y envía una notificación
     * @param entity envío a crear
     * @return envío creado
     */
    @Override
    public Shipment create(Shipment entity) {
        Shipment shipment = decoratedService.create(entity);
        sendCreationNotification(shipment);
        return shipment;
    }
    
    /**
     * Actualiza un envío y envía una notificación
     * @param entity envío a actualizar
     * @return envío actualizado
     */
    @Override
    public Shipment update(Shipment entity) {
        Shipment shipment = decoratedService.update(entity);
        sendUpdateNotification(shipment);
        return shipment;
    }
    
    /**
     * Elimina un envío y envía una notificación
     * @param id identificador del envío a eliminar
     */
    @Override
    public void delete(UUID id) {
        Optional<Shipment> shipment = findById(id);
        if (shipment.isPresent()) {
            decoratedService.delete(id);
            sendDeletionNotification(shipment.get());
        }
    }
    
    /**
     * Busca un envío por su identificador
     * @param id identificador del envío
     * @return envío encontrado o vacío si no existe
     */
    @Override
    public Optional<Shipment> findById(UUID id) {
        return decoratedService.findById(id);
    }
    
    /**
     * Envía una notificación de creación de envío
     * @param shipment envío creado
     */
    private void sendCreationNotification(Shipment shipment) {
        System.out.println("[" + LocalDateTime.now() + "] NOTIFICACIÓN: Nuevo envío creado con ID: " + 
                shipment.getId() + " para el cliente: " + shipment.getUser().getName());
        // Aquí se implementaría la lógica real de notificación (email, SMS, etc.)
    }
    
    /**
     * Envía una notificación de actualización de envío
     * @param shipment envío actualizado
     */
    private void sendUpdateNotification(Shipment shipment) {
        System.out.println("[" + LocalDateTime.now() + "] NOTIFICACIÓN: Actualización del envío con ID: " + 
                shipment.getId() + " - Estado: " + shipment.getStatus());
        // Aquí se implementaría la lógica real de notificación (email, SMS, etc.)
    }
    
    /**
     * Envía una notificación de eliminación de envío
     * @param shipment envío eliminado
     */
    private void sendDeletionNotification(Shipment shipment) {
        System.out.println("[" + LocalDateTime.now() + "] NOTIFICACIÓN: Eliminación del envío con ID: " + 
                shipment.getId());
        // Aquí se implementaría la lógica real de notificación (email, SMS, etc.)
    }
}