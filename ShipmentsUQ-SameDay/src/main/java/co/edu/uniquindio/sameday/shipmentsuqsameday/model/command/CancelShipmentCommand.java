package co.edu.uniquindio.sameday.shipmentsuqsameday.model.command;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Shipment;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums.ShipmentStatus;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces.Command;

import lombok.Data;

/**
 * Comando para cancelar un envío
 * Implementa el patrón Command para la operación de cancelación
 */
@Data
public class CancelShipmentCommand implements Command {
    
    private Shipment shipment;
    private ShipmentStatus previousStatus;
    
    /**
     * Constructor que recibe el envío a cancelar
     * @param shipment Envío que se va a cancelar
     */
    public CancelShipmentCommand(Shipment shipment) {
        this.shipment = shipment;
        this.previousStatus = shipment.getStatus();
    }
    
    /**
     * Ejecuta la cancelación del envío
     * Guarda el estado anterior y cambia el estado a CANCELADO
     */
    @Override
    public void execute() {
        this.previousStatus = shipment.getStatus();
        shipment.setStatus(ShipmentStatus.CANCELLED);
    }
    
    /**
     * Deshace la cancelación, restaurando el estado anterior
     * @return true si se pudo deshacer la cancelación
     */
    @Override
    public boolean undo() {
        if (isUndoable()) {
            shipment.setStatus(previousStatus);
            return true;
        }
        return false;
    }
    
    /**
     * Verifica si se puede deshacer la cancelación
     * Solo se puede deshacer si el envío no estaba entregado o en tránsito
     * @return true si se puede deshacer, false en caso contrario
     */
    @Override
    public boolean isUndoable() {
        // Solo se puede deshacer si el envío no estaba entregado o en tránsito
        return previousStatus != ShipmentStatus.DELIVERED 
            && previousStatus != ShipmentStatus.IN_TRANSIT;
    }
}