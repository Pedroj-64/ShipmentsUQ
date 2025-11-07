package co.edu.uniquindio.sameday.shipmentsuqsameday.model.service;

import java.util.UUID;

/**
 * Servicio encargado de manejar las reasignaciones de envíos
 * cuando ocurren incidentes que lo requieren
 */
public class ShipmentReassignmentService {
    private final ShipmentService shipmentService;
    private final IncidentService incidentService;

    public ShipmentReassignmentService(ShipmentService shipmentService, IncidentService incidentService) {
        this.shipmentService = shipmentService;
        this.incidentService = incidentService;
    }

    /**
     * Maneja la reasignación de un envío basado en un incidente
     * @param incidentId ID del incidente que requiere reasignación
     * @param reason Motivo de la reasignación
     */
    public void handleReassignment(UUID incidentId, String reason) {
        incidentService.getRepository().findById(incidentId).ifPresent(incident -> {
            if (!incident.isResolved()) {
                shipmentService.reassignShipment(incident.getShipment().getId(), reason);
            }
        });
    }
}