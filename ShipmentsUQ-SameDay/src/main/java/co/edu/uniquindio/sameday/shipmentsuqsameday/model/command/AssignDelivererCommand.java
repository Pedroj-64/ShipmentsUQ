package co.edu.uniquindio.sameday.shipmentsuqsameday.model.command;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.Command;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Comando para asignar un repartidor a un envío
 * Implementa el patrón Command para la operación de asignación de repartidor
 */
@Data
public class AssignDelivererCommand implements Command {
    
    private Shipment shipment;
    private Deliverer newDeliverer;
    private Deliverer previousDeliverer;
    private LocalDateTime previousAssignmentDate;
    private ShipmentStatus previousStatus;
    
    /**
     * Constructor que recibe el envío y el nuevo repartidor
     * @param shipment Envío al que se asignará el repartidor
     * @param deliverer Repartidor que se asignará al envío
     */
    public AssignDelivererCommand(Shipment shipment, Deliverer deliverer) {
        this.shipment = shipment;
        this.newDeliverer = deliverer;
        this.previousDeliverer = shipment.getDeliverer();
        this.previousAssignmentDate = shipment.getAssignmentDate();
        this.previousStatus = shipment.getStatus();
    }
    
    /**
     * Ejecuta la asignación del repartidor al envío
     * Guarda el estado anterior y actualiza el envío con el nuevo repartidor
     */
    @Override
    public void execute() {
        // Guardar estado actual
        this.previousDeliverer = shipment.getDeliverer();
        this.previousAssignmentDate = shipment.getAssignmentDate();
        this.previousStatus = shipment.getStatus();
        
        // Actualizar con el nuevo repartidor
        shipment.setDeliverer(newDeliverer);
        shipment.setAssignmentDate(LocalDateTime.now());
        
        // Actualizar estado si es necesario
        if (shipment.getStatus() == ShipmentStatus.PENDING) {
            shipment.setStatus(ShipmentStatus.ASSIGNED);
        }
    }
    
    /**
     * Deshace la asignación, restaurando el repartidor anterior
     * @return true si se pudo deshacer la asignación, false en caso contrario
     */
    @Override
    public boolean undo() {
        if (isUndoable()) {
            shipment.setDeliverer(previousDeliverer);
            shipment.setAssignmentDate(previousAssignmentDate);
            shipment.setStatus(previousStatus);
            return true;
        }
        return false;
    }
    
    /**
     * Verifica si se puede deshacer la asignación
     * Solo se puede deshacer si el envío no ha sido entregado
     * @return true si se puede deshacer, false en caso contrario
     */
    @Override
    public boolean isUndoable() {
        // Solo se puede deshacer si el envío no ha sido entregado
        return shipment.getStatus() != ShipmentStatus.DELIVERED;
    }
}